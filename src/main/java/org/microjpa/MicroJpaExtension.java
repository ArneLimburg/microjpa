/*
 * Copyright 2020 - 2021 Arne Limburg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.microjpa;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Collections.unmodifiableList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;
import static javax.persistence.PersistenceContextType.TRANSACTION;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.ConversationScoped;
import javax.enterprise.context.Destroyed;
import javax.enterprise.context.Initialized;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.event.TransactionPhase;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.CDI;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.ProcessObserverMethod;
import javax.enterprise.inject.spi.configurator.AnnotatedTypeConfigurator;
import javax.enterprise.util.AnnotationLiteral;
import javax.enterprise.util.Nonbinding;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.persistence.PersistenceContexts;
import javax.persistence.PersistenceProperty;
import javax.persistence.PersistenceUnit;
import javax.persistence.PersistenceUnits;
import javax.persistence.SynchronizationType;
import javax.transaction.TransactionScoped;

public class MicroJpaExtension implements Extension {

    private static final String JTA_DATA_SOURCE_PROPERTY = "javax.persistence.jtaDataSource";
    private static final String BEAN_MANAGER_PROPERTY = "javax.persistence.bean.manager";
    private static final PersistenceProperty[] EMPTY_PERSISTENCE_PROPERTIES = new PersistenceProperty[0];
    private static final String NAME = "name";
    private static final List<String> NONBINDING_PROPERTIES = unmodifiableList(asList(NAME, "properties"));
    private static final List<Class<? extends Annotation>> DEFAULT_SCOPE_TYPES
        = unmodifiableList(asList(ApplicationScoped.class, SessionScoped.class, ConversationScoped.class, RequestScoped.class));

    private Map<PersistenceUnitLiteral, Map<String, Object>> persistenceProperties = new ConcurrentHashMap<>();
    private Set<PersistenceContextLiteral> persistenceContexts
        = Collections.newSetFromMap(new ConcurrentHashMap<PersistenceContextLiteral, Boolean>());
    private TransactionContext transactionContext = new TransactionContext();
    private ExtendedPersistenceContext extendedPersistenceContext = new ExtendedPersistenceContext();

    public void addQualifiers(@Observes BeforeBeanDiscovery event) {
        event.configureQualifier(PersistenceUnit.class)
            .filterMethods(m -> m.getJavaMember().getName().equals(NAME))
            .forEach(m -> m.add(Nonbinding.Literal.INSTANCE));
        event.configureQualifier(PersistenceContext.class)
            .filterMethods(m -> NONBINDING_PROPERTIES.contains(m.getJavaMember().getName()))
            .forEach(m -> m.add(Nonbinding.Literal.INSTANCE));
    }

    public void configurePersistenceAnnotations(@Observes ProcessAnnotatedType<?> event) {
        AnnotatedType<?> annotatedType = event.getAnnotatedType();

        if (isPersistenceAnnotationPresent(annotatedType.getFields())) {
            AnnotatedTypeConfigurator<?> typeConfigurator = event.configureAnnotatedType();
            typeConfigurator.fields().stream()
                .filter(configurer -> isPersistenceAnnotationPresent(configurer.getAnnotated()))
                .forEach(configurer -> {
                    configurer.add(new AnnotationLiteral<Inject>() { });
                    ofNullable(configurer.getAnnotated().getAnnotation(PersistenceUnit.class)).ifPresent(this::initPersistenceProperties);
                    ofNullable(configurer.getAnnotated().getAnnotation(PersistenceContext.class)).ifPresent(this::addPersistenceContext);
                });
        }
        annotatedType.getMethods().stream()
            .map(method -> ofNullable(method.getAnnotation(PersistenceUnit.class)))
            .filter(Optional::isPresent).map(Optional::get)
            .forEach(this::initPersistenceProperties);
        annotatedType.getMethods().stream()
            .map(method -> ofNullable(method.getAnnotation(PersistenceContext.class)))
            .filter(Optional::isPresent).map(Optional::get)
            .forEach(this::addPersistenceContext);
        ofNullable(annotatedType.getAnnotation(PersistenceUnit.class)).ifPresent(this::initPersistenceProperties);
        ofNullable(annotatedType.getAnnotation(PersistenceContext.class)).ifPresent(this::addPersistenceContext);
        ofNullable(annotatedType.getAnnotation(PersistenceUnits.class))
            .ifPresent(units -> stream(units.value()).forEach(this::initPersistenceProperties));
        ofNullable(annotatedType.getAnnotation(PersistenceContexts.class))
            .ifPresent(contexts -> stream(contexts.value()).forEach(this::addPersistenceContext));
    }

    public void addTransactionHandling(@Observes ProcessObserverMethod<?, ?> observerMethodEvent) {
        if (!observerMethodEvent.getObserverMethod().isAsync()) {
            TransactionPhase transactionPhase = observerMethodEvent.getObserverMethod().getTransactionPhase();
            switch (transactionPhase) {
                case IN_PROGRESS:
                    if (observerMethodEvent.getObserverMethod().getObservedQualifiers().stream()
                        .noneMatch(this::isInitializedOrDestroyedDefaultScopeQualifier)) {

                        observerMethodEvent.configureObserverMethod()
                            .addQualifier(new InProgress.Literal())
                            .transactionPhase(TransactionPhase.IN_PROGRESS);
                    }
                    break;
                case BEFORE_COMPLETION:
                    observerMethodEvent.configureObserverMethod()
                        .addQualifier(new BeforeCompletion.Literal())
                        .transactionPhase(TransactionPhase.IN_PROGRESS);
                    break;
                case AFTER_COMPLETION:
                    observerMethodEvent.configureObserverMethod()
                        .addQualifier(new AfterCompletion.Literal())
                        .transactionPhase(TransactionPhase.IN_PROGRESS);
                    break;
                case AFTER_FAILURE:
                    observerMethodEvent.configureObserverMethod()
                        .addQualifier(new AfterFailure.Literal())
                        .transactionPhase(TransactionPhase.IN_PROGRESS);
                    break;
                case AFTER_SUCCESS:
                    observerMethodEvent.configureObserverMethod()
                        .addQualifier(new AfterSuccess.Literal())
                        .transactionPhase(TransactionPhase.IN_PROGRESS);
                    break;
                default:
                    throw new IllegalStateException("Unsupported transaction phase " + transactionPhase);
            }
        }
    }

    private void initPersistenceProperties(PersistenceUnit persistenceUnit) {
        persistenceProperties.computeIfAbsent(new PersistenceUnitLiteral(persistenceUnit), p -> new HashMap<>());
    }

    private void addPersistenceContext(PersistenceContext persistenceContext) {
        persistenceContexts.add(new PersistenceContextLiteral(persistenceContext));
        PersistenceUnitLiteral persistenceUnitKey = new PersistenceUnitLiteral(persistenceContext);
        persistenceProperties.computeIfAbsent(persistenceUnitKey, p -> new HashMap<>())
            .putAll(stream(persistenceContext.properties()).collect(toMap(PersistenceProperty::name, PersistenceProperty::value)));
    }

    public void addBeans(@Observes AfterBeanDiscovery event, BeanManager beanManager) {
        event.addBean()
            .scope(ApplicationScoped.class)
            .addType(TransactionContext.class)
            .createWith(c -> transactionContext);
        event.addContext(transactionContext);
        event.addBean()
            .scope(ApplicationScoped.class)
            .addType(ExtendedPersistenceContext.class)
            .createWith(c -> extendedPersistenceContext);
        event.addObserverMethod()
            .beanClass(ExtendedPersistenceContext.class)
            .addQualifier(Initialized.Literal.of(RequestScoped.class))
            .observedType(Object.class)
            .notifyWith(extendedPersistenceContext::beginRequest);
        event.addObserverMethod()
            .beanClass(ExtendedPersistenceContext.class)
            .addQualifier(Destroyed.Literal.of(RequestScoped.class))
            .observedType(Object.class)
            .notifyWith(extendedPersistenceContext::endRequest);
        event.addObserverMethod()
            .beanClass(ExtendedPersistenceContext.class)
            .addQualifier(Initialized.Literal.of(TransactionScoped.class))
            .observedType(Object.class)
            .notifyWith(extendedPersistenceContext::beginTransaction);
        event.addObserverMethod()
            .beanClass(ExtendedPersistenceContext.class)
            .addQualifier(Destroyed.Literal.of(TransactionScoped.class))
            .observedType(Object.class)
            .notifyWith(extendedPersistenceContext::endTransaction);
        event.addObserverMethod()
            .beanClass(ExtendedPersistenceContext.class)
            .addQualifier(Destroyed.Literal.of(ApplicationScoped.class))
            .observedType(Object.class)
            .notifyWith(extendedPersistenceContext::endApplication);
        event.addContext(extendedPersistenceContext);
        persistenceProperties.values().forEach(properties -> overrideProperties(properties, beanManager));
        persistenceProperties.entrySet().forEach(entry -> event
            .<EntityManagerFactory>addBean()
            .scope(ApplicationScoped.class)
            .addType(EntityManagerFactory.class)
            .addQualifiers(entry.getKey())
            .createWith(c -> Persistence.createEntityManagerFactory(entry.getKey().unitName, entry.getValue()))
            .destroyWith((emf, c) -> emf.close()));
        persistenceContexts.forEach(persistenceContext -> event
            .<EntityManager>addBean()
            .scope(persistenceContext.type() == TRANSACTION ? TransactionScoped.class : PersistenceScoped.class)
            .addType(EntityManager.class)
            .addQualifiers(persistenceContext)
            .createWith(c -> {
                EntityManager entityManager = CDI.current()
                    .select(EntityManagerFactory.class, new PersistenceUnitLiteral(persistenceContext))
                    .get()
                    .createEntityManager();
                if (transactionContext.isActive()) {
                    entityManager.getTransaction().begin();
                }
                return entityManager;
            })
            .destroyWith((em, c) -> ofNullable(em).ifPresent(EntityManager::close)));
        Logger.getLogger("org.microjpa.MicroJpa").info("MicroJPA started.");
    }

    private void overrideProperties(Map<String, Object> properties, BeanManager beanManager) {
        properties.put(BEAN_MANAGER_PROPERTY, beanManager);
        properties.putAll((Map<String, String>)(Map<?, ?>)System.getProperties());
        properties.putAll(System.getenv().entrySet().stream()
            .collect(toMap(entry -> entry.getKey().replace("_", ".").toLowerCase(), Entry::getValue)));
        ofNullable(properties.get(JTA_DATA_SOURCE_PROPERTY))
            .map(Object::toString)
            .filter(String::isEmpty)
            .ifPresent(jtaDataSource -> properties.put(JTA_DATA_SOURCE_PROPERTY, null));
    }

    private <F extends AnnotatedField<?>> boolean isPersistenceAnnotationPresent(Set<F> fields) {
        return fields.stream().anyMatch(this::isPersistenceAnnotationPresent);
    }

    private <F extends AnnotatedField<?>> boolean isPersistenceAnnotationPresent(F field) {
        return field.isAnnotationPresent(PersistenceContext.class) || field.isAnnotationPresent(PersistenceUnit.class);
    }

    private boolean isInitializedOrDestroyedDefaultScopeQualifier(Annotation annotation) {
        if (annotation.annotationType().equals(Initialized.class)) {
            Initialized initialized = (Initialized)annotation;
            return DEFAULT_SCOPE_TYPES.contains(initialized.value());
        }
        if (annotation.annotationType().equals(Destroyed.class)) {
            Destroyed destroyed = (Destroyed)annotation;
            return DEFAULT_SCOPE_TYPES.contains(destroyed.value());
        }
        return false;
    }

    public static class PersistenceUnitLiteral extends AnnotationLiteral<PersistenceUnit> implements PersistenceUnit {

        private String name;
        private String unitName;

        public PersistenceUnitLiteral(PersistenceUnit unit) {
            name = unit.name();
            unitName = unit.unitName();
        }

        PersistenceUnitLiteral(PersistenceContext context) {
            name = context.name();
            unitName = context.unitName();
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public String unitName() {
            return unitName;
        }
    }

    public static class PersistenceContextLiteral extends AnnotationLiteral<PersistenceContext> implements PersistenceContext {

        private String name;
        private String unitName;
        private PersistenceContextType type;
        private SynchronizationType synchronization;

        public PersistenceContextLiteral(PersistenceContext context) {
            name = context.name();
            unitName = context.unitName();
            type = context.type();
            synchronization = context.synchronization();
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public String unitName() {
            return unitName;
        }

        @Override
        public PersistenceContextType type() {
            return type;
        }

        @Override
        public SynchronizationType synchronization() {
            return synchronization;
        }

        @Override
        public PersistenceProperty[] properties() {
            return EMPTY_PERSISTENCE_PROPERTIES;
        }
    }
}

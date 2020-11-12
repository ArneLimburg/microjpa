/*
 * Copyright 2020 Arne Limburg
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
package rocks.limburg.microjpa;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Collections.unmodifiableList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.CDI;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
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

public class MicroJpaExtension implements Extension {

    private static final String JTA_DATA_SOURCE_PROPERTY = "javax.persistence.jtaDataSource";
    private static final PersistenceProperty[] EMPTY_PERSISTENCE_PROPERTIES = new PersistenceProperty[0];
    private static final Annotation NONBINDING_LITERAL = new AnnotationLiteral<Nonbinding>() { };
    private static final String NAME = "name";
    private static final List<String> NONBINDING_PROPERTIES = unmodifiableList(asList(NAME, "properties"));

    private Map<PersistenceUnitLiteral, Map<String, String>> persistenceProperties = new ConcurrentHashMap<>();
    private Set<PersistenceContextLiteral> persistenceContexts
        = Collections.newSetFromMap(new ConcurrentHashMap<PersistenceContextLiteral, Boolean>());

    public void addQualifiers(@Observes BeforeBeanDiscovery event) {
        event.configureQualifier(PersistenceUnit.class)
            .filterMethods(m -> m.getJavaMember().getName().equals(NAME)).forEach(m -> m.add(NONBINDING_LITERAL));
        event.configureQualifier(PersistenceContext.class)
            .filterMethods(m -> NONBINDING_PROPERTIES.contains(m.getJavaMember().getName())).forEach(m -> m.add(NONBINDING_LITERAL));
    }

    public void configurePersistenceAnnotations(@Observes ProcessAnnotatedType<?> event) {
        AnnotatedType<?> annotatedType = event.getAnnotatedType();

        Consumer<PersistenceUnit> initPersistenceProperties
            = persistenceUnit -> persistenceProperties.computeIfAbsent(new PersistenceUnitLiteral(persistenceUnit), p -> new HashMap<>());
        Consumer<PersistenceContext> addPersistenceContext = persistenceContext -> addPersistenceContext(persistenceContext);

        if (isPersistenceAnnotationPresent(annotatedType.getFields())) {
            AnnotatedTypeConfigurator<?> typeConfigurator = event.configureAnnotatedType();
            typeConfigurator.fields().stream()
                .filter(configurer -> isPersistenceAnnotationPresent(configurer.getAnnotated()))
                .forEach(configurer -> {
                    configurer.add(new AnnotationLiteral<Inject>() { });
                    ofNullable(configurer.getAnnotated().getAnnotation(PersistenceUnit.class)).ifPresent(initPersistenceProperties);
                    ofNullable(configurer.getAnnotated().getAnnotation(PersistenceContext.class)).ifPresent(addPersistenceContext);
                });
        }
        annotatedType.getMethods().stream()
            .map(method -> ofNullable(method.getAnnotation(PersistenceUnit.class)))
            .filter(Optional::isPresent).map(Optional::get)
            .forEach(initPersistenceProperties);
        annotatedType.getMethods().stream()
            .map(method -> ofNullable(method.getAnnotation(PersistenceContext.class)))
            .filter(Optional::isPresent).map(Optional::get)
            .forEach(persistenceContext -> addPersistenceContext(persistenceContext));
        ofNullable(annotatedType.getAnnotation(PersistenceUnit.class)).ifPresent(initPersistenceProperties);
        ofNullable(annotatedType.getAnnotation(PersistenceContext.class)).ifPresent(addPersistenceContext);
        ofNullable(annotatedType.getAnnotation(PersistenceUnits.class))
            .ifPresent(units -> stream(units.value()).forEach(initPersistenceProperties));
        ofNullable(annotatedType.getAnnotation(PersistenceContexts.class))
            .ifPresent(contexts -> stream(contexts.value()).forEach(addPersistenceContext));
    }

    private void addPersistenceContext(PersistenceContext persistenceContext) {
        persistenceContexts.add(new PersistenceContextLiteral(persistenceContext));
        PersistenceUnitLiteral persistenceUnitKey = new PersistenceUnitLiteral(persistenceContext);
        persistenceProperties.computeIfAbsent(persistenceUnitKey, p -> new HashMap<>())
            .putAll(stream(persistenceContext.properties()).collect(toMap(PersistenceProperty::name, PersistenceProperty::value)));
    }

    public void addBeans(@Observes AfterBeanDiscovery event, BeanManager beanManager) {
        persistenceProperties.values().forEach(properties -> overrideProperties(properties));
        persistenceProperties.entrySet().forEach(entry -> event
                .<EntityManagerFactory>addBean()
                .scope(ApplicationScoped.class)
                .addType(EntityManagerFactory.class)
                .addQualifiers(entry.getKey())
                .createWith(c -> Persistence.createEntityManagerFactory(entry.getKey().unitName, entry.getValue()))
                .destroyWith((emf, c) -> emf.close()));
        persistenceContexts.forEach(persistenceContext -> event
                .<EntityManager>addBean()
                .scope(RequestScoped.class)
                .addType(EntityManager.class)
                .addQualifiers(persistenceContext)
                .createWith(c -> {
                    EntityManager entityManager = CDI.current()
                        .select(EntityManagerFactory.class, new PersistenceUnitLiteral(persistenceContext))
                        .get()
                        .createEntityManager();
                    if (TransactionalInterceptor.isTransactionActive()) {
                        entityManager.getTransaction().begin();
                    }
                    return entityManager;
                })
                .destroyWith((em, c) -> em.close()));
    }

    private void overrideProperties(Map<String, String> properties) {
        properties.putAll((Map<String, String>)(Map<?, ?>)System.getProperties());
        ofNullable(properties.get(JTA_DATA_SOURCE_PROPERTY))
            .filter(String::isEmpty)
            .ifPresent(jtaDataSource -> properties.put(JTA_DATA_SOURCE_PROPERTY, null));
    }

    private <F extends AnnotatedField<?>> boolean isPersistenceAnnotationPresent(Set<F> fields) {
        return fields.stream().filter(this::isPersistenceAnnotationPresent).findAny().isPresent();
    }

    private <F extends AnnotatedField<?>> boolean isPersistenceAnnotationPresent(F field) {
        return field.isAnnotationPresent(PersistenceContext.class) || field.isAnnotationPresent(PersistenceUnit.class);
    }

    private static class PersistenceUnitLiteral extends AnnotationLiteral<PersistenceUnit> implements PersistenceUnit {

        private String name;
        private String unitName;

        PersistenceUnitLiteral(PersistenceUnit unit) {
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

    private static class PersistenceContextLiteral extends AnnotationLiteral<PersistenceContext> implements PersistenceContext {

        private String name;
        private String unitName;
        private PersistenceContextType type;
        private SynchronizationType synchronization;

        PersistenceContextLiteral(PersistenceContext context) {
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

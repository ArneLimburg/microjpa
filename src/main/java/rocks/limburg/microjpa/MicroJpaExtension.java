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

import static java.util.Arrays.stream;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.configurator.AnnotatedTypeConfigurator;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceContexts;
import javax.persistence.PersistenceProperty;
import javax.persistence.PersistenceUnits;

public class MicroJpaExtension implements Extension {

    private Map<PersistenceUnit, Map<String, String>> persistenceProperties = new ConcurrentHashMap<>();
    private Set<PersistenceContext> persistenceContexts = Collections.newSetFromMap(new ConcurrentHashMap<PersistenceContext, Boolean>());

    public void collectAndReplacePersistenceAnnotations(@Observes ProcessAnnotatedType<?> event) {
        AnnotatedType<?> annotatedType = event.getAnnotatedType();
        if (isPersistenceAnnotationPresent(annotatedType.getFields())) {
            AnnotatedTypeConfigurator<?> typeConfigurator = event.configureAnnotatedType();
            typeConfigurator.fields().stream()
                .filter(configurer -> isPersistenceAnnotationPresent(configurer.getAnnotated()))
                .forEach(configurer -> {
                    configurer.add(new AnnotationLiteral<Inject>() { });
                    ofNullable(configurer.getAnnotated().getAnnotation(javax.persistence.PersistenceUnit.class))
                        .map(PersistenceUnit.Literal::new)
                        .ifPresent(persistenceUnit -> {
                            configurer.add(persistenceUnit);
                            persistenceProperties.put(persistenceUnit, new HashMap<>());
                        });
                    ofNullable(configurer.getAnnotated().getAnnotation(javax.persistence.PersistenceContext.class))
                        .ifPresent(persistenceContext -> {
                            configurer.add(new PersistenceContext.Literal(persistenceContext));
                            addPersistenceContext(persistenceContext);
                        });
                });
        }
        annotatedType.getMethods().stream().filter(method -> method.isAnnotationPresent(javax.persistence.PersistenceUnit.class))
            .forEach(method -> persistenceProperties.put(new PersistenceUnit.Literal(
                    method.getAnnotation(javax.persistence.PersistenceUnit.class)), new HashMap<>()));
        annotatedType.getMethods().stream().filter(method -> method.isAnnotationPresent(javax.persistence.PersistenceContext.class))
            .forEach(method -> addPersistenceContext(method.getAnnotation(javax.persistence.PersistenceContext.class)));
        ofNullable(annotatedType.getAnnotation(javax.persistence.PersistenceUnit.class))
            .ifPresent(persistenceUnit -> persistenceProperties.put(new PersistenceUnit.Literal(persistenceUnit), new HashMap<>()));
        ofNullable(annotatedType.getAnnotation(javax.persistence.PersistenceContext.class))
            .ifPresent(persistenceContext -> addPersistenceContext(persistenceContext));
        ofNullable(annotatedType.getAnnotation(PersistenceUnits.class)).ifPresent(units -> stream(units.value())
            .forEach(persistenceUnit -> persistenceProperties.put(new PersistenceUnit.Literal(persistenceUnit), new HashMap<>())));
        ofNullable(annotatedType.getAnnotation(PersistenceContexts.class)).ifPresent(contexts -> stream(contexts.value())
            .forEach(persistenceContext -> addPersistenceContext(persistenceContext)));
    }

    private void addPersistenceContext(javax.persistence.PersistenceContext persistenceContext) {
        PersistenceContext.Literal literal = new PersistenceContext.Literal(persistenceContext);
        persistenceContexts.add(literal);
        PersistenceUnit.Literal persistenceUnit = new PersistenceUnit.Literal(persistenceContext.unitName());
        persistenceProperties.computeIfAbsent(persistenceUnit, p -> new HashMap<>()).putAll(
                stream(persistenceContext.properties()).collect(toMap(PersistenceProperty::name, PersistenceProperty::value)));
    }

    public void addBeans(@Observes AfterBeanDiscovery event, BeanManager beanManager) {
        persistenceProperties.entrySet().forEach(entry -> event
                .<EntityManagerFactory>addBean()
                .scope(ApplicationScoped.class)
                .addType(EntityManagerFactory.class)
                .addQualifiers(entry.getKey())
                .createWith(c -> Persistence.createEntityManagerFactory(entry.getKey().unitName(), entry.getValue()))
                .destroyWith((emf, c) -> emf.close()));
        persistenceContexts.forEach(persistenceContext -> event
                .<EntityManager>addBean()
                .scope(ApplicationScoped.class)
                .addType(EntityManager.class)
                .addQualifiers(persistenceContext)
                .createWith(c -> {
                    EntityManager entityManager = CDI.current()
                        .select(EntityManagerFactory.class, new PersistenceUnit.Literal(persistenceContext.unitName()))
                        .get()
                        .createEntityManager();
                    if (TransactionalInterceptor.isTransactionActive()) {
                        entityManager.getTransaction().begin();
                    }
                    return entityManager;
                })
                .destroyWith((em, c) -> em.close()));
    }

    private <F extends AnnotatedField<?>> boolean isPersistenceAnnotationPresent(Set<F> fields) {
        return fields.stream().filter(this::isPersistenceAnnotationPresent).findAny().isPresent();
    }

    private <F extends AnnotatedField<?>> boolean isPersistenceAnnotationPresent(F field) {
        return field.isAnnotationPresent(javax.persistence.PersistenceContext.class)
                || field.isAnnotationPresent(javax.persistence.PersistenceUnit.class);
    }
}

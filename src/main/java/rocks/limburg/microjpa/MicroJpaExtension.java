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

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AfterTypeDiscovery;
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
import javax.persistence.PersistenceUnits;

public class MicroJpaExtension implements Extension {

    private Set<PersistenceUnit> persistenceUnits = Collections.newSetFromMap(new ConcurrentHashMap<PersistenceUnit, Boolean>());
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
                            persistenceUnits.add(persistenceUnit);
                        });
                    ofNullable(configurer.getAnnotated().getAnnotation(javax.persistence.PersistenceContext.class))
                        .map(PersistenceContext.Literal::new)
                        .ifPresent(persistenceContext -> {
                            configurer.add(persistenceContext);
                            persistenceUnits.add(new PersistenceUnit.Literal(persistenceContext.unitName()));
                            persistenceContexts.add(persistenceContext);
                        });
                });
        }
        annotatedType.getMethods().stream().filter(method -> method.isAnnotationPresent(javax.persistence.PersistenceUnit.class))
            .forEach(method -> persistenceUnits.add(new PersistenceUnit.Literal(
                    method.getAnnotation(javax.persistence.PersistenceUnit.class))));
        annotatedType.getMethods().stream().filter(method -> method.isAnnotationPresent(javax.persistence.PersistenceContext.class))
            .forEach(method -> persistenceContexts.add(new PersistenceContext.Literal(
                    method.getAnnotation(javax.persistence.PersistenceContext.class))));
        ofNullable(annotatedType.getAnnotation(javax.persistence.PersistenceUnit.class))
            .ifPresent(persistenceUnit -> persistenceUnits.add(new PersistenceUnit.Literal(persistenceUnit)));
        ofNullable(annotatedType.getAnnotation(javax.persistence.PersistenceContext.class))
            .ifPresent(persistenceContext -> persistenceContexts.add(new PersistenceContext.Literal(persistenceContext)));
        ofNullable(annotatedType.getAnnotation(PersistenceUnits.class)).ifPresent(units -> stream(units.value())
            .forEach(persistenceUnit -> persistenceUnits.add(new PersistenceUnit.Literal(persistenceUnit))));
        ofNullable(annotatedType.getAnnotation(PersistenceContexts.class)).ifPresent(contexts -> stream(contexts.value())
            .forEach(persistenceContext -> persistenceContexts.add(new PersistenceContext.Literal(persistenceContext))));
    }

    public void enableTransactionalInterceptors(@Observes AfterTypeDiscovery event) {
        event.getInterceptors().add(TransactionalInterceptor.class);
    }

    public void addBeans(@Observes AfterBeanDiscovery event, BeanManager beanManager) {
        persistenceUnits.forEach(persistenceUnit -> event
                .<EntityManagerFactory>addBean()
                .scope(ApplicationScoped.class)
                .addType(EntityManagerFactory.class)
                .addQualifiers(persistenceUnit)
                .createWith(c -> Persistence.createEntityManagerFactory(persistenceUnit.unitName()))
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

/*
 * Copyright 2021 Arne Limburg
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
package org.microjpa.event;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.NotificationOptions;
import javax.enterprise.util.AnnotationLiteral;
import javax.enterprise.util.TypeLiteral;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

@ApplicationScoped
public class EventProducer {

    @Inject
    private Event<SomeEvent> eventPublisher;

    @Inject
    @SomeQualifier
    private Event<Object> qualifiedEventPublisher;

    @PersistenceContext(unitName = "test-unit")
    private EntityManager entityManager;

    private Runnable inProgress;
    private Runnable beforeCompletion;
    private Runnable afterCompletion;
    private Runnable afterFailure;
    private Runnable afterSuccess;

    public EventProducer assertThatInProgress(Runnable inProgress) {
        this.inProgress = inProgress;
        return this;
    }

    public EventProducer assertThatBeforeCompletion(Runnable beforeCompletion) {
        this.beforeCompletion = beforeCompletion;
        return this;
    }

    public EventProducer assertThatAfterCompletion(Runnable afterCompletion) {
        this.afterCompletion = afterCompletion;
        return this;
    }

    public EventProducer assertThatAfterFailure(Runnable afterFailure) {
        this.afterFailure = afterFailure;
        return this;
    }

    public EventProducer assertThatAfterSuccess(Runnable afterSuccess) {
        this.afterSuccess = afterSuccess;
        return this;
    }

    @Transactional
    public void fireInTransaction(SomeEvent event) {
        eventPublisher.fire(event);
        inProgress.run();
    }

    @Transactional
    public void fireQualifiedInTransaction(SomeEvent event) {
        qualifiedEventPublisher.select(SomeEvent.class).fire(event);
        inProgress.run();
    }

    @Transactional
    public void fireDoubleQualifiedInTransaction(SomeEvent event) {
        qualifiedEventPublisher.select(new TypeLiteral<SomeEvent>() { }).select(new AnnotationLiteral<AnotherQualifier>() { }).fire(event);
        inProgress.run();
    }

    @Transactional
    public void fireInTransactionWithException(SomeEvent event) {
        eventPublisher.fire(event);
        inProgress.run();
        throw new RuntimeException();
    }

    @Transactional
    public void fireWithEntitymanagerInTransactionWithException(SomeEvent event) {
        entityManager.getCriteriaBuilder();
        eventPublisher.fire(event);
        inProgress.run();
        throw new RuntimeException();
    }

    @Transactional
    public void fireInTransactionWithRollback(SomeEvent event) {
        eventPublisher.fire(event);
        inProgress.run();
        throw new RuntimeException();
    }

    public void fireWithoutTransaction(SomeEvent event) {
        eventPublisher.fire(event);
        inProgress.run();
    }

    public void fireAsync(SomeEvent event) {
        eventPublisher.fireAsync(event);
        inProgress.run();
    }

    public void fireQualifiedAsync(SomeEvent event) {
        qualifiedEventPublisher.fireAsync(event);
        inProgress.run();
    }

    public void fireDoubleQualifiedAsync(SomeEvent event) {
        qualifiedEventPublisher
            .select(new AnnotationLiteral<AnotherQualifier>() { })
            .fireAsync(event, NotificationOptions.of("testkey", "testvalue"));
        inProgress.run();
    }
}

/*
 * Copyright 2021 - 2024 Arne Limburg
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

import static jakarta.interceptor.Interceptor.Priority.LIBRARY_AFTER;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.concurrent.CompletionStage;
import java.util.function.BiConsumer;

import jakarta.annotation.Priority;
import jakarta.decorator.Decorator;
import jakarta.decorator.Delegate;
import jakarta.enterprise.event.Event;
import jakarta.enterprise.event.NotificationOptions;
import jakarta.enterprise.util.AnnotationLiteral;
import jakarta.enterprise.util.TypeLiteral;
import jakarta.inject.Inject;
import jakarta.transaction.Status;
import jakarta.transaction.Synchronization;
import jakarta.transaction.TransactionSynchronizationRegistry;

@Decorator
@Priority(LIBRARY_AFTER)
public class TransactionalEventDecorator<T> implements Event<T>, Serializable {

    private Event<T> delegate;
    private TransactionContext transactionContext;
    private TransactionSynchronizationRegistry registry;
    private ExtendedPersistenceContext extendedContext;

    @Inject
    public TransactionalEventDecorator(
        @Delegate Event<T> delegate,
        TransactionContext transactionContext,
        TransactionSynchronizationRegistry registry,
        ExtendedPersistenceContext extendedContext) {

        this.delegate = delegate;
        this.transactionContext = transactionContext;
        this.registry = registry;
        this.extendedContext = extendedContext;
    }

    @Override
    public void fire(T event) {
        delegate.select(new AnnotationLiteral<InProgress>() { }).fire(event);
        if (transactionContext.isActive()) {
            registry.registerInterposedSynchronization(new Synchronization() {

                @Override
                public void beforeCompletion() {
                    delegate.select(new AnnotationLiteral<BeforeCompletion>() { }).fire(event);
                }

                @Override
                public void afterCompletion(int status) {
                    if (status == Status.STATUS_COMMITTED) {
                        delegate.select(new AnnotationLiteral<AfterSuccess>() { }).fire(event);
                    } else if (status == Status.STATUS_ROLLEDBACK) {
                        delegate.select(new AnnotationLiteral<AfterFailure>() { }).fire(event);
                    }
                    delegate.select(new AnnotationLiteral<AfterCompletion>() { }).fire(event);
                }
            });
        }
    }

    @Override
    public Event<T> select(Annotation... qualifiers) {
        return new TransactionalEventDecorator<>(delegate.select(qualifiers), transactionContext, registry, extendedContext);
    }

    @Override
    public <U extends T> Event<U> select(Class<U> subtype, Annotation... qualifiers) {
        return new TransactionalEventDecorator<>(delegate.select(subtype, qualifiers), transactionContext, registry, extendedContext);
    }

    @Override
    public <U extends T> Event<U> select(TypeLiteral<U> subtype, Annotation... qualifiers) {
        return new TransactionalEventDecorator<>(delegate.select(subtype, qualifiers), transactionContext, registry, extendedContext);
    }

    @Override
    public <U extends T> CompletionStage<U> fireAsync(U event) {
        return fireAsync(event, NotificationOptions.builder().build()).whenComplete(deactivateExtendedContext());
    }

    @Override
    public <U extends T> CompletionStage<U> fireAsync(U event, NotificationOptions options) {
        return delegate.fireAsync(event, options).whenComplete(deactivateExtendedContext());
    }

    private <U> BiConsumer<U, Throwable> deactivateExtendedContext() {
        return (result, exception) -> extendedContext.deactivate();
    }
}

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

import static java.util.Optional.ofNullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.logging.Logger;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ContextNotActiveException;
import jakarta.enterprise.context.Destroyed;
import jakarta.enterprise.context.Initialized;
import jakarta.enterprise.context.spi.Context;
import jakarta.enterprise.event.Event;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.transaction.Synchronization;
import jakarta.transaction.SystemException;
import jakarta.transaction.TransactionScoped;
import jakarta.transaction.TransactionSynchronizationRegistry;
import jakarta.transaction.UserTransaction;

@PersistenceScoped
public class MicroTransaction implements UserTransaction, EntityTransaction, TransactionSynchronizationRegistry {

    private static final Logger LOG = Logger.getLogger(MicroTransaction.class.getName());

    @Inject
    private BeanManager beanManager;
    @Inject
    private TransactionContext transactionContext;
    @Inject @Initialized(TransactionScoped.class)
    private Event<MicroTransaction> transactionScopedInitializedEvent;
    @Inject @Destroyed(TransactionScoped.class)
    private Event<MicroTransaction> transactionScopedDestroyedEvent;

    private Object transactionKey;
    private TransactionStatus status = TransactionStatus.STATUS_NO_TRANSACTION;
    private List<Synchronization> synchronizations;
    private Map<Object, Object> transactionResources;

    @PostConstruct
    public void initializeSynchronizations() {
        synchronizations = new ArrayList<>();
    }

    @Override
    public Object getTransactionKey() {
        return transactionKey;
    }

    @Override
    public void begin() {
        transactionContext.activate();
        transactionScopedInitializedEvent.fire(this);
        transactionKey = UUID.randomUUID();
        status = TransactionStatus.STATUS_ACTIVE;
        EntityManagerOperation beginTransaction = operation(em -> em.getTransaction().begin());
        getActiveEntityManagers().forEach(beginTransaction);
        LOG.fine("Transaction started.");
        beginTransaction.checkForException();
    }

    @Override
    public void commit() {
        status = TransactionStatus.STATUS_COMMITTING;
        synchronizations.forEach(Synchronization::beforeCompletion);
        EntityManagerOperation commitTransaction = operation(em -> em.getTransaction().commit());
        getActiveEntityManagers().forEach(commitTransaction);
        status = TransactionStatus.STATUS_COMMITTED;
        end();
        LOG.fine("Transaction committed.");
        commitTransaction.checkForException();
    }

    @Override
    public void rollback() {
        status = TransactionStatus.STATUS_ROLLING_BACK;
        synchronizations.forEach(Synchronization::beforeCompletion);
        EntityManagerOperation rollbackTransaction = operation(em -> em.getTransaction().rollback());
        getActiveEntityManagers().forEach(rollbackTransaction);
        status = TransactionStatus.STATUS_ROLLEDBACK;
        end();
        LOG.fine("Transaction rolled back.");
        rollbackTransaction.checkForException();
    }

    @Override
    public boolean getRollbackOnly() {
        if (status == TransactionStatus.STATUS_MARKED_ROLLBACK) {
            return true;
        }
        boolean rollbackOnly = getActiveEntityManagers().stream()
            .map(EntityManager::getTransaction)
            .anyMatch(EntityTransaction::getRollbackOnly);
        if (rollbackOnly) {
            status = TransactionStatus.STATUS_MARKED_ROLLBACK;
            LOG.fine("Transaction marked as rollback only.");
        }
        return rollbackOnly;
    }

    @Override
    public void setRollbackOnly() {
        EntityManagerOperation setRollbackOnly = operation(em -> em.getTransaction().setRollbackOnly());
        getActiveEntityManagers().forEach(setRollbackOnly);
        status = TransactionStatus.STATUS_MARKED_ROLLBACK;
        LOG.fine("Transaction marked as rollback only.");
        setRollbackOnly.checkForException();
    }

    @Override
    public boolean isActive() {
        return status == TransactionStatus.STATUS_ACTIVE;
    }

    @Override
    public int getTransactionStatus() {
        return status.ordinal();
    }

    @Override
    public int getStatus() throws SystemException {
        return getTransactionStatus();
    }

    @Override
    public void setTransactionTimeout(int seconds) throws SystemException {
        throw new UnsupportedOperationException("transaction timeout is not supported");
    }

    @Override
    public void registerInterposedSynchronization(Synchronization synchronization) {
        synchronizations.add(synchronization);
    }

    @Override
    public void putResource(Object key, Object value) {
        if (transactionResources == null) {
            transactionResources = new HashMap<>();
        }
        transactionResources.put(key, value);
    }

    @Override
    public Object getResource(Object key) {
        return ofNullable(transactionResources).map(r -> r.get(key)).orElse(null);
    }

    private void end() {
        synchronizations.forEach(s -> s.afterCompletion(getTransactionStatus()));
        transactionContext.deactivate();
        transactionKey = null;
        transactionResources = null;
        transactionScopedDestroyedEvent.fire(this);
    }

    private List<EntityManager> getActiveEntityManagers() {
        Set<Bean<? extends EntityManager>> entityManagerBeans
            = (Set<Bean<? extends EntityManager>>)(Set<?>)beanManager.getBeans(EntityManager.class, new Any.Literal());

        List<EntityManager> entityManagers = new ArrayList<>();
        entityManagerBeans.forEach(bean -> {
            try {
                Context context = beanManager.getContext(bean.getScope());
                ofNullable(context.get(bean)).ifPresent(entityManagers::add);
            } catch (ContextNotActiveException e) {
                // thrown when the context is not active,
                // in which case no entity manager is available for that bean.
                // So the exception can safely be ignored.
            }
        });
        return entityManagers;
    }

    private EntityManagerOperation operation(Consumer<EntityManager> consumer) {
        return new EntityManagerOperation(consumer);
    }

    private static class EntityManagerOperation implements Consumer<EntityManager> {

        private Consumer<EntityManager> delegate;
        private RuntimeException thrownException;

        EntityManagerOperation(Consumer<EntityManager> delegate) {
            this.delegate = delegate;
        }

        @Override
        public void accept(EntityManager em) {
            try {
                delegate.accept(em);
            } catch (RuntimeException e) {
                thrownException = e;
            }
        }

        public void checkForException() {
            if (thrownException != null) {
                throw thrownException;
            }
        }
    }

    private enum TransactionStatus {
        STATUS_ACTIVE,
        STATUS_MARKED_ROLLBACK,
        STATUS_PREPARED,
        STATUS_COMMITTED,
        STATUS_ROLLEDBACK,
        STATUS_UNKNOWN,
        STATUS_NO_TRANSACTION,
        STATUS_PREPARING,
        STATUS_COMMITTING,
        STATUS_ROLLING_BACK;
    }
}

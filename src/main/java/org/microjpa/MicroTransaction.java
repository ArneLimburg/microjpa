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
package org.microjpa;

import static java.util.Optional.ofNullable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ContextNotActiveException;
import javax.enterprise.context.spi.Context;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.TransactionScoped;
import javax.transaction.TransactionSynchronizationRegistry;
import javax.transaction.UserTransaction;

@TransactionScoped
public class MicroTransaction implements UserTransaction, EntityTransaction, TransactionSynchronizationRegistry, Serializable {

    @Inject
    private BeanManager beanManager;
    @Inject
    private TransactionContext transactionContext;

    private TransactionStatus status = TransactionStatus.STATUS_NO_TRANSACTION;
    private List<Synchronization> synchronizations;
    private Map<Object, Object> transactionResources;

    @PostConstruct
    public void initializeSynchronizations() {
        synchronizations = new ArrayList<>();
    }

    @Override
    public Object getTransactionKey() {
        return System.identityHashCode(this);
    }

    @Override
    public void begin() {
        transactionContext.activate();
        status = TransactionStatus.STATUS_ACTIVE;
        EntityManagerOperation beginTransaction = operation(em -> em.getTransaction().begin());
        getActiveEntityManagers().forEach(beginTransaction);
        beginTransaction.checkForException();
    }

    @Override
    public void commit() {
        status = TransactionStatus.STATUS_COMMITTING;
        synchronizations.forEach(Synchronization::beforeCompletion);
        EntityManagerOperation commitTransaction = operation(em -> em.getTransaction().commit());
        getActiveEntityManagers().forEach(commitTransaction);
        status = TransactionStatus.STATUS_COMMITTED;
        synchronizations.forEach(s -> s.afterCompletion(getTransactionStatus()));
        transactionContext.deactivate();
        commitTransaction.checkForException();
    }

    @Override
    public void rollback() {
        status = TransactionStatus.STATUS_ROLLING_BACK;
        synchronizations.forEach(Synchronization::beforeCompletion);
        EntityManagerOperation rollbackTransaction = operation(em -> em.getTransaction().rollback());
        getActiveEntityManagers().forEach(rollbackTransaction);
        status = TransactionStatus.STATUS_ROLLEDBACK;
        synchronizations.forEach(s -> s.afterCompletion(getTransactionStatus()));
        transactionContext.deactivate();
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
        }
        return rollbackOnly;
    }

    @Override
    public void setRollbackOnly() {
        EntityManagerOperation setRollbackOnly = operation(em -> em.getTransaction().setRollbackOnly());
        getActiveEntityManagers().forEach(setRollbackOnly);
        status = TransactionStatus.STATUS_MARKED_ROLLBACK;
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
            transactionResources = new HashMap<Object, Object>();
        }
        transactionResources.put(key, value);
    }

    @Override
    public Object getResource(Object key) {
        return ofNullable(transactionResources).map(r -> r.get(key)).orElse(null);
    }

    private List<EntityManager> getActiveEntityManagers() {
        Set<Bean<? extends EntityManager>> entityManagerBeans = (Set<Bean<? extends EntityManager>>)(Set<?>)beanManager
                .getBeans(EntityManager.class, new Any.Literal());
        List<EntityManager> entityManagers = new ArrayList<>();
        entityManagerBeans.forEach(bean -> {
            try {
                Context context = beanManager.getContext(bean.getScope());
                if (context.isActive()) {
                    Optional.ofNullable(context.get(bean)).ifPresent(entityManagers::add);
                }
            } catch (ContextNotActiveException e) {
                // thrown by weld when the context is not active
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

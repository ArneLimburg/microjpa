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
package org.microjpa;

import static java.util.Optional.ofNullable;
import static javax.interceptor.Interceptor.Priority.LIBRARY_AFTER;
import static javax.transaction.Transactional.TxType.REQUIRED;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import javax.annotation.Priority;
import javax.ejb.ApplicationException;
import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.Context;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.transaction.Transactional;

@Dependent
@Transactional(REQUIRED)
@Interceptor
@Priority(LIBRARY_AFTER)
public class TransactionalInterceptor {

    private static final boolean APPLICATION_EXCEPTION_AVAILABLE;
    static {
        boolean applicationExceptionAvailable = true;
        try {
            Class.forName("javax.ejb.ApplicationException");
        } catch (ClassNotFoundException e) {
            applicationExceptionAvailable = false;
        }
        APPLICATION_EXCEPTION_AVAILABLE = applicationExceptionAvailable;
    }

    @Inject
    private BeanManager beanManager;
    @Inject
    private TransactionContext transactionContext;

    @AroundInvoke
    public Object transactional(InvocationContext context) throws Exception {
        boolean beginInThisMethod = !transactionContext.isActive();
        if (beginInThisMethod) {
            transactionContext.activate();
            getActiveEntityManagers().stream().map(EntityManager::getTransaction).forEach(EntityTransaction::begin);
        }
        try {
            return context.proceed();
        } catch (Exception e) {
            checkRollback(e);
            throw e;
        } finally {
            if (beginInThisMethod) {
                getActiveEntityManagers().stream().map(EntityManager::getTransaction).forEach(completeTransaction());
                transactionContext.deactivate();
            }
        }
    }

    private Consumer<EntityTransaction> completeTransaction() {
        return t -> {
            if (t.getRollbackOnly()) {
                t.rollback();
            } else {
                t.commit();
            }
        };
    }

    private List<EntityManager> getActiveEntityManagers() {
        Set<Bean<? extends EntityManager>> entityManagerBeans = (Set<Bean<? extends EntityManager>>)(Set<?>)beanManager
                .getBeans(EntityManager.class, new Any.Literal());
        List<EntityManager> entityManagers = new ArrayList<>();
        entityManagerBeans.forEach(bean -> {
            Context context = beanManager.getContext(bean.getScope());
            Optional.ofNullable(context.get(bean)).ifPresent(entityManagers::add);
        });
        return entityManagers;
    }

    private void checkRollback(Exception e) {
        if (APPLICATION_EXCEPTION_AVAILABLE) {
            new ApplicationExceptionRollbackPolicy(getActiveEntityManagers()).checkRollback(e);
        } else {
            getActiveEntityManagers().stream().map(EntityManager::getTransaction).forEach(EntityTransaction::setRollbackOnly);
        }
    }

    private static class ApplicationExceptionRollbackPolicy {

        private List<EntityManager> entityManagers;

        ApplicationExceptionRollbackPolicy(List<EntityManager> activeEntityManagers) {
            entityManagers = activeEntityManagers;
        }

        public void checkRollback(Exception e) {
            Optional<ApplicationException> annotation = ofNullable(e.getClass().getAnnotation(ApplicationException.class));
            if (annotation.isPresent()) {
                if (annotation.get().rollback()) {
                    setRollbackOnly();
                }
            } else {
                checkInheritedRollback(e.getClass().getSuperclass());
            }
        }

        private void checkInheritedRollback(Class<?> exceptionType) {
            if (Exception.class.equals(exceptionType)) {
                setRollbackOnly();
                return;
            }
            Optional<ApplicationException> annotation = ofNullable(exceptionType.getAnnotation(ApplicationException.class));
            if (annotation.isPresent()) {
                ApplicationException applicationExceptionAnnotation = annotation.get();
                if (applicationExceptionAnnotation.inherited() && applicationExceptionAnnotation.rollback()) {
                    setRollbackOnly();
                }
            } else {
                checkInheritedRollback(exceptionType.getSuperclass());
            }
        }

        private void setRollbackOnly() {
            entityManagers.stream().map(EntityManager::getTransaction).forEach(EntityTransaction::setRollbackOnly);
        }
    }
}

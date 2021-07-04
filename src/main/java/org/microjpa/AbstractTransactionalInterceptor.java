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

import static java.util.Optional.ofNullable;

import java.util.Optional;

import javax.ejb.ApplicationException;
import javax.inject.Inject;
import javax.interceptor.InvocationContext;
import javax.persistence.EntityTransaction;

public abstract class AbstractTransactionalInterceptor {

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
    private TransactionContext context;
    @Inject
    private EntityTransaction transaction;

    protected Object transactional(InvocationContext context) throws Exception {
        try {
            return context.proceed();
        } catch (Exception e) {
            checkRollback(e);
            throw e;
        }
    }

    protected boolean isTransactionActive() {
        return context.isActive();
    }

    protected void beginTransaction() {
        transaction.begin();
    }

    protected void completeTransaction() {
        if (transaction.getRollbackOnly()) {
            transaction.rollback();
        } else {
            transaction.commit();
        }
    }

    private void checkRollback(Exception e) {
        if (APPLICATION_EXCEPTION_AVAILABLE) {
            new ApplicationExceptionRollbackPolicy().checkRollback(e);
        } else {
            transaction.setRollbackOnly();
        }
    }

    private class ApplicationExceptionRollbackPolicy {

        public void checkRollback(Exception e) {
            Optional<ApplicationException> annotation = ofNullable(e.getClass().getAnnotation(ApplicationException.class));
            if (annotation.isPresent()) {
                if (annotation.get().rollback()) {
                    transaction.setRollbackOnly();
                }
            } else {
                checkInheritedRollback(e.getClass().getSuperclass());
            }
        }

        private void checkInheritedRollback(Class<?> exceptionType) {
            if (Exception.class.equals(exceptionType)) {
                transaction.setRollbackOnly();
                return;
            }
            Optional<ApplicationException> annotation = ofNullable(exceptionType.getAnnotation(ApplicationException.class));
            if (annotation.isPresent()) {
                ApplicationException applicationExceptionAnnotation = annotation.get();
                if (applicationExceptionAnnotation.inherited() && applicationExceptionAnnotation.rollback()) {
                    transaction.setRollbackOnly();
                }
            } else {
                checkInheritedRollback(exceptionType.getSuperclass());
            }
        }
    }
}

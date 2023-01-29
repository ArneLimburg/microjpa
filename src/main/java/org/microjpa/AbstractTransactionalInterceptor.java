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

import java.io.Serializable;
import java.util.Optional;

import javax.ejb.ApplicationException;
import javax.inject.Inject;
import javax.interceptor.InvocationContext;
import javax.persistence.EntityTransaction;
import javax.transaction.Transactional;

public abstract class AbstractTransactionalInterceptor implements Serializable {

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
    private static final Class[] ROLLBACK_ON = {RuntimeException.class};

    @Inject
    private TransactionContext context;
    @Inject
    private EntityTransaction transaction;

    protected Object transactional(InvocationContext context) throws Exception {
        try {
            return context.proceed();
        } catch (Exception e) {
            checkRollback(getTransactional(context), e);
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

    private Transactional getTransactional(InvocationContext context) {
        Transactional transactional = context.getMethod().getAnnotation(Transactional.class);
        if (transactional != null) {
            return transactional;
        }
        Class<?> type = context.getTarget().getClass();
        do {
            transactional = type.getAnnotation(Transactional.class);
            type = type.getSuperclass();
        } while (transactional == null);
        return transactional;
    }

    private void checkRollback(Transactional transactional, Exception exception) {
        RollbackPolicy rollbackPolicy;
        if (APPLICATION_EXCEPTION_AVAILABLE) {
            rollbackPolicy = new ApplicationExceptionRollbackPolicy(transactional);
        } else {
            rollbackPolicy = new RollbackPolicy(transactional);
        }
        rollbackPolicy.checkRollback(exception);
    }

    private class RollbackPolicy implements Serializable {

        private Class[] rollbackOn;
        private Class[] dontRollbackOn;

        protected RollbackPolicy(Transactional transactional) {
            rollbackOn = transactional.rollbackOn();
            dontRollbackOn = transactional.dontRollbackOn();
            if (rollbackOn.length == 0) {
                rollbackOn = ROLLBACK_ON;
            }
        }

        public void checkRollback(Exception e) {
            checkInheritedRollback(e.getClass());
        }

        protected void checkInheritedRollback(Class<?> exceptionType) {
            if (shouldNotRollback(exceptionType)) {
                // don't roll back
                return;
            }
            if (shouldRollback(exceptionType)) {
                transaction.setRollbackOnly();
                return;
            }
            checkInheritedRollback(exceptionType.getSuperclass());
        }

        protected boolean shouldRollback(Class<?> exceptionType) {
            for (Class<?> rollbackOnClass: rollbackOn) {
                if (rollbackOnClass.equals(exceptionType)) {
                    return true;
                }
            }
            return false;
        }

        protected boolean shouldNotRollback(Class<?> exceptionType) {
            if (exceptionType == null) {
                return true;
            }
            for (Class<?> dontRollbackOnClass: dontRollbackOn) {
                if (dontRollbackOnClass.equals(exceptionType)) {
                    return true;
                }
            }
            return false;
        }
    }

    private class ApplicationExceptionRollbackPolicy extends RollbackPolicy implements Serializable {

        protected ApplicationExceptionRollbackPolicy(Transactional transactional) {
            super(transactional);
        }

        @Override
        public void checkRollback(Exception e) {
            Optional<ApplicationException> annotation = ofNullable(e.getClass().getAnnotation(ApplicationException.class));
            if (annotation.isPresent()) {
                if (annotation.get().rollback()) {
                    transaction.setRollbackOnly();
                }
            } else {
                checkInheritedRollback(e.getClass());
            }
        }

        @Override
        protected void checkInheritedRollback(Class<?> exceptionType) {
            if (shouldNotRollback(exceptionType)) {
                // don't roll back
                return;
            }
            if (shouldRollback(exceptionType)) {
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

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

import static javax.interceptor.Interceptor.Priority.LIBRARY_AFTER;
import static javax.transaction.Transactional.TxType.REQUIRED;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import javax.annotation.Priority;
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

    @Inject
    private BeanManager beanManager;

    private static ThreadLocal<Boolean> transactionActive = new ThreadLocal<>();

    public static boolean isTransactionActive() {
        return Optional.ofNullable(transactionActive.get()).orElse(Boolean.FALSE);
    }

    @AroundInvoke
    public Object transactional(InvocationContext context) throws Exception {
        boolean beginInThisMethod = !isTransactionActive();
        if (beginInThisMethod) {
            getActiveEntityManagers().stream().map(EntityManager::getTransaction).forEach(EntityTransaction::begin);
            transactionActive.set(Boolean.TRUE);
        }
        try {
            return context.proceed();
        } catch (Exception e) {
            getActiveEntityManagers().stream().map(EntityManager::getTransaction).forEach(EntityTransaction::setRollbackOnly);
            throw e;
        } finally {
            if (beginInThisMethod) {
                transactionActive.remove();
                getActiveEntityManagers().stream().map(EntityManager::getTransaction).forEach(completeTransaction());
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
}

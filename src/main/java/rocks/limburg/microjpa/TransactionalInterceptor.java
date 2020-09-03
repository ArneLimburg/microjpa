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

import static javax.interceptor.Interceptor.Priority.LIBRARY_AFTER;
import static javax.transaction.Transactional.TxType.REQUIRED;

import javax.annotation.Priority;
import javax.enterprise.context.Dependent;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

@Dependent
@Transactional(REQUIRED)
@Interceptor
@Priority(LIBRARY_AFTER)
public class TransactionalInterceptor {

    @PersistenceContext(unitName = "test-unit")
    private EntityManager entityManager;

    public TransactionalInterceptor() {
        // TODO Auto-generated constructor stub
    }
    @AroundInvoke
    public Object transactional(InvocationContext context) throws Exception {
        entityManager.getTransaction().begin();
        try {
            return context.proceed();
        } finally {
            entityManager.getTransaction().commit();
        }
    }
}

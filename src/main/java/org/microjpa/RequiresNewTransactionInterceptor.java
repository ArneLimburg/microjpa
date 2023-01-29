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

import static javax.interceptor.Interceptor.Priority.LIBRARY_AFTER;
import static javax.transaction.Transactional.TxType.REQUIRES_NEW;

import javax.annotation.Priority;
import javax.enterprise.context.Dependent;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import javax.transaction.Transactional;

@Dependent
@Transactional(REQUIRES_NEW)
@Interceptor
@Priority(LIBRARY_AFTER)
public class RequiresNewTransactionInterceptor extends AbstractTransactionalInterceptor {

    @Override
    @AroundInvoke
    public Object transactional(InvocationContext context) throws Exception {
        if (isTransactionActive()) {
            throw new UnsupportedOperationException("REQUIRES_NEW is not supported by MicroJPA when transaction is already active");
        }
        beginTransaction();
        try {
            return super.transactional(context);
        } finally {
            completeTransaction();
        }
    }
}

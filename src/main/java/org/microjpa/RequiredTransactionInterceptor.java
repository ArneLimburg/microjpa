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
import static jakarta.transaction.Transactional.TxType.REQUIRED;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.Dependent;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import jakarta.transaction.Transactional;

@Dependent
@Transactional(REQUIRED)
@Interceptor
@Priority(LIBRARY_AFTER)
public class RequiredTransactionInterceptor extends AbstractTransactionalInterceptor {

    @Override
    @AroundInvoke
    public Object transactional(InvocationContext context) throws Exception {
        boolean beginInThisMethod = !isTransactionActive();
        if (beginInThisMethod) {
            beginTransaction();
        }
        try {
            return super.transactional(context);
        } finally {
            if (beginInThisMethod) {
                completeTransaction();
            }
        }
    }
}

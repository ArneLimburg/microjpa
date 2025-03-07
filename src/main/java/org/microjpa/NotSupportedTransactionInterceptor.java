/*
 * Copyright 2021 - 2025 Arne Limburg
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

import static jakarta.transaction.Transactional.TxType.NOT_SUPPORTED;
import static org.microjpa.AbstractTransactionalInterceptor.TRANSACTIONAL_INTERCEPTOR_PRIORITY;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.Dependent;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import jakarta.transaction.Transactional;

@Dependent
@Transactional(NOT_SUPPORTED)
@Interceptor
@Priority(TRANSACTIONAL_INTERCEPTOR_PRIORITY)
public class NotSupportedTransactionInterceptor extends AbstractTransactionalInterceptor {

    @Override
    @AroundInvoke
    public Object transactional(InvocationContext context) throws Exception {
        if (isTransactionActive()) {
            throw new UnsupportedOperationException("NOT_SUPPORTED is not supported by MicroJPA when transaction is already active");
        }
        return super.transactional(context);
    }
}

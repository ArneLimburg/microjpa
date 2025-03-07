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
package org.microjpa.beans;

import static javax.persistence.PersistenceContextType.EXTENDED;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

@ApplicationScoped
@Transactional(TxType.NEVER) // overridden by method level annotations
public class InnerBean {

    @PersistenceContext(unitName = "test-unit", type = EXTENDED)
    private EntityManager entityManager;

    @Transactional(TxType.REQUIRED)
    public boolean isTransactionActiveWithRequiredTransaction() {
        return entityManager.getTransaction().isActive();
    }

    @Transactional(TxType.REQUIRES_NEW)
    public boolean isTransactionActiveWithRequiresNewTransaction() {
        return entityManager.getTransaction().isActive();
    }

    @Transactional(TxType.MANDATORY)
    public boolean isTransactionActiveWithMandatoryTransaction() {
        return entityManager.getTransaction().isActive();
    }

    @Transactional(TxType.SUPPORTS)
    public boolean isTransactionActiveWithSupportsTransaction() {
        return entityManager.getTransaction().isActive();
    }

    @Transactional(TxType.NOT_SUPPORTED)
    public boolean isTransactionActiveWithNotSupportedTransaction() {
        return entityManager.getTransaction().isActive();
    }

    @Transactional(TxType.NEVER)
    public boolean isTransactionActiveWithNeverTransaction() {
        return entityManager.getTransaction().isActive();
    }
}

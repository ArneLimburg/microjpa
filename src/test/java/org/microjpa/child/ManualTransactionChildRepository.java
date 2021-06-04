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
package org.microjpa.child;

import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.microjpa.TransactionContext;

public class ManualTransactionChildRepository extends AbstractChildRepository {

    @PersistenceContext(unitName = "test-unit")
    private EntityManager entityManager;
    @Inject
    private TransactionContext transactionContext;

    @Override
    protected EntityManager getEntityManager() {
        return entityManager;
    }

    public void persistWithManualRollback(TestChild testChild) {
        persist(testChild);
        entityManager.getTransaction().setRollbackOnly();
    }

    public List<TestChild> findAll() {
        transactionContext.activate();
        try {
            return super.findAll();
        } finally {
            transactionContext.deactivate();
        }
    }

    public void clear() {
        // do nothing
    }
}

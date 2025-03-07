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
package org.microjpa.relation;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.Transactional;
import javax.transaction.UserTransaction;

import org.microjpa.TransactionContext;
import org.microjpa.child.ManualTransactionChildRepository;
import org.microjpa.child.TestChild;
import org.microjpa.parent.ManualTransactionParentRepository;
import org.microjpa.parent.TestParent;

@ApplicationScoped
public class ManualTransactionRelationService
    extends AbstractRelationService<ManualTransactionParentRepository, ManualTransactionChildRepository> {

    @Inject
    private TransactionContext transactionContext;
    @Inject
    private UserTransaction transaction;

    public void persist(TestChild testChild) {
        try {
            transactionContext.activate();
            transaction.begin();
            childRepository.persist(testChild);
            transaction.commit();
        } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException | HeuristicRollbackException e) {
            throw new IllegalStateException(e);
        }
    }

    public void persistWithManualRollback(TestChild testChild) {
        try {
            transactionContext.activate();
            transaction.begin();
            childRepository.persist(testChild);
            transaction.setRollbackOnly();
            transaction.commit();
        } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException | HeuristicRollbackException e) {
            throw new IllegalStateException(e);
        }
    }

    @Transactional
    public void transactionalPersistWithRollback(TestChild testChild) {
        childRepository.persistWithManualRollback(testChild);
    }

    public Relation findParentAndChild(long parentId) {
        try {
            transactionContext.activate();
            transaction.begin();
            TestParent parent = parentRepository.find(parentId);
            TestChild child = childRepository.findByParentId(parentId);
            transaction.commit();
            return new Relation(parent, child);
        } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException | HeuristicRollbackException e) {
            throw new IllegalStateException(e);
        }
    }
}

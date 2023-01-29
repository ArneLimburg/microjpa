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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import javax.persistence.RollbackException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microjpa.child.ManualTransactionChildRepository;
import org.microjpa.child.TestChild;
import org.microjpa.parent.ManualTransactionParentRepository;
import org.microjpa.relation.ManualTransactionRelationService;
import org.microjpa.test.CdiExtension;

@ExtendWith(CdiExtension.class)
class ManualTransactionTest extends
    AbstractPersistenceUnitTest<ManualTransactionRelationService, ManualTransactionParentRepository, ManualTransactionChildRepository> {

    @Test
    @DisplayName("transaction.setRollbackOnly leads to RollbackException on commit")
    public void manualRollback() {
        List<TestChild> children = testChildRepository.findAll();

        assertThrows(RollbackException.class, () -> testService.persistWithManualRollback(new TestChild()));

        assertEquals(children.size(), testChildRepository.findAll().size());
    }

    @Test
    @DisplayName("entityTransaction.setRollbackOnly in @Transactional leads to rollback")
    public void transactionalManualRollback() {
        List<TestChild> children = testChildRepository.findAll();

        testService.transactionalPersistWithRollback(new TestChild());

        assertEquals(children.size(), testChildRepository.findAll().size());
    }
}

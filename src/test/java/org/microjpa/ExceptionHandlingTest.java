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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.microjpa.child.TestChild;
import org.microjpa.child.TransactionalChildRepository;
import org.microjpa.exception.InheritingRollbackApplicationException;
import org.microjpa.exception.InheritingRollbackApplicationExceptionSubclass;
import org.microjpa.parent.TransactionalParentRepository;
import org.microjpa.relation.TransactionalRelationService;
import org.microjpa.tags.MultiplePersistenceUnitsTest;

@MultiplePersistenceUnitsTest
public class ExceptionHandlingTest
    extends AbstractPersistenceUnitTest<TransactionalRelationService, TransactionalParentRepository, TransactionalChildRepository> {

    @Test
    @DisplayName("persist rolls back on exception")
    void rollbackOnPersist() {

        testService.findParentAndChild(parentId);
        TestChild newChild = new TestChild();
        assertThrows(IllegalStateException.class, () -> testService.persistWithException(newChild));
        testChildRepository.clear();
        assertEquals(1, testChildRepository.findAll().size());
    }

    @Test
    @DisplayName("persist rolls back on nested exception")
    void nestedRollbackOnPersist() {

        testService.findParentAndChild(parentId);
        TestChild newChild = new TestChild();
        assertThrows(IllegalStateException.class, () -> testService.persistWithNestedRuntimeException(newChild));
        testChildRepository.clear();
        assertEquals(1, testChildRepository.findAll().size());
    }

    @Test
    @DisplayName("persist rolls back when it throws exception annotated with @ApplicationException(rollback = true, inherited = true)")
    void nestedRollbackOnPersistWithInheritingApplicationException() {


        testService.findParentAndChild(parentId);
        TestChild newChild = new TestChild();
        assertThrows(InheritingRollbackApplicationException.class,
            () -> testService.persistWithInheritingRollbackApplicationException(newChild));
        testChildRepository.clear();
        assertEquals(1, testChildRepository.findAll().size());
    }

    @Test
    @DisplayName("persist rolls back when it throws exception "
        + "with superclass annotated with @ApplicationException(rollback = true, inherited = true)")
    void nestedRollbackOnPersistWithInheritingApplicationExceptionSuperclass() {

        testService.findParentAndChild(parentId);
        TestChild newChild = new TestChild();
        assertThrows(InheritingRollbackApplicationExceptionSubclass.class,
            () -> testService.persistWithInheritingRollbackApplicationExceptionSubclass(newChild));
        testChildRepository.clear();
        assertEquals(1, testChildRepository.findAll().size());
    }
}

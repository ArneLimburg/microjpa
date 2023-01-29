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
import org.junit.jupiter.api.extension.ExtendWith;
import org.microjpa.child.TestChild;
import org.microjpa.child.TransactionalChildRepository;
import org.microjpa.exception.InheritingNoRollbackApplicationException;
import org.microjpa.exception.InheritingNoRollbackApplicationExceptionSubclass;
import org.microjpa.exception.NoRollbackApplicationException;
import org.microjpa.exception.RollbackApplicationException;
import org.microjpa.exception.RollbackApplicationExceptionSubclass;
import org.microjpa.parent.TransactionalParentRepository;
import org.microjpa.relation.TransactionalRelationService;
import org.microjpa.test.CdiExtension;

@ExtendWith(CdiExtension.class)
class ExceptionHandlingWithApplicationExceptionTest
    extends AbstractPersistenceUnitTest<TransactionalRelationService, TransactionalParentRepository, TransactionalChildRepository> {

    @Test
    @DisplayName("persist throws exception annotated with @ApplicationException(rollback = false, inherited = false)")
    void persistWithNoRollbackApplicationException() {

        testService.findParentAndChild(parentId);
        TestChild newChild = new TestChild();
        assertThrows(NoRollbackApplicationException.class, () -> testService.persistWithNoRollbackApplicationException(newChild));
        testChildRepository.clear();
        assertEquals(2, testChildRepository.findAll().size());
    }

    @Test
    @DisplayName("persist rolls back when it throws exception annotated with @ApplicationException(rollback = true, inherited = false)")
    void nestedRollbackOnPersistWithApplicationException() {


        testService.findParentAndChild(parentId);
        TestChild newChild = new TestChild();
        assertThrows(RollbackApplicationException.class, () -> testService.persistWithRollbackApplicationException(newChild));
        testChildRepository.clear();
        assertEquals(1, testChildRepository.findAll().size());
    }

    @Test
    @DisplayName("persist rolls back when it throws exception with superclass "
        + "annotated with @ApplicationException(rollback = true, inherited = false)")
    void nestedRollbackOnPersistWithApplicationExceptionSuperclass() {

        testService.findParentAndChild(parentId);
        TestChild newChild = new TestChild();
        assertThrows(RollbackApplicationExceptionSubclass.class,
            () -> testService.persistWithRollbackApplicationExceptionSubclass(newChild));
        testChildRepository.clear();
        assertEquals(2, testChildRepository.findAll().size());
    }

    @Test
    @DisplayName("persist throws exception with superclass annotated with @ApplicationException(rollback = false, inherited = false)")
    void persistWithApplicationExceptionSuperclass() {

        testService.findParentAndChild(parentId);
        TestChild newChild = new TestChild();
        assertThrows(NoRollbackApplicationException.class, () -> testService.persistWithNoRollbackApplicationExceptionSubclass(newChild));
        testChildRepository.clear();
        assertEquals(2, testChildRepository.findAll().size());
    }

    @Test
    @DisplayName("persist throws exception annotated with @ApplicationException(rollback = false, inherited = true)")
    void persistWithInheritingNoRollbackApplicationException() {

        testService.findParentAndChild(parentId);
        TestChild newChild = new TestChild();
        assertThrows(InheritingNoRollbackApplicationException.class,
            () -> testService.persistWithInheritingNoRollbackApplicationException(newChild));
        testChildRepository.clear();
        assertEquals(2, testChildRepository.findAll().size());
    }

    @Test
    @DisplayName("persist throws exception with superclass annotated with @ApplicationException(rollback = false, inherited = true)")
    void persistWithInheritingApplicationExceptionSuperclass() {

        testService.findParentAndChild(parentId);
        TestChild newChild = new TestChild();
        assertThrows(InheritingNoRollbackApplicationExceptionSubclass.class,
            () -> testService.persistWithInheritingNoRollbackApplicationExceptionSubclass(newChild));
        testChildRepository.clear();
        assertEquals(2, testChildRepository.findAll().size());
    }
}

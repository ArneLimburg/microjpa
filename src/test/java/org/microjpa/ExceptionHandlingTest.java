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

import java.io.IOException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microjpa.child.TestChild;
import org.microjpa.child.TransactionalChildRepository;
import org.microjpa.exception.InheritingRollbackApplicationException;
import org.microjpa.exception.InheritingRollbackApplicationExceptionSubclass;
import org.microjpa.parent.TransactionalParentRepository;
import org.microjpa.relation.TransactionalRelationService;
import org.microjpa.relation.TransactionalRelationService.NoRollbackException;
import org.microjpa.relation.TransactionalRelationService.RollbackException;
import org.microjpa.tags.ApplicationExceptionTest;
import org.microjpa.test.CdiExtension;

@ApplicationExceptionTest
@ExtendWith(CdiExtension.class)
class ExceptionHandlingTest
    extends AbstractPersistenceUnitTest<TransactionalRelationService, TransactionalParentRepository, TransactionalChildRepository> {

    @Test
    @DisplayName("persist rolls back on runtime exception")
    void rollbackOnPersist() {

        testService.findParentAndChild(parentId);
        TestChild newChild = new TestChild();
        assertThrows(IllegalStateException.class, () -> testService.persistWithRuntimeException(newChild));
        testChildRepository.clear();
        assertEquals(1, testChildRepository.findAll().size());
    }

    @Test
    @DisplayName("persist rolls back on nested runtime exception")
    void nestedRollbackOnPersist() {

        testService.findParentAndChild(parentId);
        TestChild newChild = new TestChild();
        assertThrows(IllegalStateException.class, () -> testService.persistWithNestedRuntimeException(newChild));
        testChildRepository.clear();
        assertEquals(1, testChildRepository.findAll().size());
    }

    @Test
    @DisplayName("persist does not roll back on checked exception")
    void noRollbackOnCheckedException() {

        testService.findParentAndChild(parentId);
        TestChild newChild = new TestChild();
        assertThrows(IOException.class, () -> testService.persistWithCheckedException(newChild));
        testChildRepository.clear();
        assertEquals(2, testChildRepository.findAll().size());
    }

    @Test
    @DisplayName("persist does not roll back on nested checked exception")
    void noRollbackOnNestedCheckedException() {

        testService.findParentAndChild(parentId);
        TestChild newChild = new TestChild();
        assertThrows(IOException.class, () -> testService.persistWithNestedCheckedException(newChild));
        testChildRepository.clear();
        assertEquals(2, testChildRepository.findAll().size());
    }

    @Test
    @DisplayName("@Transactional(dontRollbackOn = ...) takes precedence")
    void dontRollbackTakesPrecedence() {

        testService.findParentAndChild(parentId);
        TestChild newChild = new TestChild();
        assertThrows(NoRollbackException.class, () -> testService.persistWithConflictingDeclaration(newChild));
        testChildRepository.clear();
        assertEquals(2, testChildRepository.findAll().size());
    }

    @Test
    @DisplayName("persist rolls back on included checked exception")
    void rollbackOnDeclaredCheckedException() {

        testService.findParentAndChild(parentId);
        TestChild newChild = new TestChild();
        assertThrows(RollbackException.class, () -> testService.persistWithDeclaredCheckedException(newChild));
        testChildRepository.clear();
        assertEquals(1, testChildRepository.findAll().size());
    }

    @Test
    @DisplayName("persist rolls back on nested included checked exception")
    void nestedRollbackOnDeclaredCheckedException() {

        testService.findParentAndChild(parentId);
        TestChild newChild = new TestChild();
        assertThrows(RollbackException.class, () -> testService.persistWithNestedDeclaredCheckedException(newChild));
        testChildRepository.clear();
        assertEquals(1, testChildRepository.findAll().size());
    }

    @Test
    @DisplayName("persist does not roll back on excluded runtime exception")
    void noRollbackOnDeclaredRuntimeException() {

        testService.findParentAndChild(parentId);
        TestChild newChild = new TestChild();
        assertThrows(NoRollbackException.class, () -> testService.persistWithDeclaredRuntimeException(newChild));
        testChildRepository.clear();
        assertEquals(2, testChildRepository.findAll().size());
    }

    @Test
    @DisplayName("persist does not roll back on nested excluded runtime exception")
    void noRollbackOnNestedDeclaredRuntimeException() {

        testService.findParentAndChild(parentId);
        TestChild newChild = new TestChild();
        assertThrows(NoRollbackException.class, () -> testService.persistWithNestedDeclaredRuntimeException(newChild));
        testChildRepository.clear();
        assertEquals(2, testChildRepository.findAll().size());
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

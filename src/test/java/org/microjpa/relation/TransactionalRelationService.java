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

import java.io.IOException;

import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;

import org.microjpa.child.TestChild;
import org.microjpa.child.TransactionalChildRepository;
import org.microjpa.parent.TransactionalParentRepository;
import org.microjpa.relation.TransactionalRelationService.NoRollbackException;
import org.microjpa.relation.TransactionalRelationService.RollbackException;

@Transactional(rollbackOn = RollbackException.class, dontRollbackOn = NoRollbackException.class)
@ApplicationScoped
public class TransactionalRelationService
    extends AbstractRelationService<TransactionalParentRepository, TransactionalChildRepository> {

    @Transactional
    public void persistWithRuntimeException(TestChild testChild) {
        childRepository.persist(testChild);
        childRepository.flush();
        throw new IllegalStateException("persist failed");
    }

    @Transactional
    public void persistWithNestedRuntimeException(TestChild testChild) {
        childRepository.persistWithRuntimeException(testChild);
    }

    public void persistWithCheckedException(TestChild testChild) throws IOException {
        childRepository.persist(testChild);
        childRepository.flush();
        throw new IOException("persist successfull");
    }

    public void persistWithNestedCheckedException(TestChild testChild) throws IOException {
        childRepository.persistWithCheckedException(testChild);
    }

    @Transactional(rollbackOn = NoRollbackException.class, dontRollbackOn = NoRollbackException.class)
    public void persistWithConflictingDeclaration(TestChild testChild) throws IOException {
        childRepository.persist(testChild);
        childRepository.flush();
        throw new NoRollbackException();
    }

    public void persistWithDeclaredRuntimeException(TestChild testChild) {
        childRepository.persist(testChild);
        childRepository.flush();
        throw new NoRollbackException();
    }

    public void persistWithNestedDeclaredRuntimeException(TestChild testChild) throws IOException {
        childRepository.persistWithDeclaredRuntimeException(testChild);
    }

    public void persistWithDeclaredCheckedException(TestChild testChild) throws RollbackException {
        childRepository.persist(testChild);
        childRepository.flush();
        throw new RollbackException();
    }

    public void persistWithNestedDeclaredCheckedException(TestChild testChild) throws RollbackException {
        childRepository.persistWithDeclaredCheckedException(testChild);
    }

    public void persistWithNoRollbackApplicationException(TestChild testChild) {
        childRepository.persistWithNoRollbackApplicationException(testChild);
    }

    public void persistWithRollbackApplicationException(TestChild testChild) {
        childRepository.persistWithRollbackApplicationException(testChild);
    }

    public void persistWithNoRollbackApplicationExceptionSubclass(TestChild testChild) {
        childRepository.persistWithNoRollbackApplicationExceptionSubclass(testChild);
    }

    public void persistWithRollbackApplicationExceptionSubclass(TestChild testChild) {
        childRepository.persistWithRollbackApplicationExceptionSubclass(testChild);
    }

    public void persistWithInheritingNoRollbackApplicationException(TestChild testChild) {
        childRepository.persistWithInheritingNoRollbackApplicationException(testChild);
    }

    public void persistWithInheritingRollbackApplicationException(TestChild testChild) {
        childRepository.persistWithInheritingRollbackApplicationException(testChild);
    }

    public void persistWithInheritingNoRollbackApplicationExceptionSubclass(TestChild testChild) {
        childRepository.persistWithInheritingNoRollbackApplicationExceptionSubclass(testChild);
    }

    public void persistWithInheritingRollbackApplicationExceptionSubclass(TestChild testChild) {
        childRepository.persistWithInheritingRollbackApplicationExceptionSubclass(testChild);
    }

    public static class NoRollbackException extends RuntimeException {
    }

    public static class RollbackException extends Exception {
    }
}

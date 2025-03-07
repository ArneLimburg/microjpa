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

import java.io.IOException;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import org.microjpa.exception.InheritingNoRollbackApplicationException;
import org.microjpa.exception.InheritingNoRollbackApplicationExceptionSubclass;
import org.microjpa.exception.InheritingRollbackApplicationException;
import org.microjpa.exception.InheritingRollbackApplicationExceptionSubclass;
import org.microjpa.exception.NoRollbackApplicationException;
import org.microjpa.exception.NoRollbackApplicationExceptionSubclass;
import org.microjpa.exception.RollbackApplicationException;
import org.microjpa.exception.RollbackApplicationExceptionSubclass;
import org.microjpa.relation.TransactionalRelationService.NoRollbackException;
import org.microjpa.relation.TransactionalRelationService.RollbackException;

@Transactional
@ApplicationScoped
public class TransactionalChildRepository extends AbstractChildRepository {

    @PersistenceContext(unitName = "test-unit")
    private EntityManager entityManager;

    @Override
    protected EntityManager getEntityManager() {
        return entityManager;
    }

    public void flush() {
        entityManager.flush();
    }

    public void persistWithRuntimeException(TestChild testChild) {
        entityManager.persist(testChild);
        entityManager.flush();
        throw new IllegalStateException("persit failed");
    }

    public void persistWithCheckedException(TestChild testChild) throws IOException {
        entityManager.persist(testChild);
        entityManager.flush();
        throw new IOException("persit successfull");
    }

    @Transactional(dontRollbackOn = NoRollbackException.class)
    public void persistWithDeclaredRuntimeException(TestChild testChild) {
        entityManager.persist(testChild);
        entityManager.flush();
        throw new NoRollbackException();
    }

    public void persistWithDeclaredCheckedException(TestChild testChild) throws RollbackException {
        entityManager.persist(testChild);
        entityManager.flush();
        throw new RollbackException();
    }

    public void persistWithNoRollbackApplicationException(TestChild testChild) {
        entityManager.persist(testChild);
        entityManager.flush();
        throw new NoRollbackApplicationException();
    }

    public void persistWithRollbackApplicationException(TestChild testChild) {
        entityManager.persist(testChild);
        entityManager.flush();
        throw new RollbackApplicationException();
    }

    public void persistWithNoRollbackApplicationExceptionSubclass(TestChild testChild) {
        entityManager.persist(testChild);
        entityManager.flush();
        throw new NoRollbackApplicationExceptionSubclass();
    }

    public void persistWithRollbackApplicationExceptionSubclass(TestChild testChild) {
        entityManager.persist(testChild);
        entityManager.flush();
        throw new RollbackApplicationExceptionSubclass();
    }

    public void persistWithInheritingNoRollbackApplicationException(TestChild testChild) {
        entityManager.persist(testChild);
        entityManager.flush();
        throw new InheritingNoRollbackApplicationException();
    }

    public void persistWithInheritingRollbackApplicationException(TestChild testChild) {
        entityManager.persist(testChild);
        entityManager.flush();
        throw new InheritingRollbackApplicationException();
    }

    public void persistWithInheritingNoRollbackApplicationExceptionSubclass(TestChild testChild) {
        entityManager.persist(testChild);
        entityManager.flush();
        throw new InheritingNoRollbackApplicationExceptionSubclass();
    }

    public void persistWithInheritingRollbackApplicationExceptionSubclass(TestChild testChild) {
        entityManager.persist(testChild);
        entityManager.flush();
        throw new InheritingRollbackApplicationExceptionSubclass();
    }
}

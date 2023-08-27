/*
 * Copyright 2020 - 2024 Arne Limburg
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

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnit;
import jakarta.transaction.Transactional;

@Transactional
@ApplicationScoped
public class ManualChildRepository extends AbstractChildRepository {

    @PersistenceUnit(unitName = "test-unit")
    private EntityManagerFactory entityManagerFactory;

    public void persist(TestChild testChild) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        entityManager.persist(testChild);
        entityManager.getTransaction().commit();
        entityManager.close();
    }

    public TestChild findByParentId(long parentId) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        TestChild foundChild = entityManager.createNamedQuery(TestChild.FIND_BY_PARENT_ID, TestChild.class)
            .setParameter("parentId", parentId)
            .getSingleResult();
        entityManager.close();
        return foundChild;
    }

    @Override
    protected EntityManager getEntityManager() {
        return entityManagerFactory.createEntityManager();
    }
}

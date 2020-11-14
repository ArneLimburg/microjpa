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

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaQuery;
import javax.transaction.Transactional;

public abstract class AbstractChildRepository {

    @Transactional
    public void persist(TestChild testChild) {
        getEntityManager().persist(testChild);
    }

    public List<TestChild> findAll() {
        CriteriaQuery<TestChild> q = getEntityManager().getCriteriaBuilder().createQuery(TestChild.class);
        return getEntityManager().createQuery(q.select(q.from(TestChild.class))).getResultList();
    }

    public TestChild findByParentId(long parentId) {
        return getEntityManager().createNamedQuery(TestChild.FIND_BY_PARENT_ID, TestChild.class)
                .setParameter("parentId", parentId)
                .getSingleResult();
    }

    public void clear() {
        getEntityManager().clear();
    }

    protected abstract EntityManager getEntityManager();
}

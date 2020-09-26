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
package rocks.limburg.microjpa;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@ApplicationScoped
public class TransactionalProductionRelationService {

    @Inject
    private TransactionalProductionParentRepository parentRepository;
    @Inject
    private TransactionalProductionChildRepository childRepository;
    @PersistenceContext(unitName = "production-unit")
    private EntityManager entityManager;

    public void persist(TestChild testChild) {
        childRepository.persist(testChild);
    }

    public Relation findParentAndChild(long parentId) {
        TestParent parent = parentRepository.find(parentId);
        TestChild child = childRepository.findByParentId(parentId);
        return new Relation(parent, child);
    }
}

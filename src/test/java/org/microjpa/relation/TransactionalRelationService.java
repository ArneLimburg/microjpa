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
import javax.transaction.Transactional;

import org.microjpa.child.TestChild;
import org.microjpa.child.TransactionalChildRepository;
import org.microjpa.parent.TransactionalParentRepository;

@Transactional
@ApplicationScoped
public class TransactionalRelationService
    extends AbstractRelationService<TransactionalParentRepository, TransactionalChildRepository> {

    public void persistWithException(TestChild testChild) {
        childRepository.persist(testChild);
        childRepository.flush();
        throw new IllegalStateException("persist failed");
    }

    public void persistWithNestedException(TestChild testChild) {
        childRepository.persistWithException(testChild);
    }
}

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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.microjpa.test.CdiTest.ContextScope.PER_TEST_CLASS;

import org.junit.jupiter.api.Test;
import org.microjpa.child.ExtendedChildRepository;
import org.microjpa.child.TestChild;
import org.microjpa.parent.ExtendedParentRepository;
import org.microjpa.parent.TestParent;
import org.microjpa.relation.ExtendedRelationService;
import org.microjpa.relation.Relation;
import org.microjpa.test.CdiTest;
import org.microjpa.test.MicroJpaTest;

@CdiTest(cdiContext = PER_TEST_CLASS)
@MicroJpaTest
class ExtendedPersistenceUnitTest
    extends AbstractPersistenceUnitTest<ExtendedRelationService, ExtendedParentRepository, ExtendedChildRepository> {

    @Test
    public void lazyLoadingAfterTransaction() {
        Relation relation = testService.findParentAndChild(parentId);
        testService.persist(new TestChild(new TestParent())); // any call in transaction

        assertTrue(relation.getParent().getChildren().contains(relation.getChild()));
    }
}

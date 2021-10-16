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
import static org.junit.jupiter.api.Assertions.assertNotSame;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.microjpa.child.ManualChildRepository;
import org.microjpa.parent.ManualParentRepository;
import org.microjpa.relation.ManualRelationService;
import org.microjpa.relation.Relation;

public class ManualPersistenceUnitTest
    extends AbstractPersistenceUnitTest<ManualRelationService, ManualParentRepository, ManualChildRepository> {

    @Test
    @DisplayName("found parent's id equals parent's id of found child but they are not same (different EntityManagers are used)")
    public void find() {

        Relation parentAndChild = testService.findParentAndChild(parentId);

        assertNotSame(parentAndChild.getChild().getParent(), parentAndChild.getParent());
        assertEquals(parentAndChild.getChild().getParent().getId(), parentAndChild.getParent().getId());
    }
}

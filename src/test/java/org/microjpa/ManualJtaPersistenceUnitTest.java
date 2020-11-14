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

import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceProperty;
import javax.persistence.PersistenceUnit;
import javax.persistence.PersistenceUnits;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.microjpa.child.ManualJtaChildRepository;
import org.microjpa.parent.ManualJtaParentRepository;
import org.microjpa.relation.ManualJtaRelationService;
import org.microjpa.relation.Relation;

@PersistenceUnit(unitName = "jta-unit")
@PersistenceUnits(@PersistenceUnit(unitName = "jta-unit"))
public class ManualJtaPersistenceUnitTest
    extends AbstractPersistenceUnitTest<ManualJtaRelationService, ManualJtaParentRepository, ManualJtaChildRepository> {

    @Test
    @DisplayName("found parent's id equals parent's id of found child but they are not same (different EntityManagers are used)")
    @PersistenceContext(unitName = "jta-unit", properties = {
            @PersistenceProperty(name = "javax.persistence.transactionType", value = "RESOURCE_LOCAL"),
            @PersistenceProperty(name = "javax.persistence.jtaDataSource", value = ""),
            @PersistenceProperty(name = "javax.persistence.jdbc.driver", value = "org.h2.Driver"),
            @PersistenceProperty(name = "javax.persistence.jdbc.url", value = "jdbc:h2:mem:test"),
            @PersistenceProperty(name = "javax.persistence.jdbc.user", value = "sa"),
            @PersistenceProperty(name = "javax.persistence.jdbc.password", value = ""),
            @PersistenceProperty(name = "javax.persistence.schema-generation.database.action", value = "drop-and-create")
    })
    public void find() {

        Relation parentAndChild = testService.findParentAndChild(parentId);

        assertNotSame(parentAndChild.getChild().getParent(), parentAndChild.getParent());
        assertEquals(parentAndChild.getChild().getParent().getId(), parentAndChild.getParent().getId());
    }
}

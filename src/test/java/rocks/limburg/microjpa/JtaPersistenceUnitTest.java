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

import static org.junit.jupiter.api.Assertions.assertSame;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.se.SeContainer;
import javax.enterprise.inject.se.SeContainerInitializer;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceProperty;

import org.apache.deltaspike.cdise.api.ContextControl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@PersistenceContext(unitName = "jta-unit", properties
    = {
            @PersistenceProperty(name = "javax.persistence.transactionType", value = "RESOURCE_LOCAL"),
            @PersistenceProperty(name = "javax.persistence.jtaDataSource", value = ""),
            @PersistenceProperty(name = "javax.persistence.jdbc.driver", value = "org.h2.Driver"),
            @PersistenceProperty(name = "javax.persistence.jdbc.url", value = "jdbc:h2:mem:test"),
            @PersistenceProperty(name = "javax.persistence.jdbc.user", value = "sa"),
            @PersistenceProperty(name = "javax.persistence.jdbc.password", value = ""),
            @PersistenceProperty(name = "javax.persistence.schema-generation.database.action", value = "drop-and-create")
    })
public class JtaPersistenceUnitTest {

    private SeContainer cdiContainer;
    private ContextControl contextControl;

    private TransactionalJtaRelationService productionService;
    private long parentId;

    @BeforeEach
    public void startCdi() {
        cdiContainer = SeContainerInitializer.newInstance().initialize();
        contextControl = cdiContainer.select(ContextControl.class).get();

        contextControl.startContext(RequestScoped.class);
        productionService = cdiContainer.select(TransactionalJtaRelationService.class).get();
        TestChild testChild = new TestChild(new TestParent());
        productionService.persist(testChild);
        parentId = testChild.getParent().getId();
    }

    @AfterEach
    public void shutDownCdi() {
        contextControl.stopContext(RequestScoped.class);
        cdiContainer.close();
    }

    @Test
    @DisplayName("found parent equals parent of found child (same EntityManager is used)")
    public void find() {

        Relation parentAndChild = productionService.findParentAndChild(parentId);

        assertSame(parentAndChild.getChild().getParent(), parentAndChild.getParent());
    }
}

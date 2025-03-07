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
import static org.junit.jupiter.api.Assertions.assertSame;

import java.lang.reflect.ParameterizedType;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;
import javax.persistence.PersistenceUnit;

import org.apache.deltaspike.cdise.api.ContextControl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.microjpa.child.AbstractChildRepository;
import org.microjpa.child.TestChild;
import org.microjpa.parent.AbstractParentRepository;
import org.microjpa.parent.TestParent;
import org.microjpa.relation.AbstractRelationService;
import org.microjpa.relation.Relation;

@PersistenceUnit(unitName = "unknown")
abstract class AbstractPersistenceUnitTest
    <S extends AbstractRelationService<P, C>, P extends AbstractParentRepository, C extends AbstractChildRepository> {

    @Inject
    private ContextControl contextControl;

    protected S testService;
    protected C testChildRepository;
    protected long parentId;

    @BeforeEach
    public void startCdi() {
        contextControl.startContext(RequestScoped.class);

        ParameterizedType genericSuperclass = (ParameterizedType)getClass().getGenericSuperclass();
        testService = CDI.current().select((Class<S>)genericSuperclass.getActualTypeArguments()[0]).get();
        testChildRepository = CDI.current().select((Class<C>)genericSuperclass.getActualTypeArguments()[2]).get();
        TestChild testChild = new TestChild(new TestParent());
        testService.persist(testChild);
        parentId = testChild.getParent().getId();
        contextControl.stopContext(RequestScoped.class);
    }

    @Test
    @DisplayName("found parent equals parent of found child (same EntityManager is used)")
    void find() {

        Relation parentAndChild = testService.findParentAndChild(parentId);

        assertSame(parentAndChild.getChild().getParent(), parentAndChild.getParent());
    }

    @Test
    @DisplayName("persist joins to transaction after find")
    void persist() {

        testService.findParentAndChild(parentId);
        testService.persist(new TestChild());
        testChildRepository.clear();
        assertEquals(2, testChildRepository.findAll().size());
    }
}

/*
 * Copyright 2021 Arne Limburg
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
package org.microjpa.test;

import static javax.persistence.PersistenceContextType.EXTENDED;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.microjpa.test.MicroJpaTest.ExtendedPersistenceContextScope.PER_TEST_CLASS;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.jupiter.api.Test;
import org.microjpa.parent.TestParent;
import org.microjpa.test.CdiTest.ContextScope;

@CdiTest(cdiContext = ContextScope.PER_TEST_CLASS)
@MicroJpaTest(extendedPersistenceContext = PER_TEST_CLASS)
class PerTestClassTest {

    private static TestParent parent;

    @PersistenceContext(unitName = "test-unit", type = EXTENDED)
    private EntityManager entityManager;
    private EntityManager nonAnnotatedEntityManager;

    @Test
    public void nonAnnotatedEntityManagerIsNotInjected() {
        assertNull(nonAnnotatedEntityManager);
    }

    @Test
    public void createParentOne() {
        if (parent != null) {
            assertTrue(entityManager.contains(parent));
        }
        parent = new TestParent();
        entityManager.persist(parent);
    }

    @Test
    public void createParentTwo() {
        if (parent != null) {
            assertTrue(entityManager.contains(parent));
        }
        parent = new TestParent();
        entityManager.persist(parent);
    }
}

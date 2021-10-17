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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.microjpa.test.CdiTest.ContextScope.PER_TEST_CLASS;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microjpa.parent.TestParent;

@CdiTest(cdiContext = PER_TEST_CLASS)
@ExtendWith(MicroJpa.class) // default is PER_TEST_EXECUTION
public class PerTestExecutionTest {

    private static TestParent parent;

    @PersistenceContext(unitName = "test-unit", type = EXTENDED)
    private EntityManager entityManager;

    @RepeatedTest(2)
    public void createParent() {
        if (parent != null) {
            assertFalse(entityManager.contains(parent));
        }
        parent = new TestParent();
        entityManager.persist(parent);
    }
}

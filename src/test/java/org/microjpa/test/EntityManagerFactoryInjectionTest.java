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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.microjpa.test.CdiTest.ContextScope.PER_TEST_CLASS;
import static org.microjpa.test.MicroJpaTest.ExtendedPersistenceContextScope.PER_TEST_METHOD;

import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;

import org.junit.jupiter.api.Test;

@CdiTest(cdiContext = PER_TEST_CLASS)
@MicroJpaTest(extendedPersistenceContext = PER_TEST_METHOD)
class EntityManagerFactoryInjectionTest {

    @PersistenceUnit(unitName = "test-unit")
    private EntityManagerFactory annotatedFactory;
    private EntityManagerFactory nonAnnotatedFactory;

    @Test
    public void entityManagerInjected() {
        assertNotNull(annotatedFactory);
        assertNull(nonAnnotatedFactory);
    }
}

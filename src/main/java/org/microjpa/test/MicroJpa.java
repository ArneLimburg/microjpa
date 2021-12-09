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

import static java.util.Optional.ofNullable;
import static org.junit.platform.commons.util.AnnotationUtils.findAnnotation;

import java.lang.reflect.Field;

import javax.enterprise.inject.spi.CDI;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnit;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;
import org.microjpa.ExtendedPersistenceContext;
import org.microjpa.MicroJpaExtension.PersistenceContextLiteral;
import org.microjpa.MicroJpaExtension.PersistenceUnitLiteral;
import org.microjpa.test.MicroJpaTest.ExtendedPersistenceContextScope;

public class MicroJpa implements
    TestInstancePostProcessor,
    BeforeAllCallback,
    AfterAllCallback,
    BeforeEachCallback,
    AfterEachCallback,
    BeforeTestExecutionCallback,
    AfterTestExecutionCallback {

    private static final Namespace NAMESPACE = Namespace.create(MicroJpa.class.getName());
    private static final Object FIRST_EXECUTION_KEY = new Object();

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        getContext().activate();
    }

    @Override
    public void postProcessTestInstance(Object testInstance, ExtensionContext context) throws Exception {
        injectEntityManagerAndFactory(testInstance, testInstance.getClass());
        if (getScope(context) != ExtendedPersistenceContextScope.PER_TEST_CLASS) {
            getContext().deactivate();
            getContext().activate();
        }
    }

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        context.getStore(NAMESPACE).put(FIRST_EXECUTION_KEY, true);
    }

    @Override
    public void beforeTestExecution(ExtensionContext context) throws Exception {
        Store store = context.getStore(NAMESPACE);
        if (getScope(context) != ExtendedPersistenceContextScope.PER_TEST_CLASS && store.get(FIRST_EXECUTION_KEY, Boolean.class)) {
            getContext().deactivate();
            getContext().activate();
            store.put(FIRST_EXECUTION_KEY, false);
        }
    }

    @Override
    public void afterTestExecution(ExtensionContext context) throws Exception {
        if (getScope(context) == ExtendedPersistenceContextScope.PER_TEST_EXECUTION) {
            getContext().deactivate();
            getContext().activate();
        }
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        if (getScope(context) == ExtendedPersistenceContextScope.PER_TEST_METHOD) {
            getContext().deactivate();
        }
    }

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        if (getScope(context) == ExtendedPersistenceContextScope.PER_TEST_CLASS) {
            getContext().deactivate();
        }
    }

    private void injectEntityManagerAndFactory(Object testInstance, Class<? extends Object> type) throws ReflectiveOperationException {
        if (type == Object.class) {
            return;
        }
        injectEntityManagerAndFactory(testInstance, type.getSuperclass());
        for (Field field: type.getDeclaredFields()) {
            if (EntityManager.class == field.getType()) {
                PersistenceContext context = field.getAnnotation(PersistenceContext.class);
                if (context != null) {
                    field.setAccessible(true);
                    field.set(testInstance, CDI.current().select(EntityManager.class, new PersistenceContextLiteral(context)).get());
                }
            } else if (EntityManagerFactory.class == field.getType()) {
                PersistenceUnit unit = field.getAnnotation(PersistenceUnit.class);
                if (unit != null) {
                    field.setAccessible(true);
                    field.set(testInstance, CDI.current().select(EntityManagerFactory.class, new PersistenceUnitLiteral(unit)).get());
                }
            }
        }
    }

    private ExtendedPersistenceContextScope getScope(ExtensionContext context) {
        return ofNullable(
                findAnnotation(context.getTestMethod(), MicroJpaTest.class)
                .orElse(findAnnotation(context.getTestClass(), MicroJpaTest.class)
                .orElse(null))).map(MicroJpaTest::extendedPersistenceContext)
        .orElse(ExtendedPersistenceContextScope.PER_TEST_EXECUTION);
    }

    private ExtendedPersistenceContext getContext() {
        return CDI.current().select(ExtendedPersistenceContext.class).get();
    }
}

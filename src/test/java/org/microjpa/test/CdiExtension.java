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
package org.microjpa.test;

import static java.util.Optional.ofNullable;
import static org.junit.platform.commons.util.AnnotationUtils.findAnnotation;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.se.SeContainer;
import javax.enterprise.inject.se.SeContainerInitializer;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionTarget;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;
import org.microjpa.test.CdiTest.ContextScope;

public class CdiExtension implements BeforeAllCallback, TestInstancePostProcessor, AfterEachCallback, AfterAllCallback {

    private SeContainer cdiContainer;
    private InjectionTarget<Object> injectionTarget;

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        ContextScope scope = getScope(context);
        if (scope == ContextScope.PER_TEST_CLASS) {
            cdiContainer = SeContainerInitializer.newInstance().initialize();
        }
    }

    @Override
    public void postProcessTestInstance(Object testInstance, ExtensionContext context) throws Exception {
        ContextScope scope = getScope(context);
        if (scope == ContextScope.PER_TEST_METHOD) {
            cdiContainer = SeContainerInitializer.newInstance().initialize();
        }
        BeanManager beanManager = cdiContainer.getBeanManager();
        AnnotatedType<Object> annotatedType = beanManager.createAnnotatedType((Class<Object>)testInstance.getClass());
        injectionTarget = beanManager.createInjectionTarget(annotatedType);
        CreationalContext<Object> creationalContext = beanManager.createCreationalContext(null);
        injectionTarget.inject(testInstance, creationalContext);
        injectionTarget.postConstruct(testInstance);
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        injectionTarget.preDestroy(context.getRequiredTestInstance());
        injectionTarget.dispose(context.getRequiredTestInstance());
        ContextScope scope = getScope(context);
        if (scope == ContextScope.PER_TEST_METHOD) {
            cdiContainer.close();
        }
    }

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        ContextScope scope = getScope(context);
        if (scope == ContextScope.PER_TEST_CLASS) {
            cdiContainer.close();
        }
    }

    private ContextScope getScope(ExtensionContext context) {
        return ofNullable(
                findAnnotation(context.getTestMethod(), CdiTest.class)
                .orElse(findAnnotation(context.getTestClass(), CdiTest.class)
                .orElse(null))).map(CdiTest::cdiContext)
        .orElse(ContextScope.PER_TEST_METHOD);
    }
}

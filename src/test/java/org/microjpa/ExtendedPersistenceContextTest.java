/*
 * Copyright 2020 - 2021 Arne Limburg
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.io.Serializable;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.se.SeContainer;
import javax.enterprise.inject.se.SeContainerInitializer;
import javax.inject.Inject;
import javax.transaction.SystemException;
import javax.transaction.Transactional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ExtendedPersistenceContextTest {

    private SeContainer cdiContainer;

    @BeforeEach
    public void loadTransactionContext() {
        cdiContainer = SeContainerInitializer.newInstance().initialize();
    }

    @AfterEach
    public void closeCdi() {
        cdiContainer.close();
    }

    @Test
    @DisplayName("When activated automatically, transaction.commit() does not lead to deactivation.")
    public void transactionCommitDoesNotLeadToDeactivate() throws SystemException {
        MicroTransaction transaction = cdiContainer.select(MicroTransaction.class).get();
        assertFalse(transaction.isActive());
        PersistenceScopedTestBean bean = cdiContainer.select(PersistenceScopedTestBean.class).get();
        UUID beanId = bean.getId();
        transaction.begin();
        transaction.commit();
        assertEquals(beanId, bean.getId());
    }

    @Test
    @DisplayName("The second manual activation is ignored")
    public void secondManualActivationIsIgnored() throws SystemException {
        ExtendedPersistenceContext context = cdiContainer.select(ExtendedPersistenceContext.class).get();
        context.activate();
        PersistenceScopedTestBean bean = cdiContainer.select(PersistenceScopedTestBean.class).get();
        UUID beanId = bean.getId();
        context.activate();
        assertEquals(beanId, bean.getId());
    }

    @Test
    @DisplayName("Transaction deactivates context")
    public void transactionActivatesDeactivatesContext() throws SystemException {
        PersistenceScopedTestBeanContainer beanContainer = cdiContainer.select(PersistenceScopedTestBeanContainer.class).get();

        UUID beanId = beanContainer.getBeanId();

        PersistenceScopedTestBean bean = cdiContainer.select(PersistenceScopedTestBean.class).get();
        assertNotEquals(beanId, bean.getId());
    }

    @ApplicationScoped
    public static class PersistenceScopedTestBeanContainer implements Serializable {

        @Inject
        private PersistenceScopedTestBean bean;

        @Transactional
        public UUID getBeanId() {
            return bean.getId();
        }
    }

    @PersistenceScoped
    public static class PersistenceScopedTestBean implements Serializable {

        private UUID uuid;

        @PostConstruct
        public void initUuid() {
            uuid = UUID.randomUUID();
        }

        public UUID getId() {
            return uuid;
        }
    }
}

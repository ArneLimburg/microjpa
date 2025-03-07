/*
 * Copyright 2020 - 2024 Arne Limburg
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.Serializable;
import java.util.Set;
import java.util.UUID;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.inject.se.SeContainer;
import jakarta.enterprise.inject.se.SeContainerInitializer;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.transaction.Status;
import jakarta.transaction.SystemException;
import jakarta.transaction.TransactionScoped;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TransactionContextTest {

    private static SeContainer cdiContainer;
    private static Bean<TransactionScopedTestBean> testBean;

    @BeforeAll
    public static void loadTransactionContext() {
        cdiContainer = SeContainerInitializer.newInstance().initialize();
        Set<Bean<?>> beans = cdiContainer.getBeanManager().getBeans(TransactionScopedTestBean.class);
        testBean = (Bean<TransactionScopedTestBean>)cdiContainer.getBeanManager().resolve(beans);
    }

    @AfterAll
    public static void closeCdi() {
        cdiContainer.close();
    }

    @AfterEach
    public void deactivateExtendedPersistenceContext() {
        cdiContainer.select(ExtendedPersistenceContext.class).get().deactivate();
    }

    @Test
    @DisplayName("Activating TransacionContext does not lead to active transaction")
    public void activationLeadsToActiveTransaction() throws SystemException {
        MicroTransaction transaction = cdiContainer.select(MicroTransaction.class).get();
        assertFalse(transaction.isActive());
        assertEquals(Status.STATUS_NO_TRANSACTION, transaction.getStatus());
        transaction.begin();
        assertTrue(transaction.isActive());
        assertEquals(Status.STATUS_ACTIVE, transaction.getStatus());
        transaction.commit();
        assertFalse(transaction.isActive());
        assertEquals(Status.STATUS_NO_TRANSACTION, transaction.getStatus());
    }

    @Test
    @DisplayName("Transaction timeout is not supported")
    public void transactionTimeoutIsNotSupported() {
        MicroTransaction transaction = cdiContainer.select(MicroTransaction.class).get();
        assertThrows(UnsupportedOperationException.class, () -> transaction.setTransactionTimeout(2));
    }

    @Test
    @DisplayName("Activating TransactionContext twice does not destroy created beans")
    public void activationTwiceDoesNotDestroyBeans() {
        MicroTransaction transaction = cdiContainer.select(MicroTransaction.class).get();
        transaction.begin();
        TransactionContext context = cdiContainer.select(TransactionContext.class).get();
        context.activate();
        TransactionScopedTestBean instance = context.get(testBean, cdiContainer.getBeanManager().createCreationalContext(testBean));
        context.activate();
        assertEquals(instance.getId(), context.get(testBean).getId());
        context.deactivate();
        transaction.commit();
    }

    @Test
    @DisplayName("Storing transactional resources")
    public void transactionResources() {
        MicroTransaction transaction = cdiContainer.select(MicroTransaction.class).get();
        transaction.begin();
        assertNull(transaction.getResource("key1"));
        transaction.putResource("key1", "value1");
        transaction.putResource("keyForNullValue", null);
        assertEquals("value1", transaction.getResource("key1"));
        assertNull(transaction.getResource("keyForNullValue"));
        transaction.commit();
    }

    @Test
    @DisplayName("Same key in same transaction and different keys for different transactions")
    public void transactionKey() {
        MicroTransaction transaction = cdiContainer.select(MicroTransaction.class).get();
        transaction.begin();
        Object transactionKey = transaction.getTransactionKey();

        assertEquals(transactionKey,  cdiContainer.select(MicroTransaction.class).get().getTransactionKey());
        transaction.commit();

        transaction.begin();
        assertNotEquals(transactionKey,  cdiContainer.select(MicroTransaction.class).get().getTransactionKey());
        transaction.commit();
    }

    @Test
    @DisplayName("TransactionContext#destroy removes instance from context")
    public void destroy() {
        MicroTransaction transaction = cdiContainer.select(MicroTransaction.class).get();
        transaction.begin();
        TransactionContext context = cdiContainer.select(TransactionContext.class).get();
        context.activate();
        context.get(testBean, cdiContainer.getBeanManager().createCreationalContext(testBean));

        context.destroy(testBean);

        assertNull(context.get(testBean));
        transaction.commit();
    }

    @Test
    @DisplayName("Calling TransactionContext#destroy twice does not lead to an error")
    public void destroyTwice() {
        MicroTransaction transaction = cdiContainer.select(MicroTransaction.class).get();
        transaction.begin();
        TransactionContext context = cdiContainer.select(TransactionContext.class).get();
        context.activate();
        context.get(testBean, cdiContainer.getBeanManager().createCreationalContext(testBean));

        context.destroy(testBean);
        context.destroy(testBean);

        assertNull(context.get(testBean));
        transaction.commit();
    }

    @TransactionScoped
    public static class TransactionScopedTestBean implements Serializable {

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

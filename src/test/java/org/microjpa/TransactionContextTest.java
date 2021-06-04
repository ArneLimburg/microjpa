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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.Serializable;
import java.util.Set;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.se.SeContainer;
import javax.enterprise.inject.se.SeContainerInitializer;
import javax.enterprise.inject.spi.Bean;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.TransactionScoped;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.microjpa.tags.UnitTest;

@UnitTest
public class TransactionContextTest {

    private static SeContainer cdiContainer;
    private static TransactionContext context;
    private static Bean<TransactionScopedTestBean> testBean;

    @BeforeAll
    public static void loadTransactionContext() {
        cdiContainer = SeContainerInitializer.newInstance().initialize();
        context = cdiContainer.select(TransactionContext.class).get();
        Set<Bean<?>> beans = cdiContainer.getBeanManager().getBeans(TransactionScopedTestBean.class);
        testBean = (Bean<TransactionScopedTestBean>)cdiContainer.getBeanManager().resolve(beans);
    }

    @AfterAll
    public static void closeCdi() {
        context.deactivate();
        cdiContainer.close();
    }

    @Test
    @DisplayName("Activating TransacionContext does not lead to active transaction")
    public void activationLeadsToActiveTransaction() throws SystemException {
        context.activate();
        MicroTransaction transaction = cdiContainer.select(MicroTransaction.class).get();
        assertFalse(transaction.isActive());
        assertEquals(Status.STATUS_NO_TRANSACTION, transaction.getStatus());
        transaction.begin();
        assertTrue(transaction.isActive());
        context.deactivate();
    }

    @Test
    @DisplayName("Transaction timeout is not supported")
    public void transactionTimeoutIsNotSupported() {
        context.activate();
        MicroTransaction transaction = cdiContainer.select(MicroTransaction.class).get();
        assertThrows(UnsupportedOperationException.class, () -> transaction.setTransactionTimeout(2));
        context.deactivate();
    }

    @Test
    @DisplayName("Activating TransactionContext twice does not destroy created beans")
    public void activationTwiceDoesNotDestroyBeans() {
        context.activate();
        TransactionScopedTestBean instance = context.get(testBean, cdiContainer.getBeanManager().createCreationalContext(testBean));
        context.activate();
        assertEquals(instance.getId(), context.get(testBean).getId());
        context.deactivate();
    }

    @Test
    @DisplayName("Storing transactional resources")
    public void transactionResources() {
        context.activate();
        MicroTransaction transaction = cdiContainer.select(MicroTransaction.class).get();
        assertNull(transaction.getResource("key1"));
        transaction.putResource("key1", "value1");
        transaction.putResource("keyForNullValue", null);
        assertEquals("value1", transaction.getResource("key1"));
        assertNull(transaction.getResource("keyForNullValue"));
        context.deactivate();
    }

    @Test
    @DisplayName("Same key in same transaction and different keys for different transactions")
    public void transactionKey() {
        context.activate();
        MicroTransaction transaction1 = cdiContainer.select(MicroTransaction.class).get();
        Object transactionKey = transaction1.getTransactionKey();

        assertEquals(transactionKey,  cdiContainer.select(MicroTransaction.class).get().getTransactionKey());
        context.deactivate();

        context.activate();
        assertNotEquals(transactionKey,  cdiContainer.select(MicroTransaction.class).get().getTransactionKey());
        context.deactivate();
    }

    @Test
    @DisplayName("TransactionContext#destroy removes instance from context")
    public void destroy() {
        context.activate();
        context.get(testBean, cdiContainer.getBeanManager().createCreationalContext(testBean));

        context.destroy(testBean);

        assertNull(context.get(testBean));
    }

    @Test
    @DisplayName("Calling TransactionContext#destroy twice does not lead to an error")
    public void destroyTwice() {
        context.activate();
        context.get(testBean, cdiContainer.getBeanManager().createCreationalContext(testBean));

        context.destroy(testBean);
        context.destroy(testBean);

        assertNull(context.get(testBean));
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

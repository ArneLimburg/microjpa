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
package org.microjpa;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.enterprise.context.ContextNotActiveException;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.se.SeContainer;
import javax.enterprise.inject.se.SeContainerInitializer;

import org.apache.deltaspike.cdise.api.ContextControl;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.microjpa.beans.OuterBean;
import org.microjpa.tags.UnitTest;

@UnitTest
public class TransactionTypeTest {

    private static SeContainer cdiContainer;
    private static OuterBean outerBean;

    @BeforeAll
    public static void loadContext() {
        cdiContainer = SeContainerInitializer.newInstance().initialize();
        outerBean = cdiContainer.select(OuterBean.class).get();
    }

    @AfterAll
    public static void closeContext() {
        cdiContainer.close();
    }

    @Test
    @DisplayName("all transaction types with active request context")
    // This test basically tests the following table: https://docs.oracle.com/javaee/6/tutorial/doc/bncij.html
    public void activeRequestContext() {
        ContextControl contextControl = cdiContainer.select(ContextControl.class).get();
        contextControl.startContext(RequestScoped.class);

        assertTrue(outerBean.isInnerTransactionActiveWithOuterTransactionAndRequiredInnerTransaction());
        assertTrue(outerBean.isInnerTransactionActiveWithoutOuterTransactionAndRequiredInnerTransaction());

        assertThrows(UnsupportedOperationException.class,
            () -> outerBean.isInnerTransactionActiveWithOuterTransactionAndRequiresNewInnerTransaction());
        assertTrue(outerBean.isInnerTransactionActiveWithoutOuterTransactionAndRequiresNewInnerTransaction());

        assertTrue(outerBean.isInnerTransactionActiveWithOuterTransactionAndMandatoryInnerTransaction());
        assertThrows(IllegalStateException.class,
            () -> outerBean.isInnerTransactionActiveWithoutOuterTransactionAndMandatoryInnerTransaction());

        assertTrue(outerBean.isInnerTransactionActiveWithOuterTransactionAndSupportsInnerTransaction());
        assertFalse(outerBean.isInnerTransactionActiveWithoutOuterTransactionAndSupportsInnerTransaction());

        assertThrows(UnsupportedOperationException.class,
            () -> outerBean.isInnerTransactionActiveWithOuterTransactionAndNotSupportedInnerTransaction());
        assertFalse(outerBean.isInnerTransactionActiveWithoutOuterTransactionAndNotSupportedInnerTransaction());

        assertThrows(IllegalStateException.class,
            () -> outerBean.isInnerTransactionActiveWithOuterTransactionAndNeverInnerTransaction());
        assertFalse(outerBean.isInnerTransactionActiveWithoutOuterTransactionAndNeverInnerTransaction());

        contextControl.stopContext(RequestScoped.class);
    }

    @Test
    @DisplayName("all transaction types with inactive request context")
    // This test basically tests the following table: https://docs.oracle.com/javaee/6/tutorial/doc/bncij.html
    public void inactiveRequestContext() {
        ContextControl contextControl = cdiContainer.select(ContextControl.class).get();
        contextControl.stopContext(RequestScoped.class);

        assertThrows(ContextNotActiveException.class,
            () -> outerBean.isInnerTransactionActiveWithOuterTransactionAndRequiredInnerTransaction());
        assertThrows(ContextNotActiveException.class,
            () -> outerBean.isInnerTransactionActiveWithoutOuterTransactionAndRequiredInnerTransaction());

        assertThrows(UnsupportedOperationException.class,
            () -> outerBean.isInnerTransactionActiveWithOuterTransactionAndRequiresNewInnerTransaction());
        assertThrows(ContextNotActiveException.class,
            () -> outerBean.isInnerTransactionActiveWithoutOuterTransactionAndRequiresNewInnerTransaction());

        assertThrows(ContextNotActiveException.class,
            () -> outerBean.isInnerTransactionActiveWithOuterTransactionAndMandatoryInnerTransaction());
        assertThrows(IllegalStateException.class,
            () -> outerBean.isInnerTransactionActiveWithoutOuterTransactionAndMandatoryInnerTransaction());

        assertThrows(ContextNotActiveException.class,
            () -> outerBean.isInnerTransactionActiveWithOuterTransactionAndSupportsInnerTransaction());
        assertThrows(ContextNotActiveException.class,
            () -> outerBean.isInnerTransactionActiveWithoutOuterTransactionAndSupportsInnerTransaction());

        assertThrows(UnsupportedOperationException.class,
            () -> outerBean.isInnerTransactionActiveWithOuterTransactionAndNotSupportedInnerTransaction());
        assertThrows(ContextNotActiveException.class,
            () -> outerBean.isInnerTransactionActiveWithoutOuterTransactionAndNotSupportedInnerTransaction());

        assertThrows(IllegalStateException.class,
            () -> outerBean.isInnerTransactionActiveWithOuterTransactionAndNeverInnerTransaction());
        assertThrows(ContextNotActiveException.class,
            () -> outerBean.isInnerTransactionActiveWithoutOuterTransactionAndNeverInnerTransaction());
    }
}

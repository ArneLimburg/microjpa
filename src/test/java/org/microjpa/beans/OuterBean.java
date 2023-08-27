/*
 * Copyright 2021 - 2024 Arne Limburg
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
package org.microjpa.beans;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class OuterBean {

    @Inject
    private InnerBean innerBean;

    @Transactional
    public boolean isInnerTransactionActiveWithOuterTransactionAndRequiredInnerTransaction() {
        return innerBean.isTransactionActiveWithRequiredTransaction();
    }

    public boolean isInnerTransactionActiveWithoutOuterTransactionAndRequiredInnerTransaction() {
        return innerBean.isTransactionActiveWithRequiredTransaction();
    }

    @Transactional
    public boolean isInnerTransactionActiveWithOuterTransactionAndRequiresNewInnerTransaction() {
        return innerBean.isTransactionActiveWithRequiresNewTransaction();
    }

    public boolean isInnerTransactionActiveWithoutOuterTransactionAndRequiresNewInnerTransaction() {
        return innerBean.isTransactionActiveWithRequiresNewTransaction();
    }

    @Transactional
    public boolean isInnerTransactionActiveWithOuterTransactionAndMandatoryInnerTransaction() {
        return innerBean.isTransactionActiveWithMandatoryTransaction();
    }

    public boolean isInnerTransactionActiveWithoutOuterTransactionAndMandatoryInnerTransaction() {
        return innerBean.isTransactionActiveWithMandatoryTransaction();
    }

    @Transactional
    public boolean isInnerTransactionActiveWithOuterTransactionAndSupportsInnerTransaction() {
        return innerBean.isTransactionActiveWithSupportsTransaction();
    }

    public boolean isInnerTransactionActiveWithoutOuterTransactionAndSupportsInnerTransaction() {
        return innerBean.isTransactionActiveWithSupportsTransaction();
    }

    @Transactional
    public boolean isInnerTransactionActiveWithOuterTransactionAndNotSupportedInnerTransaction() {
        return innerBean.isTransactionActiveWithNotSupportedTransaction();
    }

    public boolean isInnerTransactionActiveWithoutOuterTransactionAndNotSupportedInnerTransaction() {
        return innerBean.isTransactionActiveWithNotSupportedTransaction();
    }

    @Transactional
    public boolean isInnerTransactionActiveWithOuterTransactionAndNeverInnerTransaction() {
        return innerBean.isTransactionActiveWithNeverTransaction();
    }

    public boolean isInnerTransactionActiveWithoutOuterTransactionAndNeverInnerTransaction() {
        return innerBean.isTransactionActiveWithNeverTransaction();
    }
}

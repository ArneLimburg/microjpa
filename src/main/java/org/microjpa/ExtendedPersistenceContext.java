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

import java.lang.annotation.Annotation;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Destroyed;
import javax.enterprise.context.Initialized;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.transaction.TransactionScoped;

public class ExtendedPersistenceContext extends AbstractThreadLocalContext {

    private ThreadLocal<ActivationTrigger> activationTrigger = new ThreadLocal<ActivationTrigger>();

    @Override
    public Class<? extends Annotation> getScope() {
        return PersistenceScoped.class;
    }
    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    public void activate() {
        if (activationTrigger.get() == null) {
            activationTrigger.set(ActivationTrigger.MANUALLY);
            super.activate();
        }
    }

    @Override
    public void deactivate() {
        super.deactivate();
        activationTrigger.remove();
    }

    @Override
    public <T> T get(Contextual<T> contextual, CreationalContext<T> creationalContext) {
        if (activationTrigger.get() == ActivationTrigger.BY_MICRO_TRANSACTION && !isMicroTransaction(contextual)) {
            activationTrigger.set(ActivationTrigger.AUTOMATICALLY);
        } else if (activationTrigger.get() == null) {
            if (isMicroTransaction(contextual)) {
                activationTrigger.set(ActivationTrigger.BY_MICRO_TRANSACTION);
            } else {
                activationTrigger.set(ActivationTrigger.AUTOMATICALLY);
            }
            super.activate();
        }
        return super.get(contextual, creationalContext);
    }

    public void beginRequest(@Observes @Initialized(RequestScoped.class) Object event) {
        if (activationTrigger.get() == null) {
            activationTrigger.set(ActivationTrigger.BY_REQUEST);
            super.activate();
        }
    }

    public void beginTransaction(@Observes @Initialized(TransactionScoped.class) Object event) {
        if (activationTrigger.get() == null || activationTrigger.get() == ActivationTrigger.BY_MICRO_TRANSACTION) {
            activationTrigger.set(ActivationTrigger.BY_TRANSACTION);
            super.activate();
        }
    }

    public void endTransaction(@Observes @Destroyed(TransactionScoped.class) Object event) {
        if (activationTrigger.get() == ActivationTrigger.BY_TRANSACTION) {
            deactivate();
        }
    }

    public void endRequest(@Observes @Destroyed(RequestScoped.class) Object event) {
        if (activationTrigger.get() == ActivationTrigger.BY_REQUEST) {
            deactivate();
        }
    }

    public void endApplication(@Observes @Destroyed(ApplicationScoped.class) Object event) {
        deactivate();
    }

    private boolean isMicroTransaction(Contextual<?> contextual) {
        return contextual instanceof javax.enterprise.inject.spi.Bean
            && ((javax.enterprise.inject.spi.Bean<?>)contextual).getBeanClass().equals(MicroTransaction.class);
    }

    private enum ActivationTrigger {
      AUTOMATICALLY, MANUALLY, BY_REQUEST, BY_TRANSACTION, BY_MICRO_TRANSACTION
    }
}

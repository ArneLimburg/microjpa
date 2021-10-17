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

    private ThreadLocal<Boolean> requestActive = new ThreadLocal<Boolean>();
    private ThreadLocal<Boolean> transactionActive = new ThreadLocal<Boolean>();

    @Override
    public Class<? extends Annotation> getScope() {
        return PersistenceScoped.class;
    }

    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    public <T> T get(Contextual<T> contextual, CreationalContext<T> creationalContext) {
        if (!super.isActive()) {
            activate();
        }
        if (requestActive.get() == null) {
            requestActive.set(false);
        }
        try {
            return super.get(contextual, creationalContext);
        } finally {
            if (Boolean.FALSE.equals(requestActive.get())) {
                requestActive.remove();
            }
        }
    }

    public void beginRequest(@Observes @Initialized(RequestScoped.class) Object event) {
        if (requestActive.get() == null) {
            requestActive.set(true);
        }
    }

    public void beginTransaction(@Observes @Initialized(TransactionScoped.class) Object event) {
        transactionActive.set(true);
    }

    public void endTransaction(@Observes @Destroyed(TransactionScoped.class) Object event) {
        transactionActive.remove();
        if (!Boolean.TRUE.equals(requestActive.get())) {
            deactivate();
        }
    }

    public void endRequest(@Observes @Destroyed(RequestScoped.class) Object event) {
        if (Boolean.TRUE.equals(requestActive.get())) {
            deactivate();
            requestActive.remove();
        }
    }

    public void endApplication(@Observes @Destroyed(ApplicationScoped.class) Object event) {
        deactivate();
    }
}

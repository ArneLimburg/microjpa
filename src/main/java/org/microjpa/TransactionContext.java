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

import static java.util.Optional.ofNullable;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.enterprise.context.spi.AlterableContext;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import javax.transaction.TransactionScoped;

public class TransactionContext implements AlterableContext {

    private ThreadLocal<Map<Contextual, Bean>> beans = new ThreadLocal<>();

    @Override
    public Class<? extends Annotation> getScope() {
        return TransactionScoped.class;
    }

    public void activate() {
        if (!isActive()) {
            beans.set(new HashMap<Contextual, Bean>());
        }
    }

    public void deactivate() {
        destroyAll();
    }

    @Override
    public boolean isActive() {
        return beans.get() != null;
    }

    @Override
    public <T> T get(Contextual<T> contextual) {
        return get(contextual, null);
    }

    @Override
    public <T> T get(Contextual<T> contextual, CreationalContext<T> creationalContext) {
        Map<Contextual<T>, Bean<T>> activeBeans = (Map<Contextual<T>, Bean<T>>)(Map<?, ?>)beans.get();
        return ofNullable(activeBeans.computeIfAbsent(contextual, createBean(creationalContext)))
                .map(Bean::getInstance)
                .orElse(null);
    }

    @Override
    public void destroy(Contextual<?> contextual) {
        Map<Contextual, Bean> activeBeans = beans.get();
        Bean bean = activeBeans.remove(contextual);
        if (bean != null) {
            bean.destroy(contextual);
        }
    }

    private void destroyAll() {
        Map<Contextual, Bean> activeBeans = beans.get();
        beans.remove();
        activeBeans.forEach((c, b) -> b.destroy(c));
    }

    private <T> Function<Contextual<T>, Bean<T>> createBean(CreationalContext<T> context) {
        return contextual -> ofNullable(context).map(c -> new Bean<T>(contextual, c)).orElse(null);
    }

    static class Bean<T> {

        private T instance;
        private CreationalContext<T> creationalContext;

        Bean(Contextual<T> contextual, CreationalContext<T> context) {
            instance = contextual.create(context);
            creationalContext = context;
        }

        T getInstance() {
            return instance;
        }

        void destroy(Contextual<T> contextual) {
            contextual.destroy(instance, creationalContext);
        }
    }
}

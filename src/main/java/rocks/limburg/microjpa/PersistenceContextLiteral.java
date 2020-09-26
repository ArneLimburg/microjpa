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
package rocks.limburg.microjpa;

import java.lang.annotation.Annotation;

import javax.enterprise.util.AnnotationLiteral;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.persistence.PersistenceProperty;
import javax.persistence.SynchronizationType;

public class PersistenceContextLiteral extends AnnotationLiteral<PersistenceContext> implements PersistenceContext {

    private String name;
    private String unitName;
    private PersistenceContextType type;
    private SynchronizationType synchronization;
    private PersistenceProperty[] properties;

    PersistenceContextLiteral(PersistenceContext context, PersistenceProperty... overridingProperties) {
        name = context.name();
        unitName = context.unitName();
        type = context.type();
        synchronization = context.synchronization();
        // TODO warn, when overriding properties
        properties = new PersistenceProperty[context.properties().length + overridingProperties.length];
        System.arraycopy(context.properties(), 0, properties, 0, context.properties().length);
        System.arraycopy(overridingProperties, 0, properties, context.properties().length, overridingProperties.length);
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public PersistenceContextType type() {
        return type;
    }

    @Override
    public SynchronizationType synchronization() {
        return synchronization;
    }

    @Override
    public PersistenceProperty[] properties() {
        return properties;
    }

    @Override
    public String unitName() {
        return unitName;
    }

    public Class<? extends Annotation> annotationType() {
        return PersistenceContext.class;
    }
}

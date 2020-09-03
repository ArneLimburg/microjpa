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

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Qualifier;
import javax.persistence.PersistenceContextType;
import javax.persistence.SynchronizationType;

@Qualifier
@Target({}) // not intended to be used explicitly
@Retention(RUNTIME)
public @interface PersistenceContext {

    String unitName() default "";
    PersistenceContextType type() default PersistenceContextType.TRANSACTION;
    SynchronizationType synchronization() default SynchronizationType.SYNCHRONIZED;

    class Literal extends AnnotationLiteral<PersistenceContext> implements PersistenceContext {

        private String unitName;
        private PersistenceContextType type;
        private SynchronizationType synchronization;

        Literal(javax.persistence.PersistenceContext persistenceContext) {
            unitName = persistenceContext.unitName();
            type = persistenceContext.type();
            synchronization = persistenceContext.synchronization();
        }

        @Override
        public String unitName() {
            return unitName;
        }

        @Override
        public PersistenceContextType type() {
            return type;
        }

        @Override
        public SynchronizationType synchronization() {
            return synchronization;
        }
    }
}

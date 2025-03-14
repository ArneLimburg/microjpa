/*
 * Copyright 2025 Arne Limburg
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
package org.microjpa.relation;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Stereotype;
import jakarta.transaction.Transactional;

import org.microjpa.relation.TransactionalRelationService.NoRollbackException;
import org.microjpa.relation.TransactionalRelationService.RollbackException;

@Inherited
@Stereotype
@Transactional(rollbackOn = RollbackException.class, dontRollbackOn = NoRollbackException.class)
@ApplicationScoped
@Retention(RUNTIME)
public @interface TransactionalStereotype {
}

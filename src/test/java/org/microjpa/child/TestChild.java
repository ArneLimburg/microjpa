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
package org.microjpa.child;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQuery;

import org.microjpa.parent.TestParent;

@Entity
@NamedQuery(name = TestChild.FIND_BY_PARENT_ID, query = "SELECT c FROM TestChild c WHERE c.parent.id = :parentId")
public class TestChild {

    public static final String FIND_BY_PARENT_ID = "TestChild.findByParentId";
    @Id
    @GeneratedValue
    private long id;
    @ManyToOne(cascade = CascadeType.PERSIST)
    private TestParent parent;

    public TestChild() {
    }

    public TestChild(TestParent parent) {
        this.parent = parent;
    }

    public TestParent getParent() {
        return parent;
    }
}

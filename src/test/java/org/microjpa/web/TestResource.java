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
package org.microjpa.web;

import static jakarta.persistence.PersistenceContextType.EXTENDED;
import static org.junit.jupiter.api.Assertions.assertFalse;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

import org.microjpa.child.TestChild;
import org.microjpa.parent.TestParent;

@ApplicationScoped
@Path("/test-parent")
public class TestResource {

    @Inject
    private TransactionTemplate transactionTemplate;
    @PersistenceContext(unitName = "test-unit", type = EXTENDED)
    private EntityManager entityManager;
    private TestParent parent;

    @PostConstruct
    public void createTestParent() {
        parent = new TestParent();
        TestChild child = new TestChild(parent);
        transactionTemplate.runInTransaction(() -> entityManager.persist(child));
        entityManager.detach(parent);
        entityManager.detach(child);
        parent = entityManager.find(TestParent.class, parent.getId());
        assertFalse(entityManager.getEntityManagerFactory().getPersistenceUnitUtil().isLoaded(parent, "children"),
            "children should not be initialized");
    }

    @GET
    public Response initialize() {
        return Response.ok().build();
    }

    @POST
    @Path("/managed")
    public Response setManaged() {
        entityManager.detach(parent);
        parent = entityManager.find(TestParent.class, parent.getId());
        return Response.ok().build();
    }

    @GET
    @Path("/managed")
    public Response isManaged() {
        try {
            parent.getChildren().size(); // should throw LazyInitializationException
            return Response.ok().build();
        } catch (PersistenceException e) {
            return Response.noContent().build();
        }
    }
}

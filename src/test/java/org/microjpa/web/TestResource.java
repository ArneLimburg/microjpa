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
package org.microjpa.web;

import static javax.persistence.PersistenceContextType.EXTENDED;
import static org.junit.jupiter.api.Assertions.assertFalse;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

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

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

import static jakarta.enterprise.event.TransactionPhase.AFTER_COMPLETION;
import static jakarta.enterprise.event.TransactionPhase.BEFORE_COMPLETION;
import static jakarta.persistence.PersistenceContextType.EXTENDED;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static java.util.Optional.ofNullable;
import static org.junit.jupiter.api.Assertions.assertFalse;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceException;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.EntityTag;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import org.microjpa.child.TestChild;
import org.microjpa.exception.RollbackApplicationException;
import org.microjpa.parent.TestParent;

@ApplicationScoped
@Path("/test-parent")
public class TestResource {

    @Inject
    private TransactionTemplate transactionTemplate;
    @PersistenceContext(unitName = "test-unit", type = EXTENDED)
    private EntityManager entityManager;
    @Inject
    private Event<TestParent> testParentModifiedEvent;
    @Inject
    private Event<EntityTag> entityTagEvent;
    private TestParent parent;
    private boolean beforeCompletion;

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
    @WithETag
    @Transactional
    @Consumes(APPLICATION_JSON)
    public Response createParent(TestParent parent, @Context UriInfo uriInfo) {
        TestParent testParent = new TestParent(parent.getName());
        entityManager.persist(testParent);
        testParentModifiedEvent.fire(testParent);
        return Response
            .created(uriInfo.getAbsolutePathBuilder().path(Long.toString(testParent.getId())).build())
            .build();
    }

    @PUT
    @WithETag
    @Path("{id}")
    @Transactional
    @Consumes(APPLICATION_JSON)
    public void updateParent(
        @PathParam("id") long id,
        @QueryParam("rollback") Boolean rollback,
        @QueryParam("beforeCompletion") Boolean beforeCompletion,
        TestParent parent) {
        this.beforeCompletion = ofNullable(beforeCompletion).orElse(false);

        TestParent loadedParent = entityManager.find(TestParent.class, id);
        loadedParent.setName(parent.getName());
        testParentModifiedEvent.fire(loadedParent);
        if (ofNullable(rollback).orElse(false)) {
            throw new RollbackApplicationException();
        }
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

    void publishEntityTagBeforeCompletion(@Observes(during = BEFORE_COMPLETION) TestParent testParent) {
        if (beforeCompletion) {
            entityTagEvent.fire(new EntityTag(Long.toString(testParent.getVersion())));
        }
    }

    void publishEntityTagAfterSuccess(@Observes(during = AFTER_COMPLETION) TestParent testParent) {
        if (beforeCompletion) {
            return;
        }
        entityTagEvent.fire(new EntityTag(Long.toString(testParent.getVersion())));
    }
}

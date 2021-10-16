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

import static io.undertow.Handlers.path;
import static io.undertow.Handlers.redirect;
import static java.util.Arrays.asList;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.URL;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.inject.se.SeContainer;
import javax.enterprise.inject.se.SeContainerInitializer;
import javax.servlet.ServletException;

import org.jboss.resteasy.plugins.servlet.ResteasyServletInitializer;
import org.jboss.weld.environment.servlet.Listener;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import io.undertow.Undertow;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.ListenerInfo;
import io.undertow.servlet.api.ServletContainerInitializerInfo;

public class WeldRequestScopedTest extends AbstractRequestScopedTest {

    private Undertow server;
    private SeContainer container;

    @BeforeEach
    public void initialize() throws IOException, ServletException {
        container = SeContainerInitializer.newInstance().initialize();

        DeploymentInfo servletBuilder = Servlets.deployment()
            .setClassLoader(Thread.currentThread().getContextClassLoader())
            .setContextPath("/")
            .setDeploymentName("test.war")
            .addServletContainerInitializer(new ServletContainerInitializerInfo(ResteasyServletInitializer.class,
            new HashSet<>(asList(TestApplication.class, TestResource.class))))
            .addListener(new ListenerInfo(Listener.class))
            .addInitParameter("resteasy.injector.factory", "org.jboss.resteasy.cdi.CdiInjectorFactory");

        DeploymentManager manager = Servlets.defaultContainer().addDeployment(servletBuilder);
        try {
            manager.deploy();
        } catch (RuntimeException e) {
            Logger.getLogger("Test").log(Level.WARNING, e.getMessage(), e);
            throw e;
        }

        int port = findFreePort();
        server = Undertow.builder()
            .addHttpListener(port, "localhost")
            .setHandler(path(redirect("/")).addPrefixPath("/", manager.start()))
            .build();
        server.start();
        URL baseUrl = new URL("http://localhost:" + port + "/test-parent");
        super.initialize(baseUrl);
    }

    @AfterEach
    public void shutDown() {
        container.close();
        server.stop();
    }

    private int findFreePort() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(0)) {
            return serverSocket.getLocalPort();
        }
    }
}

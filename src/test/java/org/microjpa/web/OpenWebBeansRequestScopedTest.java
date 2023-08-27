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

import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.servlet.ServletConfig;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Feature;
import jakarta.ws.rs.ext.Provider;

import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.jaxrs.utils.ResourceUtils;
import org.apache.cxf.transport.servlet.CXFNonSpringServlet;
import org.apache.webbeans.servlet.WebBeansConfigurationListener;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.microjpa.tags.WebTest;

@WebTest
class OpenWebBeansRequestScopedTest extends AbstractRequestScopedTest {

    private Server server;

    @BeforeEach
    public void initialize() throws Exception {
        server = new Server();
        ServerConnector connector = new ServerConnector(server);
        connector.setPort(findFreePort());
        server.addConnector(connector);
        ServletContextHandler context = new ServletContextHandler();
        context.setContextPath("/");
        context.addEventListener(new WebBeansConfigurationListener());
        context.addServlet(CXFCdiServlet.class, "/*");
        server.setHandler(context);
        server.start();
        super.initialize(new URL("http://localhost:" + connector.getPort() + "/test-parent"));
    }

    @AfterEach
    public void shutDown() throws Exception {
        server.stop();
    }

    private int findFreePort() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(0)) {
            return serverSocket.getLocalPort();
        }
    }

    public static class CXFCdiServlet extends CXFNonSpringServlet {
        @Override
        protected void loadBus(ServletConfig servletConfig) {
            super.loadBus(servletConfig);
            Application application = CDI.current().select(Application.class).get();
            List<Feature> features = new ArrayList<>();
            CDI.current().select(Feature.class).forEach(features::add);
            Stream<Bean<?>> serviceBeans = CDI.current().getBeanManager()
                .getBeans(Object.class).stream().filter(b -> b.getBeanClass().isAnnotationPresent(Path.class));
            Stream<Bean<?>> providerBeans = CDI.current().getBeanManager()
                .getBeans(Object.class).stream().filter(b -> b.getBeanClass().isAnnotationPresent(Provider.class));
            final JAXRSServerFactoryBean instance
                = ResourceUtils.createApplication(application, false, false, false, bus);
            instance.setServiceBeans(serviceBeans.map(b -> CDI.current().select(b.getBeanClass()).get()).collect(toList()));
            instance.setProviders(providerBeans.map(b -> CDI.current().select(b.getBeanClass()).get()).collect(toList()));
            instance.setFeatures((List)features);
            instance.setApplication(application);
            instance.init();
        }

        @Override
        public void destroyBus() {
            getBus().shutdown(true);
            setBus(null);
        }
    }
}

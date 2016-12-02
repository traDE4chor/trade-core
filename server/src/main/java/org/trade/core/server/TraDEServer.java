/*
 * Copyright 2016 Michael Hahn
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

package org.trade.core.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.servlet.ServletContainer;

/**
 * Created by hahnml on 07.11.2016.
 */
public class TraDEServer {

    private int port = 3000;

    public void startHTTPServer() throws Exception {
        Server server = new Server(port);

        // Create a new ServletContextHandler for the API
        ServletContextHandler apiContext = new ServletContextHandler(ServletContextHandler.SESSIONS);
        apiContext.setContextPath("/");

        // Setup Swagger API resources
        ServletHolder apiServlet = apiContext.addServlet(ServletContainer.class, "/api/*");
        apiServlet.setInitOrder(1);
        apiServlet.setInitParameter("jersey.config.server.provider.packages", "io.swagger.jaxrs.listing;" +
                "io.swagger.jaxrs.json;io.swagger.sample.resource;io.swagger.trade.server.jersey.api");
        apiServlet.setInitParameter("jersey.config.server.provider.classnames",
                "org.glassfish.jersey.media.multipart.MultiPartFeature;org.glassfish.jersey.jackson.JacksonFeature;" +
                        "org.trade.core.server.jackson.CustomObjectMapperProvider");
        apiServlet.setInitParameter("jersey.config.server.wadl.disableWadl", "true");

        // Create a new ServletContextHandler for the Swagger UI and the API documentation
        ServletContextHandler docContext = new ServletContextHandler(ServletContextHandler.SESSIONS);
        docContext.setContextPath("/docs");

        // Setup Swagger servlet
        ServletHolder swaggerServlet = docContext.addServlet(io.swagger.jersey.config.JerseyJaxrsConfig.class,
                "/swagger-core");
        swaggerServlet.setInitOrder(2);

        // Setup Swagger-UI static resources
        String resourceBasePath = TraDEServer.class.getResource("/swaggerui").toExternalForm();
        docContext.setWelcomeFiles(new String[] {"index.html"});
        docContext.setResourceBase(resourceBasePath);
        docContext.addServlet(new ServletHolder(new DefaultServlet()), "/*");

        // Add the handlers to the server
        ContextHandlerCollection handlers = new ContextHandlerCollection();
        handlers.setHandlers(new Handler[]{apiContext, docContext});
        server.setHandler(handlers);

        try {
            server.start();
            server.join();
        } finally {
            server.destroy();
        }
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}

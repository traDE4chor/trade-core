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

import io.swagger.trade.server.jersey.api.ApiOriginFilter;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.glassfish.jersey.servlet.ServletContainer;
import org.trade.core.data.management.DataManagerFactory;
import org.trade.core.data.management.IDataManager;
import org.trade.core.notification.management.INotificationManager;
import org.trade.core.notification.management.NotificationManagerFactory;
import org.trade.core.utils.TraDEProperties;

/**
 * Created by hahnml on 07.11.2016.
 */
public class TraDEServer {

    private Server server = null;

    private IDataManager dataManager = null;

    private INotificationManager notificationManager = null;

    public void startHTTPServer(TraDEProperties properties) throws Exception {
        QueuedThreadPool threadPool = new QueuedThreadPool(properties.getServerMaxNumberOfThreads(), properties
                .getServerMinNumberOfThreads());

        server = new Server(threadPool);

        setupConnectors(properties);

        setupHandlers(properties);

        initializeManagers();

        server.start();
    }

    public void stopHTTPServer() throws Exception {
        server.stop();
        server.destroy();
    }

    private void setupConnectors(TraDEProperties props) {
        Connector[] connectors;

        // HTTP Configuration
        HttpConfiguration http_config = new HttpConfiguration();
        http_config.setSecureScheme("https");
        http_config.setSecurePort(props.getHttpsServerPort());
        http_config.setOutputBufferSize(32768);

        // HTTP connector
        ServerConnector http = new ServerConnector(server,
                new HttpConnectionFactory(http_config));
        http.setPort(props.getHttpServerPort());
        http.setIdleTimeout(30000);

        connectors = new Connector[]{http};

          // TODO: It seems that the registration of the https connector causes for some reason performance issues for high workloads. This has to be further inspected in future.
//        String keystorePath = props.getServerKeystore();
//        if (keystorePath != null) {
//            File keystoreFile = new File(".", keystorePath).getAbsoluteFile();
//            if (keystoreFile.exists()) {
//                // SSL Context Factory for HTTPS
//                SslContextFactory sslContextFactory = new SslContextFactory();
//                sslContextFactory.setKeyStorePath(keystoreFile.getAbsolutePath());
//                sslContextFactory.setKeyStorePassword(props.getKeyStorePassword());
//
//                // HTTPS Configuration
//                HttpConfiguration https_config = new HttpConfiguration(http_config);
//                SecureRequestCustomizer src = new SecureRequestCustomizer();
//                src.setStsMaxAge(2000);
//                src.setStsIncludeSubDomains(true);
//                https_config.addCustomizer(src);
//
//                // HTTPS connector
//                ServerConnector https = new ServerConnector(server,
//                        new SslConnectionFactory(sslContextFactory, HttpVersion.HTTP_1_1.asString()),
//                        new HttpConnectionFactory(https_config));
//                https.setPort(props.getHttpsServerPort());
//                https.setIdleTimeout(500000);
//
//                connectors = new Connector[]{http, https};
//            }
//        }

        // Set the connectors
        server.setConnectors(connectors);
    }

    private void setupHandlers(TraDEProperties props) {
        // Create a new ServletContextHandler for the API
        ServletContextHandler apiContext = new ServletContextHandler(ServletContextHandler.SESSIONS);
        apiContext.setContextPath("/");

        // Setup Swagger API resources
        ServletHolder apiServlet = apiContext.addServlet(ServletContainer.class, "/api/*");
        apiServlet.setInitOrder(1);
        apiServlet.setInitParameter("jersey.config.server.provider.packages", "io.swagger.jaxrs.listing;" +
                "io.swagger.jaxrs.json;io.swagger.sample.resource;io.swagger.trade.server.jersey.api;org.trade.core.server");
        apiServlet.setInitParameter("jersey.config.server.provider.classnames",
                "org.glassfish.jersey.media.multipart.MultiPartFeature;org.glassfish.jersey.jackson.JacksonFeature;" +
                        "org.trade.core.server.jackson.CustomObjectMapperProvider");
        apiServlet.setInitParameter("jersey.config.server.wadl.disableWadl", "true");

        // Add the generated ApiOriginFilter to allow cross-origin requests
        apiContext.addFilter(ApiOriginFilter.class, "/*", null);

        // Create a new ServletContextHandler for the Swagger UI and the API documentation
        ServletContextHandler docContext = new ServletContextHandler(ServletContextHandler.SESSIONS);
        docContext.setContextPath("/docs");

        // Setup Swagger servlet
        ServletHolder swaggerServlet = docContext.addServlet(io.swagger.jersey.config.JerseyJaxrsConfig.class,
                "/swagger-core");
        swaggerServlet.setInitParameter("swagger.api.basepath", "http://localhost:" + props.getHttpServerPort() + "/api");
        swaggerServlet.setInitOrder(2);

        // Setup Swagger-UI static resources
        String resourceBasePath = TraDEServer.class.getResource("/swaggerui").toExternalForm();
        docContext.setWelcomeFiles(new String[]{"index.html"});
        docContext.setResourceBase(resourceBasePath);
        docContext.addServlet(new ServletHolder(new DefaultServlet()), "/*");

        // Add the handlers to the server
        ContextHandlerCollection handlers = new ContextHandlerCollection();
        handlers.setHandlers(new Handler[]{apiContext, docContext});
        server.setHandler(handlers);
    }

    private void initializeManagers() {
        dataManager = DataManagerFactory.createDataManager();
        notificationManager = NotificationManagerFactory.createNotificationManager();
    }
}

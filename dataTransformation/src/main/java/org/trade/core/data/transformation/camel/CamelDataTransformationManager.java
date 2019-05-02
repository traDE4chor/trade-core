/*
 * Copyright 2018 Michael Hahn
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

package org.trade.core.data.transformation.camel;

import io.swagger.hdtapps.client.jersey.ApiClient;
import org.apache.camel.CamelContext;
import org.apache.camel.ExchangePattern;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trade.core.auditing.AuditingServiceFactory;
import org.trade.core.auditing.events.ATraDEEvent;
import org.trade.core.auditing.events.internal.DataValueAssociationChanged;
import org.trade.core.data.transformation.IDataTransformationManager;
import org.trade.core.data.transformation.camel.processors.CamelDataTransformationProcessor;
import org.trade.core.model.data.DataDependencyGraph;
import org.trade.core.model.data.DataElement;
import org.trade.core.model.data.DataValue;
import org.trade.core.model.data.instance.DataElementInstance;
import org.trade.core.model.dataTransformation.DataTransformation;
import org.trade.core.utils.TraDEProperties;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class provides functionality for the management of data transformations and the invocation of underlying data
 * transformation logic specified in an data dependency graph.
 * <p>
 * Created by hahnml on 25.01.2018.
 */
public class CamelDataTransformationManager implements IDataTransformationManager {

    public static final String ERROR_LOG_ENDPOINT = "log:org.trade.core.data.transformation.management.camel.route";

    public static final String LOG_ENDPOINT = "log:org.trade.core.camel?level=DEBUG";

    public static final String DEFAULT_ROUTING_COMPONENT = "vm:";

    private static final Logger logger = LoggerFactory.getLogger("org.trade.core.data.transformation.camel.CamelDataTransformationManager");

    private Map<String, CamelContext> dynamicContexts = new HashMap<>();

    private Map<String, ProducerTemplate> producerTemplaces = new HashMap<>();

    private Map<String, List<DataTransformation>> cachedCorrelatedTransformations = new ConcurrentHashMap<>();

    private ApiClient transformationClient;

    private io.swagger.trade.client.jersey.ApiClient tradeClient;

    private DataDependencyGraph ddg;

    public CamelDataTransformationManager() {
        AuditingServiceFactory.createAuditingService().registerEventListener(this);
    }

    @Override
    public void initializeFromGraph(DataDependencyGraph graph) {
        this.ddg = graph;

        // Create dynamic routes
        if (this.ddg.hasDataTransformations()) {
            for (DataTransformation transf : this.ddg.getDataTransformations()) {
                createDynamicRoute(transf);
            }
        }
    }

    // Implementation of IAuditingService methods
    @Override
    public void onEvent(ATraDEEvent event) {

        // Only check events which are of type 'data' (DataChangeEvent)
        if (event.getType() == ATraDEEvent.TYPE.data) {
            // Check if a data dependency graph is set
            if (ddg != null) {
                // Filter the incoming events to check if they are relevant for this manager, i.e., are related to the
                // managed data transformations. Since more than one data transformation might have to be triggered for a
                // data change, we get a list back.
                List<DataTransformation> transformations = resolveTransformations(event);

                // Trigger the resolved data transformations
                for (DataTransformation transformation : transformations) {
                    ProducerTemplate producer = null;

                    // Check if we already have a producer template
                    if (this.producerTemplaces.containsKey(transformation.getIdentifier())) {
                        producer = this.producerTemplaces.get(transformation.getIdentifier());
                    } else {
                        // Resolve the dynamic context of the transformation
                        CamelContext context = this.dynamicContexts.get(transformation.getIdentifier());

                        // Create a producer template to forward the event to a processor according to the defined dynamic route
                        producer = context.createProducerTemplate();

                        this.producerTemplaces.put(transformation.getIdentifier(), producer);
                    }

                    // Forward the event to the corresponding processor of the transformation
                    producer.sendBody(getDefaultEndpointString(transformation.getIdentifier()), ExchangePattern.InOnly,
                            event);
                }
            }
        } else {
            // Invalidate the corresponding cache (cachedCorrelatedTransformations) entries, if the associations of a
            // cached data value changes
            if (event.getType() == ATraDEEvent.TYPE.internal && event instanceof DataValueAssociationChanged) {
                if (this.cachedCorrelatedTransformations.containsKey(event.getIdentifier())) {
                    List<DataTransformation> transformations = this.cachedCorrelatedTransformations.get(event
                            .getIdentifier());

                    for (DataTransformation transformation : transformations) {
                        // If an association changes we also have to invalidate the list of related
                        // data element instance that share the same data value at the transformations
                        transformation.getRelatedDataElementInstances().remove(event.getIdentifier());
                    }

                    this.cachedCorrelatedTransformations.remove(event.getIdentifier());
                }
            }
        }
    }

    @Override
    public void startup(TraDEProperties properties) {
        try {
            // Create a new API client for the HDTApps framework
            transformationClient = new ApiClient();
            transformationClient.setBasePath(properties.getHdtAppFrameworkURL());

            // Create a new API client for the TraDE Middleware
            tradeClient = new io.swagger.trade.client.jersey.ApiClient();
            tradeClient.setBasePath(properties.getTraDEMiddlewareURL());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void shutdown() {
        try {
            // Stop all dynamic contexts
            for (CamelContext context : this.dynamicContexts.values()) {
                context.stop();
            }

            this.dynamicContexts.clear();
            this.dynamicContexts = null;
            this.ddg = null;
            this.transformationClient = null;
            this.tradeClient = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createDynamicRoute(DataTransformation transformation) {
        // Create a new camel context for the data transformation
        CamelContext context = new DefaultCamelContext();

        // Try to setup and add the route to the camel context
        try {
            RouteBuilder builder = new RouteBuilder() {
                public void configure() {
                    errorHandler(deadLetterChannel(ERROR_LOG_ENDPOINT));

                    if (logger.isDebugEnabled()) {
                        // Add a route with a custom processor which triggers the required data transformation logic for
                        // each registered data transformation. Furthermore, add the log endpoint as static
                        // intermediary, if debugging is enabled
                        from(getDefaultEndpointString(transformation.getIdentifier())).to(LOG_ENDPOINT).process(new
                                CamelDataTransformationProcessor(transformationClient, tradeClient, transformation));
                    } else {
                        // Add a route with a custom processor which triggers the required data transformation logic for
                        // each registered data transformation
                        from(getDefaultEndpointString(transformation.getIdentifier())).process(new
                                CamelDataTransformationProcessor(transformationClient, tradeClient, transformation));
                    }
                }
            };

            // Add the route to the context
            context.addRoutes(builder);

            // Remember the context
            dynamicContexts.put(transformation.getIdentifier(), context);

            // Start the context
            context.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getDefaultEndpointString(String endpoint) {
        return DEFAULT_ROUTING_COMPONENT + endpoint;
    }

    private List<DataTransformation> resolveTransformations(ATraDEEvent event) {
        List<DataTransformation> transformations = new ArrayList<>();

        // Check if the event has an attached source object which we require for resolving if a transformation
        // matches or not
        if (event.getEventSource() != null) {
            DataValue value = (DataValue) event.getEventSource();

            // Check our cache if we already correlated the data value to a transformation
            if (this.cachedCorrelatedTransformations.containsKey(value.getIdentifier())) {
                transformations = this.cachedCorrelatedTransformations.get(value.getIdentifier());
            } else {
                // Correlate the data value to one or more transformations, if there are matching ones

                // Get the data element instance to which this data value is associated and try to find if one of
                // the underlying data elements is the source of a known data transformation
                for (DataElementInstance elmInstance : value.getDataElementInstances()) {
                    DataElement element = elmInstance.getDataElement();

                    for (DataTransformation currentTransformation : this.ddg.getDataTransformations()) {
                        // Check if the data element the data value belongs to, is specified as the source of the
                        // data transformation. If this is the case, we resolved a data transformation that has
                        // to be triggered and therefore add it to the result list
                        if (currentTransformation.getSource().getIdentifier().equals(element.getIdentifier())) {
                            // HINT: The same transformation could be added multiple times to the list for different
                            // data element instance that share the same data value. To keep track of this we hold a
                            // list of these data element instance at the transformation
                            if (!currentTransformation.getRelatedDataElementInstances().containsKey(event
                                    .getIdentifier())) {
                                // Initialize the list
                                currentTransformation.getRelatedDataElementInstances().put(event
                                        .getIdentifier(), new HashSet<>());
                            }

                            currentTransformation.getRelatedDataElementInstances().get(event
                                    .getIdentifier()).add(elmInstance);

                            transformations.add(currentTransformation);
                        }
                    }
                }

                if (!transformations.isEmpty()) {
                    // Finally we add the resolved result list to the cache for its later reuse
                    this.cachedCorrelatedTransformations.put(value.getIdentifier(), transformations);
                }
            }
        }

        return transformations;
    }
}

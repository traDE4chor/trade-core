/*
 * Copyright 2017 Michael Hahn
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

package io.swagger.trade.server.jersey.api.impl;

import io.swagger.trade.server.jersey.api.DataElementInstancesApiService;
import io.swagger.trade.server.jersey.api.NotFoundException;
import io.swagger.trade.server.jersey.api.util.ResourceTransformationUtils;
import io.swagger.trade.server.jersey.model.*;
import org.trade.core.data.management.DataManager;

import javax.validation.constraints.NotNull;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.Collections;
import java.util.List;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-04-04T17:08:04.791+02:00")
public class DataElementInstancesApiServiceImpl extends DataElementInstancesApiService {
    @Override
    public Response getDataElementInstance(String instanceId, SecurityContext securityContext, UriInfo uriInfo) throws NotFoundException {
        Response response = null;

        org.trade.core.model.data.instance.DataElementInstance dataElementInstance = DataManager.getInstance()
                .getDataElementInstance(instanceId);

        try {
            if (dataElementInstance != null) {
                DataElementInstanceWithLinks result = new DataElementInstanceWithLinks();

                result.setInstance(ResourceTransformationUtils.model2Resource(dataElementInstance));

                // Set HREF and links to related resources
                UriBuilder builder = uriInfo.getAbsolutePathBuilder();
                URI valueUri = builder.build();

                result.getInstance().setHref(valueUri.toASCIIString());

                // Set links to related resources
                result.setLinks(LinkUtils.createDataElementInstanceLinks(uriInfo, dataElementInstance, result
                        .getInstance().getHref()));

                response = Response.ok().entity(result).build();
            } else {
                response = Response.status(Response.Status.NOT_FOUND).entity(new NotFound().properties(Collections
                        .singletonList(instanceId)).message("A data element instance with id = '" + instanceId +
                        "' is not available."))
                        .build();
            }
        } catch (Exception e) {
            e.printStackTrace();

            response = Response.serverError().entity(e.getMessage()).build();
        }

        return response;
    }

    @Override
    public Response getDataValue(String elementInstanceId, SecurityContext securityContext, UriInfo uriInfo) throws
            NotFoundException {
        Response response = null;

        boolean exists = DataManager.getInstance().hasDataElementInstance(elementInstanceId);

        if (exists) {
            org.trade.core.model.data.instance.DataElementInstance dataElementInstance = DataManager.getInstance()
                    .getDataElementInstance(elementInstanceId);

            try {
                org.trade.core.model.data.DataValue value = dataElementInstance.getDataValue();
                if (value != null) {
                    DataValueWithLinks result = new DataValueWithLinks();

                    result.setDataValue(ResourceTransformationUtils.model2Resource(value));

                    // Set HREF and links to related resources
                    result.getDataValue().setHref(uriInfo.getBaseUriBuilder().path(LinkUtils
                            .TEMPLATE_COLLECTION_RESOURCE).build(LinkUtils.COLLECTION_DATA_VALUE, value
                            .getIdentifier()).toASCIIString());

                    // Set links to related data objects
                    result.setLinks(LinkUtils.createDataValueLinks(uriInfo, value, result.getDataValue().getHref()));

                    response = Response.ok().entity(result).build();
                } else {
                    response = Response.status(Response.Status.NOT_FOUND).entity(new NotFound().properties(Collections
                            .singletonList(elementInstanceId)).message("A data value for data element instance with " +
                            "id = '" + elementInstanceId + "' is not available.")).build();
                }
            } catch (Exception e) {
                e.printStackTrace();

                response = Response.serverError().entity(e.getMessage()).build();
            }
        } else {
            response = Response.status(Response.Status.NOT_FOUND).entity(new NotFound().properties(Collections
                    .singletonList(elementInstanceId)).message("A data element instance with id = '" + elementInstanceId + "' is " +
                    "not available."))
                    .build();
        }

        return response;
    }

    @Override
    public Response setDataValue(String elementInstanceId, DataValue dataValueData, SecurityContext securityContext,
                                 UriInfo uriInfo) throws NotFoundException {
        Response response = null;

        org.trade.core.model.data.DataValue value = null;

        boolean exists = DataManager.getInstance().hasDataElementInstance(elementInstanceId);

        if (exists) {
            org.trade.core.model.data.instance.DataElementInstance elementInstance = DataManager.getInstance()
                    .getDataElementInstance(elementInstanceId);

            if (dataValueData.getId() != null) {
                // Try to set an existing data value
                exists = DataManager.getInstance().hasDataValue(dataValueData.getId());
                if (exists) {
                    org.trade.core.model.data.DataValue dataValue = DataManager.getInstance().getDataValue(dataValueData.getId());

                    try {
                        // Set the data value to the data element instance
                        elementInstance.setDataValue(dataValue);

                        value = dataValue;
                    } catch (Exception e) {
                        e.printStackTrace();

                        response = Response.serverError().entity(e.getMessage()).build();
                    }
                } else {
                    response = Response.status(Response.Status.NOT_FOUND).entity(new NotFound().properties(Collections
                            .singletonList(dataValueData.getId())).message("A data value with id = '" + dataValueData.getId() +
                            "' is not available. Please do not specify an ID value if you want to create and " +
                            "associate a new data value to this data element instance."))
                            .build();
                }
            } else {
                // Try to create a new data value and associate it to this data element instance
                org.trade.core.model.data.DataValue dataValue = DataManager.getInstance().registerDataValue
                        (ResourceTransformationUtils.resource2Model
                                (dataValueData));

                try {
                    elementInstance.setDataValue(dataValue);

                    value = dataValue;
                } catch (Exception e) {
                    e.printStackTrace();

                    response = Response.serverError().entity(e.getMessage()).build();
                }
            }
        } else {
            response = Response.status(Response.Status.NOT_FOUND).entity(new NotFound().properties(Collections
                    .singletonList(elementInstanceId)).message("A data element instance with id = '" + elementInstanceId + "' is " +
                    "not available."))
                    .build();
        }

        // Check if we have a data value to reply and no (fault) response yet
        if (value != null && response == null) {
            DataValueWithLinks result = new DataValueWithLinks();

            result.setDataValue(ResourceTransformationUtils.model2Resource(value));

            // Set HREF and links to related resources
            result.getDataValue().setHref(uriInfo.getBaseUriBuilder().path(LinkUtils
                    .TEMPLATE_COLLECTION_RESOURCE).build(LinkUtils.COLLECTION_DATA_VALUE, value
                    .getIdentifier()).toASCIIString());

            // Set links to related data objects
            result.setLinks(LinkUtils.createDataValueLinks(uriInfo, value, result.getDataValue().getHref()));

            response = Response.ok().entity(result).build();
        }

        return response;
    }

    @Override
    public Response queryDataElementInstance(@NotNull String dataModelNamespace, @NotNull String dataModelName, @NotNull String dataObjectName,
                                             @NotNull String dataElementName, CorrelationPropertyArray correlationProperties, SecurityContext securityContext, UriInfo uriInfo) throws NotFoundException {
        Response response = null;

        List<org.trade.core.model.data.instance.DataElementInstance> dataElementInstance = DataManager.getInstance()
                .queryDataElementInstance(dataModelNamespace, dataModelName, dataObjectName, dataElementName,
                        ResourceTransformationUtils.resource2Model(correlationProperties));

        try {
            if (dataElementInstance != null && dataElementInstance.size() == 1) {
                DataElementInstanceWithLinks result = new DataElementInstanceWithLinks();

                result.setInstance(ResourceTransformationUtils.model2Resource(dataElementInstance.get(0)));

                // Set HREF and links to related resources
                UriBuilder builder = uriInfo.getAbsolutePathBuilder();
                URI valueUri = builder.build();

                result.getInstance().setHref(valueUri.toASCIIString());

                // Set links to related resources
                result.setLinks(LinkUtils.createDataElementInstanceLinks(uriInfo, dataElementInstance.get(0), result
                        .getInstance().getHref()));

                response = Response.ok().entity(result).build();
            } else {
                String message = "";
                if (dataElementInstance.size() > 1) {
                    message = "The provided parameters result in more than one matching data element instance. " +
                            "If you have not specified correlation properties in your request, please retry with " +
                            "corresponding correlation properties leading to a unique result.";
                } else {
                    message = "A data element instance could not be found based on the provided parameters.";
                }

                response = Response.status(Response.Status.NOT_FOUND).entity(new NotFound().message(message))
                        .build();
            }
        } catch (Exception e) {
            e.printStackTrace();

            response = Response.serverError().entity(e.getMessage()).build();
        }

        return response;
    }
}

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

import io.swagger.trade.server.jersey.api.DataObjectInstancesApiService;
import io.swagger.trade.server.jersey.api.NotFoundException;
import io.swagger.trade.server.jersey.api.util.ResourceTransformationUtils;
import io.swagger.trade.server.jersey.model.*;
import org.trade.core.data.management.DataManagerFactory;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.Collections;
import java.util.List;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-04-04T17:08:04.791+02:00")
public class DataObjectInstancesApiServiceImpl extends DataObjectInstancesApiService {
    @Override
    public Response deleteDataObjectInstance(String instanceId, SecurityContext securityContext, UriInfo uriInfo) throws NotFoundException {
        Response response = null;

        try {
            boolean exists = DataManagerFactory.createDataManager().hasDataObjectInstance(instanceId);

            if (exists) {
                DataManagerFactory.createDataManager().deleteDataObjectInstance(instanceId);

                response = Response.status(Response.Status.NO_CONTENT).build();
            } else {
                response = Response.status(Response.Status.NOT_FOUND).entity(new NotFound().properties(Collections
                        .singletonList(instanceId)).message("A data object instance with id = '" + instanceId +
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
    public Response getDataElementInstanceByDataElementName(String dataObjectInstanceId, String dataElementName,
                                                            SecurityContext securityContext, UriInfo uriInfo) throws NotFoundException {
        Response response = null;

        boolean exists = DataManagerFactory.createDataManager().hasDataObjectInstance(dataObjectInstanceId);

        if (exists) {
            try {
                org.trade.core.model.data.instance.DataElementInstance dataElementInstance = DataManagerFactory.createDataManager()
                        .getDataElementInstanceFromDataObjectInstanceByName(dataObjectInstanceId, dataElementName);

                DataElementInstanceWithLinks result = new DataElementInstanceWithLinks();
                result.setInstance(ResourceTransformationUtils.model2Resource(dataElementInstance));

                // Set HREF and links to related resources
                result.getInstance().setHref(uriInfo.getBaseUriBuilder().path(LinkUtils
                        .TEMPLATE_COLLECTION_RESOURCE).build(LinkUtils.COLLECTION_DATA_ELEMENT_INSTANCE, dataElementInstance
                        .getIdentifier()).toASCIIString());

                // Set links to related data elements, etc.
                result.setLinks(LinkUtils.createDataElementInstanceLinks(uriInfo, dataElementInstance, result
                        .getInstance().getHref()));

                response = Response.ok().entity(result).build();
            } catch (Exception e) {
                e.printStackTrace();

                response = Response.serverError().entity(e.getMessage()).build();
            }
        } else {
            response = Response.status(Response.Status.NOT_FOUND).entity(new NotFound().properties(Collections
                    .singletonList(dataObjectInstanceId)).message("A data object instance with id='" +
                    dataObjectInstanceId + "' is not available."))
                    .build();
        }

        return response;
    }

    @Override
    public Response getDataElementInstances(String dataObjectInstanceId, @Min(1) Integer start, @Min(1) Integer size, String status, SecurityContext securityContext, UriInfo uriInfo) throws NotFoundException {
        Response response = null;

        boolean exists = DataManagerFactory.createDataManager().hasDataObjectInstance(dataObjectInstanceId);

        if (exists) {
            try {
                List<org.trade.core.model.data.instance.DataElementInstance> dataElementInstances = DataManagerFactory.createDataManager()
                        .getAllDataElementInstancesOfDataObjectInstance(dataObjectInstanceId, status);
                int filteredListSize = dataElementInstances.size();

                // Check if the start index and the size are in still the range of the filtered result list, if not
                // respond the whole filtered result list
                if (start > 0 && size > 0 && start <= dataElementInstances.size()) {
                    // Calculate the two index
                    int toIndex = start - 1 + size;
                    // Check if the index is still in bounds
                    if (toIndex > dataElementInstances.size()) {
                        toIndex = dataElementInstances.size();
                    }
                    // Decrease start by one since the API starts counting indexes from 1
                    dataElementInstances = dataElementInstances.subList(start - 1, toIndex);
                }

                DataElementInstanceArrayWithLinks resultList = new DataElementInstanceArrayWithLinks();
                resultList.setInstances(new DataElementInstanceArray());
                for (org.trade.core.model.data.instance.DataElementInstance dataElementInstance : dataElementInstances) {

                    DataElementInstanceWithLinks result = new DataElementInstanceWithLinks();

                    result.setInstance(ResourceTransformationUtils.model2Resource(dataElementInstance));

                    // Set HREF and links to related resources
                    UriBuilder builder = uriInfo.getAbsolutePathBuilder();
                    URI valueUri = builder.path(result.getInstance().getId()).build();

                    result.getInstance().setHref(valueUri.toASCIIString());

                    // Set links to related resources
                    result.setLinks(LinkUtils.createDataElementInstanceLinks(uriInfo, dataElementInstance, result
                            .getInstance().getHref()));

                    resultList.getInstances().add(result);
                }

                resultList.setLinks(LinkUtils.createPaginationLinks("data element instances", uriInfo, start, size,
                        filteredListSize));

                response = Response.ok().entity(resultList).build();
            } catch (Exception e) {
                e.printStackTrace();

                response = Response.serverError().entity(e.getMessage()).build();
            }
        } else {
            response = Response.status(Response.Status.NOT_FOUND).entity(new NotFound().properties(Collections
                    .singletonList(dataObjectInstanceId)).message("A data object instance with id='" +
                    dataObjectInstanceId + "' is not available."))
                    .build();
        }

        return response;
    }

    @Override
    public Response getDataObjectInstance(String instanceId, SecurityContext securityContext, UriInfo uriInfo) throws NotFoundException {
        Response response = null;

        org.trade.core.model.data.instance.DataObjectInstance dataObjectInstance = DataManagerFactory.createDataManager()
                .getDataObjectInstance(instanceId);

        try {
            if (dataObjectInstance != null) {
                DataObjectInstanceWithLinks result = new DataObjectInstanceWithLinks();

                result.setInstance(ResourceTransformationUtils.model2Resource(dataObjectInstance));

                // Set HREF and links to related resources
                UriBuilder builder = uriInfo.getAbsolutePathBuilder();
                URI valueUri = builder.build();

                result.getInstance().setHref(valueUri.toASCIIString());

                // Set links to related resources
                result.setLinks(LinkUtils.createDataObjectInstanceLinks(uriInfo, dataObjectInstance, result
                        .getInstance().getHref()));

                response = Response.ok().entity(result).build();
            } else {
                response = Response.status(Response.Status.NOT_FOUND).entity(new NotFound().properties(Collections
                        .singletonList(instanceId)).message("A data object instance with id = '" + instanceId +
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
    public Response queryDataObjectInstance(@NotNull String dataModelNamespace, @NotNull String dataModelName, @NotNull String dataObjectName, CorrelationPropertyArray correlationProperties, SecurityContext securityContext, UriInfo uriInfo) throws NotFoundException {
        Response response = null;

        List<org.trade.core.model.data.instance.DataObjectInstance> dataObjectInstance = DataManagerFactory.createDataManager()
                .queryDataObjectInstance(dataModelNamespace, dataModelName, dataObjectName,
                        ResourceTransformationUtils.resource2Model(correlationProperties));

        try {
            if (dataObjectInstance != null && dataObjectInstance.size() == 1) {
                DataObjectInstanceWithLinks result = new DataObjectInstanceWithLinks();

                result.setInstance(ResourceTransformationUtils.model2Resource(dataObjectInstance.get(0)));

                // Set HREF and links to related resources
                UriBuilder builder = uriInfo.getAbsolutePathBuilder();
                URI valueUri = builder.build();

                result.getInstance().setHref(valueUri.toASCIIString());

                // Set links to related resources
                result.setLinks(LinkUtils.createDataObjectInstanceLinks(uriInfo, dataObjectInstance.get(0), result
                        .getInstance().getHref()));

                response = Response.ok().entity(result).build();
            } else {
                String message = "";
                if (dataObjectInstance.size() > 1) {
                    message = "The provided parameters result in more than one matching data object instance. " +
                            "If you have not specified correlation properties in your request, please retry with " +
                            "corresponding correlation properties leading to a unique result.";
                } else {
                    message = "A data object instance could not be found based on the provided parameters.";
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

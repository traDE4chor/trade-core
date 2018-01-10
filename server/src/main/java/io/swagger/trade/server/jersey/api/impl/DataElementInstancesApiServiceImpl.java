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
import org.trade.core.data.management.DataManagerFactory;

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

        org.trade.core.model.data.instance.DataElementInstance dataElementInstance = DataManagerFactory.createDataManager()
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
    public Response getDataValues(String elementInstanceId, Integer indexOfDataValue, SecurityContext securityContext, UriInfo uriInfo) throws
            NotFoundException {
        Response response = null;

        boolean exists = DataManagerFactory.createDataManager().hasDataElementInstance(elementInstanceId);

        if (exists) {
            org.trade.core.model.data.instance.DataElementInstance dataElementInstance = DataManagerFactory.createDataManager()
                    .getDataElementInstance(elementInstanceId);

            try {
                int numberOfDataValues = dataElementInstance.getNumberOfDataValues();
                if (indexOfDataValue != null && dataElementInstance.getDataElement().isCollectionElement()) {
                    if (indexOfDataValue >= 1 && indexOfDataValue <= numberOfDataValues) {
                        // Retrieve the data value for the specified index. We have to subtract one because the API
                        // provides an index in the range of [1,numberOfDataElements].
                        org.trade.core.model.data.DataValue dataValue = dataElementInstance.getDataValue
                                (indexOfDataValue - 1);

                        if (dataValue != null) {
                            DataValueArrayWithLinks resultList = new DataValueArrayWithLinks();
                            resultList.setDataValues(new DataValueArray());

                            DataValueWithLinks result = new DataValueWithLinks();

                            result.setDataValue(ResourceTransformationUtils.model2Resource(dataValue));

                            // Set HREF and links to related resources
                            result.getDataValue().setHref(uriInfo.getBaseUriBuilder().path(LinkUtils
                                    .TEMPLATE_COLLECTION_RESOURCE).build(LinkUtils.COLLECTION_DATA_VALUE, dataValue
                                    .getIdentifier()).toASCIIString());

                            // Set links to related data objects
                            result.setLinks(LinkUtils.createDataValueLinks(uriInfo, dataValue, result.getDataValue().getHref()));

                            resultList.getDataValues().add(result);

                            response = Response.ok().entity(resultList).build();
                        } else {
                            response = Response.status(Response.Status.NOT_FOUND).entity(new NotFound().properties(Collections
                                    .singletonList(elementInstanceId)).message("A data value for data element instance with " +
                                    "id = '" + elementInstanceId + "' is not available.")).build();
                        }
                    } else {
                        // Index out of bounds
                        response = Response.status(Response.Status.NOT_FOUND).entity(new NotFound().properties(Collections
                                .singletonList(elementInstanceId)).message("There is no data value associated to the " +
                                "specified data element instance at the given index " +
                                "(" + indexOfDataValue + "'. A valid index value has to be in the range of [1," +
                                numberOfDataValues+"].")).build();
                    }
                } else {
                    List<org.trade.core.model.data.DataValue> dataValues = dataElementInstance.getDataValues();

                    if (dataValues != null && !dataValues.isEmpty()) {
                        DataValueArrayWithLinks resultList = new DataValueArrayWithLinks();
                        resultList.setDataValues(new DataValueArray());

                        for (org.trade.core.model.data.DataValue dataValue : dataValues) {
                            DataValueWithLinks result = new DataValueWithLinks();

                            result.setDataValue(ResourceTransformationUtils.model2Resource(dataValue));

                            // Set HREF and links to related resources
                            result.getDataValue().setHref(uriInfo.getBaseUriBuilder().path(LinkUtils
                                    .TEMPLATE_COLLECTION_RESOURCE).build(LinkUtils.COLLECTION_DATA_VALUE, dataValue
                                    .getIdentifier()).toASCIIString());

                            // Set links to related data objects
                            result.setLinks(LinkUtils.createDataValueLinks(uriInfo, dataValue, result.getDataValue().getHref()));

                            resultList.getDataValues().add(result);
                        }

                        response = Response.ok().entity(resultList).build();
                    } else {
                        response = Response.status(Response.Status.NOT_FOUND).entity(new NotFound().properties(Collections
                                .singletonList(elementInstanceId)).message("A data value for data element instance with " +
                                "id = '" + elementInstanceId + "' is not available.")).build();
                    }
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
    public Response associateDataValueToDataElementInstance(String elementInstanceId, DataValue dataValueData, SecurityContext securityContext,
                                                            UriInfo uriInfo) throws NotFoundException {
        Response response = null;

        org.trade.core.model.data.DataValue value = null;

        boolean exists = DataManagerFactory.createDataManager().hasDataElementInstance(elementInstanceId);

        if (exists) {
            org.trade.core.model.data.instance.DataElementInstance elementInstance = DataManagerFactory.createDataManager()
                    .getDataElementInstance(elementInstanceId);

            if (dataValueData.getId() != null) {
                // Try to set an existing data value
                exists = DataManagerFactory.createDataManager().hasDataValue(dataValueData.getId());
                if (exists) {
                    org.trade.core.model.data.DataValue dataValue = DataManagerFactory.createDataManager().getDataValue(dataValueData.getId());

                    try {
                        // Add the data value to the data element instance
                        elementInstance.addDataValue(dataValue);

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
                org.trade.core.model.data.DataValue dataValue = DataManagerFactory.createDataManager().registerDataValue
                        (ResourceTransformationUtils.resource2Model
                                (dataValueData));

                try {
                    elementInstance.addDataValue(dataValue);

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
    public Response removeDataValueFromDataElementInstance(String instanceId, String dataValueId, SecurityContext
            securityContext, UriInfo uriInfo) throws NotFoundException {
        Response response = null;

        org.trade.core.model.data.DataValue value = null;

        boolean exists = DataManagerFactory.createDataManager().hasDataElementInstance(instanceId);

        if (exists) {
            org.trade.core.model.data.instance.DataElementInstance elementInstance = DataManagerFactory.createDataManager()
                    .getDataElementInstance(instanceId);

            if (dataValueId != null) {
                // Try to resolve the referenced data value
                exists = DataManagerFactory.createDataManager().hasDataValue(dataValueId);

                if (exists) {
                    org.trade.core.model.data.DataValue dataValue = DataManagerFactory.createDataManager().getDataValue(dataValueId);

                    try {
                        // Remove the data value from the data element instance
                        elementInstance.removeDataValue(dataValue);

                        response = Response.status(Response.Status.NO_CONTENT).build();
                    } catch (Exception e) {
                        e.printStackTrace();

                        response = Response.serverError().entity(e.getMessage()).build();
                    }
                } else {
                    response = Response.status(Response.Status.NOT_FOUND).entity(new NotFound().properties(Collections
                            .singletonList(dataValueId)).message("A data value with id = '" + dataValueId +
                            "' is not available. Please specify an ID of an existing data value."))
                            .build();
                }
            }
        } else {
            response = Response.status(Response.Status.NOT_FOUND).entity(new NotFound().properties(Collections
                    .singletonList(instanceId)).message("A data element instance with id = '" + instanceId + "' is " +
                    "not available."))
                    .build();
        }

        return response;
    }

    @Override
    public Response queryDataElementInstance(@NotNull String dataModelNamespace, @NotNull String dataModelName, @NotNull String dataObjectName,
                                             @NotNull String dataElementName, CorrelationPropertyArray correlationProperties, SecurityContext securityContext, UriInfo uriInfo) throws NotFoundException {
        Response response = null;

        org.trade.core.model.data.instance.DataElementInstance dataElementInstance = DataManagerFactory.createDataManager()
                .queryDataElementInstance(dataModelNamespace, dataModelName, dataObjectName, dataElementName,
                        ResourceTransformationUtils.resource2Model(correlationProperties));

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
                response = Response.status(Response.Status.NOT_FOUND).entity(new NotFound().message("A data element instance could not be found based on the provided parameters."))
                        .build();
            }
        } catch (Exception e) {
            e.printStackTrace();

            response = Response.serverError().entity(e.getMessage()).build();
        }

        return response;
    }
}

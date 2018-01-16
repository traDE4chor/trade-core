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

import io.swagger.trade.server.jersey.api.DataValuesApiService;
import io.swagger.trade.server.jersey.api.NotFoundException;
import io.swagger.trade.server.jersey.api.util.ResourceTransformationUtils;
import io.swagger.trade.server.jersey.model.*;
import org.trade.core.data.management.DataManagerFactory;

import javax.validation.constraints.Min;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.*;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.List;

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-01-31T17:07:23.956+01:00")
public class DataValuesApiServiceImpl extends DataValuesApiService {

    @Override
    public Response addDataValue(DataValueData dataValueData, SecurityContext securityContext, UriInfo uriInfo) throws NotFoundException {
        Response response = null;

        try {
            // Check if createdBy is specified since this is a mandatory attribute because it can not be changed
            // after creation of the data value
            if (dataValueData.getCreatedBy() == null || dataValueData.getCreatedBy().isEmpty()) {
                response = Response.status(Response.Status.BAD_REQUEST).entity(new InvalidInput()
                        .message("The 'createdBy' attribute in parameter 'body' is required but missing in the " +
                                "processed request.").example("{\n" +
                                "  \"name\": \"someValue\",\n" +
                                "  \"createdBy\": \"hahnml\",\n" +
                                "  \"type\": \"binary\",\n" +
                                "  \"contentType\": \"text/plain\"\n" +
                                "}"))
                        .build();
            } else {
                org.trade.core.model.data.DataValue value = DataManagerFactory.createDataManager().registerDataValue
                        (ResourceTransformationUtils.resource2Model
                                (dataValueData));

                DataValue result = ResourceTransformationUtils.model2Resource(value);

                UriBuilder builder = uriInfo.getAbsolutePathBuilder();
                URI valueUri = builder.path(result.getId()).build();

                result.setHref(valueUri.toASCIIString());

                response = Response.created(valueUri).entity(result).build();
            }
        } catch (Exception e) {
            e.printStackTrace();

            response = Response.serverError().entity(e.getMessage()).build();
        }

        return response;
    }

    @Override
    public Response getDataValueDirectly(String dataValueId, SecurityContext securityContext, UriInfo uriInfo) throws NotFoundException {
        Response response = null;

        org.trade.core.model.data.DataValue value = DataManagerFactory.createDataManager().getDataValue(dataValueId);

        try {
            if (value != null) {
                DataValueWithLinks result = new DataValueWithLinks();

                result.setDataValue(ResourceTransformationUtils.model2Resource(value));

                // Set HREF and links to related resources
                UriBuilder builder = uriInfo.getAbsolutePathBuilder();
                URI valueUri = builder.build();

                result.getDataValue().setHref(valueUri.toASCIIString());

                // Set links to related data element instances
                result.setLinks(LinkUtils.createDataValueLinks(uriInfo, value, result.getDataValue().getHref()));

                response = Response.ok().entity(result).build();
            } else {
                response = Response.status(Response.Status.NOT_FOUND).entity(new NotFound().properties(Collections
                        .singletonList(dataValueId)).message("A Data Value with ID='" + dataValueId + "' is " +
                        "not available."))
                        .build();
            }
        } catch (Exception e) {
            e.printStackTrace();

            response = Response.serverError().entity(e.getMessage()).build();
        }

        return response;
    }

    @Override
    public Response getDataValuesDirectly(@Min(1) Integer start, @Min(1) Integer size, String status, String createdBy, SecurityContext securityContext, UriInfo uriInfo) throws NotFoundException {
        Response response = null;

        try {
            List<org.trade.core.model.data.DataValue> dataValues = DataManagerFactory.createDataManager().getAllDataValues(status, createdBy);
            int filteredListSize = dataValues.size();

            // Check if the start index and the size are in still the range of the filtered result list, if not
            // respond the whole filtered result list
            if (start > 0 && size > 0 && start <= dataValues.size()) {
                // Calculate the two index
                int toIndex = start - 1 + size;
                // Check if the index is still in bounds
                if (toIndex > dataValues.size()) {
                    toIndex = dataValues.size();
                }
                // Decrease start by one since the API starts counting indexes from 1
                dataValues = dataValues.subList(start - 1, toIndex);
            }

            DataValueArrayWithLinks resultList = new DataValueArrayWithLinks();
            resultList.setDataValues(new DataValueArray());
            for (org.trade.core.model.data.DataValue dataValue : dataValues) {

                DataValueWithLinks result = new DataValueWithLinks();

                result.setDataValue(ResourceTransformationUtils.model2Resource(dataValue));

                // Set HREF and links to related resources
                UriBuilder builder = uriInfo.getAbsolutePathBuilder();
                URI valueUri = builder.path(result.getDataValue().getId()).build();

                result.getDataValue().setHref(valueUri.toASCIIString());

                // Set links to related data element instances
                result.setLinks(LinkUtils.createDataValueLinks(uriInfo, dataValue, result.getDataValue().getHref()));

                resultList.getDataValues().add(result);
            }

            resultList.setLinks(LinkUtils.createPaginationLinks("data values", uriInfo, start, size,
                    filteredListSize));

            response = Response.ok().entity(resultList).build();
        } catch (Exception e) {
            e.printStackTrace();

            response = Response.serverError().entity(e.getMessage()).build();
        }

        return response;
    }

    @Override
    public Response pullDataValue(String dataValueId, SecurityContext securityContext, UriInfo uriInfo) throws NotFoundException {
        Response response = null;

        try {
            org.trade.core.model.data.DataValue value = DataManagerFactory.createDataManager().getDataValue(dataValueId);

            if (value != null) {
                StreamingOutput fileStream = new StreamingOutput() {
                    @Override
                    public void write(java.io.OutputStream output) throws IOException, WebApplicationException {
                        try {
                            byte[] data = value.getData();
                            output.write(data);
                            output.flush();
                        } catch (Exception e) {
                            throw new WebApplicationException("Streaming of data for Data Value with " +
                                    "ID='" + dataValueId + "' caused an exception.", e);
                        }
                    }
                };

                response = Response.ok(fileStream, value.getContentType()).header("content-disposition",
                        "attachment; filename = data.txt")
                        .build();
            } else {
                response = Response.status(Response.Status.NOT_FOUND).entity(new NotFound().properties(Collections
                        .singletonList(dataValueId)).message("A Data Value with ID='" + dataValueId + "' is " +
                        "not available."))
                        .build();
            }
        } catch (Exception e) {
            e.printStackTrace();

            response = Response.serverError().entity(e.getMessage()).build();
        }

        return response;
    }

    @Override
    public Response pushDataValue(String dataValueId, Long contentLength, byte[] data, SecurityContext securityContext, UriInfo uriInfo) throws NotFoundException {
        Response response = null;

        try {
            org.trade.core.model.data.DataValue value = DataManagerFactory.createDataManager().getDataValue(dataValueId);

            if (value != null) {
                if (data != null && contentLength != null) {
                    value.setData(data, contentLength);

                    response = Response.status(Response.Status.NO_CONTENT).build();
                } else {
                    // TODO: Specify usefull message and example request
                    response = Response.status(Response.Status.BAD_REQUEST).entity(new InvalidInput()
                            .message("TODO").example("TODO-EXAMPLE")).build();
                }
            } else {
                response = Response.status(Response.Status.NOT_FOUND).entity(new NotFound().properties(Collections
                        .singletonList(dataValueId)).message("A Data Value with ID='" + dataValueId + "' is " +
                        "not available."))
                        .build();
            }
        } catch (Exception e) {
            e.printStackTrace();

            response = Response.serverError().entity(e.getMessage()).build();
        }

        return response;
    }

    @Override
    public Response updateDataValueDirectly(String dataValueId, DataValue dataValue, SecurityContext securityContext, UriInfo uriInfo) throws NotFoundException {
        Response response = null;

        try {
            boolean exists = DataManagerFactory.createDataManager().hasDataValue(dataValueId);

            if (exists) {
                org.trade.core.model.data.DataValue value = DataManagerFactory.createDataManager().updateDataValue(
                        dataValueId, dataValue.getName(), dataValue.getContentType(), dataValue.getType());

                DataValueWithLinks result = new DataValueWithLinks();

                result.setDataValue(ResourceTransformationUtils.model2Resource(value));

                // Set HREF and links to related resources
                UriBuilder builder = uriInfo.getAbsolutePathBuilder();
                URI valueUri = builder.build();

                result.getDataValue().setHref(valueUri.toASCIIString());

                // Set links to realted data element instances
                result.setLinks(LinkUtils.createDataValueLinks(uriInfo, value, result.getDataValue().getHref()));

                response = Response.ok().entity(result).build();
            } else {
                response = Response.status(Response.Status.NOT_FOUND).entity(new NotFound().properties(Collections
                        .singletonList(dataValueId)).message("A Data Value with ID='" + dataValueId + "' is " +
                        "not available."))
                        .build();
            }
        } catch (Exception e) {
            e.printStackTrace();

            response = Response.serverError().entity(e.getMessage()).build();
        }

        return response;
    }

    @Override
    public Response deleteDataValue(String dataValueId, SecurityContext securityContext, UriInfo uriInfo) throws NotFoundException {
        Response response = null;

        try {
            boolean exists = DataManagerFactory.createDataManager().hasDataValue(dataValueId);

            if (exists) {
                DataManagerFactory.createDataManager().deleteDataValue(dataValueId);

                response = Response.status(Response.Status.NO_CONTENT).build();
            } else {
                response = Response.status(Response.Status.NOT_FOUND).entity(new NotFound().properties(Collections
                        .singletonList(dataValueId)).message("A Data Value with ID='" + dataValueId + "' is " +
                        "not available."))
                        .build();
            }
        } catch (Exception e) {
            e.printStackTrace();

            response = Response.serverError().entity(e.getMessage()).build();
        }

        return response;
    }

    @Override
    public Response getDataElementInstancesUsingDataValue(String dataValueId, @Min(1) Integer start, @Min(1) Integer size, SecurityContext securityContext, UriInfo uriInfo) throws NotFoundException {
        Response response = null;

        boolean exists = DataManagerFactory.createDataManager().hasDataValue(dataValueId);

        if (exists) {
            try {
                List<org.trade.core.model.data.instance.DataElementInstance> elementInstances = DataManagerFactory.createDataManager()
                        .getAllDataElementInstancesOfDataValue(dataValueId);
                int filteredListSize = elementInstances.size();

                // Check if the start index and the size are in still the range of the filtered result list, if not
                // respond the whole filtered result list
                if (start > 0 && size > 0 && start <= elementInstances.size()) {
                    // Calculate the two index
                    int toIndex = start - 1 + size;
                    // Check if the index is still in bounds
                    if (toIndex > elementInstances.size()) {
                        toIndex = elementInstances.size();
                    }
                    // Decrease start by one since the API starts counting indexes from 1
                    elementInstances = elementInstances.subList(start - 1, toIndex);
                }

                DataElementInstanceArrayWithLinks resultList = new DataElementInstanceArrayWithLinks();
                resultList.setInstances(new DataElementInstanceArray());
                for (org.trade.core.model.data.instance.DataElementInstance elmInstance : elementInstances) {

                    DataElementInstanceWithLinks result = new DataElementInstanceWithLinks();

                    result.setInstance(ResourceTransformationUtils.model2Resource(elmInstance));

                    // Set HREF and links to related resources
                    result.getInstance().setHref(uriInfo.getBaseUriBuilder().path(LinkUtils
                            .TEMPLATE_COLLECTION_RESOURCE).build(LinkUtils.COLLECTION_DATA_ELEMENT_INSTANCE, elmInstance
                            .getIdentifier()).toASCIIString());

                    // Set links to related data elements, etc.
                    result.setLinks(LinkUtils.createDataElementInstanceLinks(uriInfo, elmInstance, result.getInstance()
                            .getHref()));

                    resultList.getInstances().add(result);
                }

                resultList.setLinks(LinkUtils.createPaginationLinks("data element instances", uriInfo,
                        start, size, filteredListSize));

                response = Response.ok().entity(resultList).build();
            } catch (Exception e) {
                e.printStackTrace();

                response = Response.serverError().entity(e.getMessage()).build();
            }
        } else {
            response = Response.status(Response.Status.NOT_FOUND).entity(new NotFound().properties(Collections
                    .singletonList(dataValueId)).message("A data value with id = '" + dataValueId + "' is " +
                    "not available."))
                    .build();
        }

        return response;
    }
}

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

import de.slub.urn.URNSyntaxException;
import io.swagger.trade.server.jersey.api.DataValuesApiService;
import io.swagger.trade.server.jersey.api.NotFoundException;
import io.swagger.trade.server.jersey.api.util.ResourceTransformationUtils;
import io.swagger.trade.server.jersey.model.*;
import org.trade.core.data.management.DataManager;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-01-31T17:07:23.956+01:00")
public class DataValuesApiServiceImpl extends DataValuesApiService {

    @Override
    public Response addDataValue(DataValueRequest body, SecurityContext securityContext, UriInfo uriInfo) throws NotFoundException {
        Response response = null;

        org.trade.core.model.data.DataValue value = null;
        try {
            // Check if createdBy is specified since this is a mandatory attribute because it can not be changed
            // after creation of the data value
            if (body.getCreatedBy() == null || body.getCreatedBy().isEmpty()) {
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
                value = DataManager.getInstance().registerDataValue
                        (ResourceTransformationUtils.resource2Model
                                (body));

                DataValue result = ResourceTransformationUtils.model2Resource(value);

                UriBuilder builder = uriInfo.getAbsolutePathBuilder();
                URI valueUri = builder.path(result.getId()).build();

                result.setHref(valueUri.toASCIIString());

                response = Response.created(valueUri).entity(result).build();
            }
        } catch (URNSyntaxException e) {
            e.printStackTrace();

            response = Response.serverError().entity(e.getMessage()).build();
        }

        return response;
    }

    @Override
    public Response getDataValueDirectly(String dataValueId, SecurityContext securityContext, UriInfo uriInfo) throws NotFoundException {
        Response response = null;

        org.trade.core.model.data.DataValue value = DataManager.getInstance().getDataValue(dataValueId);

        try {
            if (value != null) {
                DataValue result = ResourceTransformationUtils.model2Resource(value);

                UriBuilder builder = uriInfo.getAbsolutePathBuilder();
                URI valueUri = builder.build();

                result.setHref(valueUri.toASCIIString());

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
    public Response getDataValuesDirectly(Integer limit, String status, SecurityContext securityContext, UriInfo uriInfo) throws NotFoundException {
        Response response = null;

        try {
            List<org.trade.core.model.data.DataValue> dataValues = null;

            dataValues = DataManager.getInstance().getAllDataValues(limit, status);

            List<DataValue> resultList = new ArrayList<>();
            for (org.trade.core.model.data.DataValue dataValue : dataValues) {

                DataValue result = ResourceTransformationUtils.model2Resource(dataValue);

                UriBuilder builder = uriInfo.getAbsolutePathBuilder();
                URI valueUri = builder.path(result.getId()).build();

                result.setHref(valueUri.toASCIIString());

                resultList.add(result);
            }

            response = Response.ok().entity(resultList).build();
        } catch (Exception e) {
            e.printStackTrace();

            response = Response.serverError().entity(e.getMessage()).build();
        }

        return response;
    }

    @Override
    public Response pullDataValue(String dataValueId, SecurityContext securityContext) throws NotFoundException {
        Response response = null;

        try {
            org.trade.core.model.data.DataValue value = DataManager.getInstance().getDataValue(dataValueId);

            if (value != null) {
                response = Response.ok(value.getData(), value.getContentType()).header("Content-Length", value.getSize())
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
    public Response pushDataValue(String dataValueId, Long contentLength, byte[] data, SecurityContext securityContext) throws NotFoundException {
        Response response = null;

        try {
            org.trade.core.model.data.DataValue value = DataManager.getInstance().getDataValue(dataValueId);

            if (value != null) {
                if (data != null && contentLength != null) {
                    value.setData(data, contentLength);

                    response = Response.ok().build();
                } else {
                    // TODO: Specify usefull message and example request
                    response = Response.status(Response.Status.BAD_REQUEST).entity(new InvalidInput()
                            .message("TODO").example("TODO-EXAMPLE"))
                            .build();
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
    public Response updateDataValueDirectly(String dataValueId, DataValueUpdateRequest dataValue, SecurityContext
            securityContext, UriInfo uriInfo) throws NotFoundException {
        Response response = null;

        try {
            boolean exists = DataManager.getInstance().hasDataValue(dataValueId);

            if (exists) {
                org.trade.core.model.data.DataValue value = DataManager.getInstance().updateDataValue(
                        dataValueId, dataValue.getName(), dataValue.getContentType(), dataValue.getType());

                DataValue result = ResourceTransformationUtils.model2Resource(value);

                UriBuilder builder = uriInfo.getAbsolutePathBuilder();
                URI valueUri = builder.build();

                result.setHref(valueUri.toASCIIString());

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
    public Response deleteDataValue(String dataValueId, SecurityContext securityContext) throws NotFoundException {
        Response response = null;

        try {
            boolean exists = DataManager.getInstance().hasDataValue(dataValueId);

            if (exists) {
                org.trade.core.model.data.DataValue value = DataManager.getInstance().deleteDataValue(
                        dataValueId);

                DataValue result = ResourceTransformationUtils.model2Resource(value);

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
}

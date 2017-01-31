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

package io.swagger.trade.server.jersey.api;

import io.swagger.annotations.ApiParam;
import io.swagger.trade.server.jersey.api.factories.DataValuesApiServiceFactory;
import io.swagger.trade.server.jersey.model.DataValue;
import io.swagger.trade.server.jersey.model.DataValueRequest;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

@Path("/dataValues")
@Consumes({"application/json"})
@Produces({"application/json"})
@io.swagger.annotations.Api(description = "the dataValues API")
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-01-31T14:32:55.744+01:00")
public class DataValuesApi {
    private final DataValuesApiService delegate = DataValuesApiServiceFactory.getDataValuesApi();

    @POST

    @Consumes({"application/json"})
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Creates and adds a new data value to the TraDE middleware", notes = "", response = DataValue.class, tags = {"dataValue",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 201, message = "Successfully created and added a new data value", response = DataValue.class),

            @io.swagger.annotations.ApiResponse(code = 400, message = "Invalid input", response = DataValue.class),

            @io.swagger.annotations.ApiResponse(code = 500, message = "Server error", response = DataValue.class)})
    public Response addDataValue(@ApiParam(value = "DataValue object that needs to be added.") DataValueRequest body
            , @Context SecurityContext securityContext, @Context UriInfo uriInfo)
            throws NotFoundException {
        return delegate.addDataValue(body, securityContext, uriInfo);
    }

    @GET
    @Path("/{dataValueId}")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Find a data value by Id", notes = "", response = DataValue.class, tags = {"dataValue",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Returns a `DataValue` resource based on the provided parameters.", response = DataValue.class),

            @io.swagger.annotations.ApiResponse(code = 400, message = "Invalid input", response = DataValue.class),

            @io.swagger.annotations.ApiResponse(code = 404, message = "The required resource was not found based on the provided query parameters.", response = DataValue.class)})
    public Response getDataValueDirectly(@ApiParam(value = "Id of the data value that needs to be fetched", required = true) @PathParam("dataValueId") String dataValueId
            , @Context SecurityContext securityContext, @Context UriInfo uriInfo)
            throws NotFoundException {
        return delegate.getDataValueDirectly(dataValueId, securityContext, uriInfo);
    }

    @GET

    @Consumes({"application/json"})
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "", notes = "Gets all available `DataValue` resources. Optional query param of **limit** determines the limit of returned resources, param **name** filters result list by name and param **status** filters result list by status of the data values. ", response = DataValue.class, responseContainer = "List", tags = {"dataValue",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Returns all `DataValue` resources based on the provided parameters.", response = DataValue.class, responseContainer = "List"),

            @io.swagger.annotations.ApiResponse(code = 404, message = "The required resource was not found based on the provided query parameters.", response = DataValue.class, responseContainer = "List")})
    public Response getDataValuesDirectly(@ApiParam(value = "Limit of returned objects") @QueryParam("limit") Integer limit
            , @ApiParam(value = "Status of data values to return") @QueryParam("status") String status
            , @Context SecurityContext securityContext, @Context UriInfo uriInfo)
            throws NotFoundException {
        return delegate.getDataValuesDirectly(limit, status, securityContext, uriInfo);
    }

    @GET
    @Path("/{dataValueId}/pull")
    @Consumes({"application/json"})
    @Produces({"application/octet-stream", "application/json"})
    @io.swagger.annotations.ApiOperation(value = "Pulls data from the data value identified by Id", notes = "", response = byte[].class, tags = {"dataValue",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Data attached to data value", response = byte[].class),

            @io.swagger.annotations.ApiResponse(code = 400, message = "Invalid input", response = byte[].class),

            @io.swagger.annotations.ApiResponse(code = 404, message = "The required resource was not found based on the provided query parameters.", response = byte[].class),

            @io.swagger.annotations.ApiResponse(code = 500, message = "Server error", response = byte[].class)})
    public Response pullDataValue(@ApiParam(value = "Id of the data value that needs to be fetched", required = true) @PathParam("dataValueId") String dataValueId
            , @Context SecurityContext securityContext, @Context UriInfo uriInfo)
            throws NotFoundException {
        return delegate.pullDataValue(dataValueId, securityContext, uriInfo);
    }

    @POST
    @Path("/{dataValueId}/push")
    @Consumes({"application/octet-stream"})
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Pushes data to the TraDE middleware by attaching it to the data value identified by Id", notes = "", response = void.class, tags = {"dataValue",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 204, message = "Successfully pushed data", response = void.class),

            @io.swagger.annotations.ApiResponse(code = 400, message = "Invalid input", response = void.class),

            @io.swagger.annotations.ApiResponse(code = 404, message = "The required resource was not found based on the provided query parameters.", response = void.class),

            @io.swagger.annotations.ApiResponse(code = 500, message = "Server error", response = void.class)})
    public Response pushDataValue(@ApiParam(value = "Id of the data value to attach data to", required = true) @PathParam("dataValueId") String dataValueId
            , @ApiParam(value = "The size of the data passed as header", required = true) @HeaderParam("Content-Length") Long contentLength
            , @ApiParam(value = "The data to push.", required = true) byte[] data
            , @Context SecurityContext securityContext, @Context UriInfo uriInfo)
            throws NotFoundException {
        return delegate.pushDataValue(dataValueId, contentLength, data, securityContext, uriInfo);
    }

    @PUT
    @Path("/{dataValueId}")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Update an existing data value", notes = "", response = DataValue.class, tags = {"dataValue",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Returns the updated `DataValue` resource based on the provided modifications.", response = DataValue.class),

            @io.swagger.annotations.ApiResponse(code = 400, message = "Invalid input", response = DataValue.class),

            @io.swagger.annotations.ApiResponse(code = 404, message = "The required resource was not found based on the provided query parameters.", response = DataValue.class),

            @io.swagger.annotations.ApiResponse(code = 500, message = "Server error", response = DataValue.class)})
    public Response updateDataValueDirectly(@ApiParam(value = "Id of the data value that needs to be fetched", required = true) @PathParam("dataValueId") String dataValueId
            , @ApiParam(value = "DataValue object that needs to be updated.", required = true) DataValueRequest dataValue
            , @Context SecurityContext securityContext, @Context UriInfo uriInfo)
            throws NotFoundException {
        return delegate.updateDataValueDirectly(dataValueId, dataValue, securityContext, uriInfo);
    }
}

package io.swagger.trade.server.jersey.api;

import io.swagger.trade.server.jersey.model.*;
import io.swagger.trade.server.jersey.api.DataElementsApiService;
import io.swagger.trade.server.jersey.api.factories.DataElementsApiServiceFactory;

import io.swagger.annotations.ApiParam;
import io.swagger.jaxrs.*;

import io.swagger.trade.server.jersey.model.DataElement;
import io.swagger.trade.server.jersey.model.Error;
import io.swagger.trade.server.jersey.model.InvalidInput;
import io.swagger.trade.server.jersey.model.NotFound;

import java.util.List;
import io.swagger.trade.server.jersey.api.NotFoundException;

import java.io.InputStream;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.*;

@Path("/dataElements")
@Consumes({ "application/json" })
@Produces({ "application/json" })
@io.swagger.annotations.Api(description = "the dataElements API")
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-01-31T14:32:55.744+01:00")
public class DataElementsApi  {
   private final DataElementsApiService delegate = DataElementsApiServiceFactory.getDataElementsApi();

    @GET
    @Path("/{dataElementId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Find a data element by Id", notes = "", response = DataElement.class, tags={ "dataElement", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "Returns a `DataElement` resource based on the provided parameters.", response = DataElement.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Data element not found", response = DataElement.class) })
    public Response getDataElementDirectly(@ApiParam(value = "Id of the data element that needs to be fetched",required=true) @PathParam("dataElementId") String dataElementId
,@Context SecurityContext securityContext)
    throws NotFoundException {
        return delegate.getDataElementDirectly(dataElementId,securityContext);
    }
    @GET
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "", notes = "Gets all available `DataElement` resources. Optional query param of **limit** determines the limit of returned resources, param **name** filters result list by name and param **status** filters result list by status of the data elements. ", response = DataElement.class, responseContainer = "List", tags={ "dataElement", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "Returns all `DataElement` resources based on the provided parameters.", response = DataElement.class, responseContainer = "List"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "No data elements found with the specified query parameters", response = DataElement.class, responseContainer = "List") })
    public Response getDataElementsDirectly(@ApiParam(value = "Limit of returned objects") @QueryParam("limit") Integer limit
,@ApiParam(value = "Status of data elements to return") @QueryParam("status") String status
,@Context SecurityContext securityContext)
    throws NotFoundException {
        return delegate.getDataElementsDirectly(limit,status,securityContext);
    }
    @GET
    @Path("/{dataElementId}/pull")
    @Consumes({ "application/json" })
    @Produces({ "application/octet-stream", "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Pulls data from the data value associated to the data element identified by Id", notes = "", response = byte[].class, tags={ "dataElement", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "The data attached to the data element", response = byte[].class),
        
        @io.swagger.annotations.ApiResponse(code = 303, message = "Redirect to the data value holding the requested data", response = byte[].class),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Invalid input", response = byte[].class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "The required resource was not found based on the provided query parameters.", response = byte[].class),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "Server error", response = byte[].class) })
    public Response pullDataElementData(@ApiParam(value = "Id of the data element that needs to be fetched",required=true) @PathParam("dataElementId") String dataElementId
,@ApiParam(value = "If the data should be returned directly or a reference to the data value holding the data", defaultValue="true") @DefaultValue("true") @QueryParam("asReference") Boolean asReference
,@Context SecurityContext securityContext)
    throws NotFoundException {
        return delegate.pullDataElementData(dataElementId,asReference,securityContext);
    }
    @POST
    @Path("/{dataElementId}/push")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Pushes data to the TraDE middleware by attaching a new data value holding the provided data to the data element", notes = "", response = DataElement.class, tags={ "dataElement", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "Successfully pushed data", response = DataElement.class),
        
        @io.swagger.annotations.ApiResponse(code = 303, message = "Redirect to the data value holding the pushed data", response = DataElement.class),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Invalid input", response = DataElement.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "The required resource was not found based on the provided query parameters.", response = DataElement.class),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "Server error", response = DataElement.class) })
    public Response pushDataElementData(@ApiParam(value = "Id of the data element to attach data to",required=true) @PathParam("dataElementId") String dataElementId
,@ApiParam(value = "The data to push." ,required=true) byte[] data
,@ApiParam(value = "If the data value holding the pushed data or only a reference to it should be returned", defaultValue="true") @DefaultValue("true") @QueryParam("replyReference") Boolean replyReference
,@Context SecurityContext securityContext)
    throws NotFoundException {
        return delegate.pushDataElementData(dataElementId,data,replyReference,securityContext);
    }
    @PUT
    @Path("/{dataElementId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Update an existing data element", notes = "", response = void.class, tags={ "dataElement", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 400, message = "Invalid input", response = void.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "The required resource was not found based on the provided query parameters.", response = void.class),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "Server error", response = void.class) })
    public Response updateDataElementDirectly(@ApiParam(value = "Id of the data element that needs to be fetched",required=true) @PathParam("dataElementId") String dataElementId
,@ApiParam(value = "DataElement object that needs to be updated." ) DataElement dataElement
,@Context SecurityContext securityContext)
    throws NotFoundException {
        return delegate.updateDataElementDirectly(dataElementId,dataElement,securityContext);
    }
}

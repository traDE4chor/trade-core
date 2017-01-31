package io.swagger.trade.server.jersey.api;

import io.swagger.trade.server.jersey.model.*;
import io.swagger.trade.server.jersey.api.DataObjectsApiService;
import io.swagger.trade.server.jersey.api.factories.DataObjectsApiServiceFactory;

import io.swagger.annotations.ApiParam;
import io.swagger.jaxrs.*;

import io.swagger.trade.server.jersey.model.DataElement;
import io.swagger.trade.server.jersey.model.Error;
import io.swagger.trade.server.jersey.model.InvalidInput;
import io.swagger.trade.server.jersey.model.NotFound;
import io.swagger.trade.server.jersey.model.DataObject;

import java.util.List;
import io.swagger.trade.server.jersey.api.NotFoundException;

import java.io.InputStream;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.*;

@Path("/dataObjects")
@Consumes({ "application/json" })
@Produces({ "application/json" })
@io.swagger.annotations.Api(description = "the dataObjects API")
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-01-31T14:32:55.744+01:00")
public class DataObjectsApi  {
   private final DataObjectsApiService delegate = DataObjectsApiServiceFactory.getDataObjectsApi();

    @POST
    @Path("/{dataObjectId}/dataElements")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Add a new dataElement to the dataObject", notes = "", response = DataElement.class, tags={ "dataElement", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 201, message = "Successfully created and added a new data element", response = DataElement.class),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Invalid input", response = DataElement.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "The required resource was not found based on the provided query parameters.", response = DataElement.class),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "Server error", response = DataElement.class) })
    public Response addDataElement(@ApiParam(value = "Id of the data object that needs to be fetched",required=true) @PathParam("dataObjectId") String dataObjectId
,@ApiParam(value = "DataElement object that needs to be added." ) DataElement body
,@Context SecurityContext securityContext)
    throws NotFoundException {
        return delegate.addDataElement(dataObjectId,body,securityContext);
    }
    @POST
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Creates and adds a new data object to the TraDE middleware", notes = "", response = DataObject.class, tags={ "dataObject", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 201, message = "Successfully created and added a new data object", response = DataObject.class),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Invalid input", response = DataObject.class),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "Server error", response = DataObject.class) })
    public Response addDataObject(@ApiParam(value = "DataObject object that needs to be added." ) DataObject body
,@Context SecurityContext securityContext)
    throws NotFoundException {
        return delegate.addDataObject(body,securityContext);
    }
    @DELETE
    @Path("/{dataObjectId}/dataElements/{dataElementId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Deletes a data element", notes = "Delete the data element from the data object", response = void.class, tags={ "dataElement", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 404, message = "Invalid combination of data object and data element Ids", response = void.class),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "Server error", response = void.class) })
    public Response deleteDataElement(@ApiParam(value = "Id of the data object that needs to be fetched",required=true) @PathParam("dataObjectId") String dataObjectId
,@ApiParam(value = "Id of the data element that needs to be deleted",required=true) @PathParam("dataElementId") String dataElementId
,@Context SecurityContext securityContext)
    throws NotFoundException {
        return delegate.deleteDataElement(dataObjectId,dataElementId,securityContext);
    }
    @DELETE
    @Path("/{dataObjectId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Deletes a dataObject", notes = "Deletes a complete data object from the TraDE middleware", response = DataObject.class, tags={ "dataObject", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "successful operation", response = DataObject.class),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Invalid input", response = DataObject.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "The required resource was not found based on the provided query parameters.", response = DataObject.class),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "Server error", response = DataObject.class) })
    public Response deleteDataObject(@ApiParam(value = "Id of the data object that needs to be fetched",required=true) @PathParam("dataObjectId") String dataObjectId
,@Context SecurityContext securityContext)
    throws NotFoundException {
        return delegate.deleteDataObject(dataObjectId,securityContext);
    }
    @GET
    @Path("/{dataObjectId}/dataElements/{dataElementId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Find a data element by Id", notes = "", response = DataElement.class, tags={ "dataElement", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "Returns a `DataElement` resource based on the provided parameters.", response = DataElement.class),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Invalid input", response = DataElement.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "The required resource was not found based on the provided query parameters.", response = DataElement.class) })
    public Response getDataElement(@ApiParam(value = "Id of the data object that needs to be fetched",required=true) @PathParam("dataObjectId") String dataObjectId
,@ApiParam(value = "Id of the data element that needs to be fetched",required=true) @PathParam("dataElementId") String dataElementId
,@Context SecurityContext securityContext)
    throws NotFoundException {
        return delegate.getDataElement(dataObjectId,dataElementId,securityContext);
    }
    @GET
    @Path("/{dataObjectId}/dataElements")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "", notes = "Gets all available `DataElement` resources. Optional query param of **limit** determines the limit of returned resources, param **name** filters result list by name and param **status** filters result list by status of the data elements. ", response = DataElement.class, responseContainer = "List", tags={ "dataElement", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "Returns all `DataElement` resources based on the provided parameters.", response = DataElement.class, responseContainer = "List"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "No data elements found with the specified query parameters", response = DataElement.class, responseContainer = "List") })
    public Response getDataElements(@ApiParam(value = "Id of the data object that needs to be fetched",required=true) @PathParam("dataObjectId") String dataObjectId
,@ApiParam(value = "Limit of returned objects") @QueryParam("limit") Integer limit
,@ApiParam(value = "Status of data elements to return") @QueryParam("status") String status
,@Context SecurityContext securityContext)
    throws NotFoundException {
        return delegate.getDataElements(dataObjectId,limit,status,securityContext);
    }
    @GET
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "", notes = "Gets all available `DataObject` resources. Optional query param of **limit** determines the limit of returned resources, param **name** filters result list by name and param **status** filters result list by status of the data objects. ", response = DataObject.class, responseContainer = "List", tags={ "dataObject", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "Returns all `DataObject` resources based on the provided parameters.", response = DataObject.class, responseContainer = "List"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Invalid input", response = DataObject.class, responseContainer = "List") })
    public Response getDataObject(@ApiParam(value = "Limit of returned objects") @QueryParam("limit") Integer limit
,@ApiParam(value = "Name of data objects to return") @QueryParam("name") String name
,@ApiParam(value = "Status of data objects to return") @QueryParam("status") String status
,@Context SecurityContext securityContext)
    throws NotFoundException {
        return delegate.getDataObject(limit,name,status,securityContext);
    }
    @GET
    @Path("/{dataObjectId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Find a data object by Id", notes = "", response = DataObject.class, tags={ "dataObject", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "Returns a `DataObject` resource based on the provided parameters.", response = DataObject.class),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Invalid input", response = DataObject.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "The required resource was not found based on the provided query parameters.", response = DataObject.class) })
    public Response getDataObjectById(@ApiParam(value = "Id of the data object that needs to be fetched",required=true) @PathParam("dataObjectId") String dataObjectId
,@Context SecurityContext securityContext)
    throws NotFoundException {
        return delegate.getDataObjectById(dataObjectId,securityContext);
    }
    @GET
    @Path("/{dataObjectId}/pull")
    @Consumes({ "application/json" })
    @Produces({ "application/octet-stream", "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Reads data from a data object identified by Id", notes = "", response = byte[].class, tags={ "dataObject", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "Returns an archive of all data being attached to the data objects' data elements.", response = byte[].class),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Invalid input", response = byte[].class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "The required resource was not found based on the provided query parameters.", response = byte[].class),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "Server error", response = byte[].class) })
    public Response pullDataObjectData(@ApiParam(value = "Id of the data object that needs to be fetched",required=true) @PathParam("dataObjectId") String dataObjectId
,@ApiParam(value = "Name of a DataElement") @QueryParam("dataElementName") String dataElementName
,@ApiParam(value = "Id of a DataElement") @QueryParam("dataElementId") String dataElementId
,@ApiParam(value = "URL of a remote TraDE middlware to pull the data object from") @QueryParam("sourceTraDE") String sourceTraDE
,@Context SecurityContext securityContext)
    throws NotFoundException {
        return delegate.pullDataObjectData(dataObjectId,dataElementName,dataElementId,sourceTraDE,securityContext);
    }
    @POST
    @Path("/{dataObjectId}/push")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Pushes the data object identified by Id to the TraDE middleware", notes = "", response = DataObject.class, tags={ "dataObject", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "Successfully pushed data", response = DataObject.class),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Invalid input", response = DataObject.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "The required resource was not found based on the provided query parameters.", response = DataObject.class),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "Server error", response = DataObject.class) })
    public Response pushDataObjectData(@ApiParam(value = "Id of the data object to write to",required=true) @PathParam("dataObjectId") String dataObjectId
,@ApiParam(value = "Name of a data element") @QueryParam("dataElement") String dataElement
,@ApiParam(value = "URL of a remote TraDE middleware to push the data object to") @QueryParam("targetTraDE") String targetTraDE
,@ApiParam(value = "DataObject object to push." ) DataObject body
,@Context SecurityContext securityContext)
    throws NotFoundException {
        return delegate.pushDataObjectData(dataObjectId,dataElement,targetTraDE,body,securityContext);
    }
    @PUT
    @Path("/{dataObjectId}/dataElements/{dataElementId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Update an existing data element", notes = "", response = void.class, tags={ "dataElement", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 400, message = "Invalid input", response = void.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Data element not found", response = void.class),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "Server error", response = void.class) })
    public Response updateDataElement(@ApiParam(value = "Id of the data object that needs to be fetched",required=true) @PathParam("dataObjectId") String dataObjectId
,@ApiParam(value = "Id of the data element that needs to be fetched",required=true) @PathParam("dataElementId") String dataElementId
,@ApiParam(value = "DataElement object that needs to be updated." ) DataElement dataElement
,@Context SecurityContext securityContext)
    throws NotFoundException {
        return delegate.updateDataElement(dataObjectId,dataElementId,dataElement,securityContext);
    }
    @PUT
    @Path("/{dataObjectId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Update an existing dataObject", notes = "", response = DataObject.class, tags={ "dataObject", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "successful operation", response = DataObject.class),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Invalid input", response = DataObject.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "The required resource was not found based on the provided query parameters.", response = DataObject.class),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "Server error", response = DataObject.class) })
    public Response updateDataObject(@ApiParam(value = "Id of the data object that needs to be updated",required=true) @PathParam("dataObjectId") String dataObjectId
,@ApiParam(value = "Data object resource that needs to be updated." ) DataObject body
,@Context SecurityContext securityContext)
    throws NotFoundException {
        return delegate.updateDataObject(dataObjectId,body,securityContext);
    }
}

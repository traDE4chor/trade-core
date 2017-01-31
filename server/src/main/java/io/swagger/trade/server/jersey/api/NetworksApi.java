package io.swagger.trade.server.jersey.api;

import io.swagger.trade.server.jersey.model.*;
import io.swagger.trade.server.jersey.api.NetworksApiService;
import io.swagger.trade.server.jersey.api.factories.NetworksApiServiceFactory;

import io.swagger.annotations.ApiParam;
import io.swagger.jaxrs.*;

import io.swagger.trade.server.jersey.model.Network;
import io.swagger.trade.server.jersey.model.Error;
import io.swagger.trade.server.jersey.model.InvalidInput;
import io.swagger.trade.server.jersey.model.Node;
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

@Path("/networks")
@Consumes({ "application/json" })
@Produces({ "application/json" })
@io.swagger.annotations.Api(description = "the networks API")
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-01-31T14:32:55.744+01:00")
public class NetworksApi  {
   private final NetworksApiService delegate = NetworksApiServiceFactory.getNetworksApi();

    @POST
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Creates and adds a new network to the TraDE middleware", notes = "", response = Network.class, tags={ "network", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 201, message = "Successfully created and added a new network", response = Network.class),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Invalid input", response = Network.class),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "Server error", response = Network.class) })
    public Response addNetwork(@ApiParam(value = "Network object that needs to be added." ) Network body
,@Context SecurityContext securityContext)
    throws NotFoundException {
        return delegate.addNetwork(body,securityContext);
    }
    @POST
    @Path("/{networkId}/nodes")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Add a new node to the network", notes = "", response = String.class, tags={ "node", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 201, message = "Successfully created and added a new node", response = String.class),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Invalid input", response = String.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "The required resource was not found based on the provided query parameters.", response = String.class),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "Server error", response = String.class) })
    public Response addNode(@ApiParam(value = "Id of the network that needs to be fetched",required=true) @PathParam("networkId") String networkId
,@ApiParam(value = "Node object that needs to be added." ) Node body
,@Context SecurityContext securityContext)
    throws NotFoundException {
        return delegate.addNode(networkId,body,securityContext);
    }
    @DELETE
    @Path("/{networkId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Deletes a network", notes = "Deletes a complete network from the TraDE middleware", response = Network.class, tags={ "network", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "Successfully deleted existing network", response = Network.class),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Invalid input", response = Network.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "The required resource was not found based on the provided query parameters.", response = Network.class),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "Server error", response = Network.class) })
    public Response deleteNetwork(@ApiParam(value = "Id of the network that needs to be fetched",required=true) @PathParam("networkId") String networkId
,@Context SecurityContext securityContext)
    throws NotFoundException {
        return delegate.deleteNetwork(networkId,securityContext);
    }
    @DELETE
    @Path("/{networkId}/nodes/{nodeId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Deletes a node", notes = "Delete the node from the network", response = void.class, tags={ "node", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 400, message = "Invalid input", response = void.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "The required resource was not found based on the provided query parameters.", response = void.class),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "Server error", response = void.class) })
    public Response deleteNode(@ApiParam(value = "Id of the network that needs to be fetched",required=true) @PathParam("networkId") String networkId
,@ApiParam(value = "Id of the node that needs to be deleted",required=true) @PathParam("nodeId") String nodeId
,@Context SecurityContext securityContext)
    throws NotFoundException {
        return delegate.deleteNode(networkId,nodeId,securityContext);
    }
    @GET
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "", notes = "Gets all available `Network` resources. Optional query parameter of **limit** determines the limit of returned resources, parameter **name** filters result list by name and parameter **status** filters result list by status of the networks. ", response = Network.class, responseContainer = "List", tags={ "network", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "Returns all `Network` resources based on the provided parameters.", response = Network.class, responseContainer = "List"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "The required resource was not found based on the provided query parameters.", response = Network.class, responseContainer = "List") })
    public Response getNetwork(@ApiParam(value = "Limit of returned objects") @QueryParam("limit") Integer limit
,@ApiParam(value = "Name of networks to return") @QueryParam("name") String name
,@ApiParam(value = "Status of networks to return") @QueryParam("status") String status
,@Context SecurityContext securityContext)
    throws NotFoundException {
        return delegate.getNetwork(limit,name,status,securityContext);
    }
    @GET
    @Path("/{networkId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Find a network by Id", notes = "", response = Network.class, tags={ "network", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "Returns a `Network` resource based on the provided parameters.", response = Network.class),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Invalid input", response = Network.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "The required resource was not found based on the provided query parameters.", response = Network.class) })
    public Response getNetworkById(@ApiParam(value = "Id of the network that needs to be fetched",required=true) @PathParam("networkId") String networkId
,@Context SecurityContext securityContext)
    throws NotFoundException {
        return delegate.getNetworkById(networkId,securityContext);
    }
    @GET
    @Path("/{networkId}/nodes/{nodeId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Find a node by Id", notes = "", response = Node.class, tags={ "node", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "Returns a `Node` resource based on the provided parameters.", response = Node.class),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Invalid input", response = Node.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "The required resource was not found based on the provided query parameters.", response = Node.class) })
    public Response getNode(@ApiParam(value = "Id of the network that needs to be fetched",required=true) @PathParam("networkId") String networkId
,@ApiParam(value = "Id of the node that needs to be fetched",required=true) @PathParam("nodeId") String nodeId
,@Context SecurityContext securityContext)
    throws NotFoundException {
        return delegate.getNode(networkId,nodeId,securityContext);
    }
    @GET
    @Path("/{networkId}/nodes")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "", notes = "Gets all available `Node` resources. Optional query param of **limit** determines the limit of returned resources, param **name** filters result list by name and param **status** filters result list by status of the nodes. ", response = Node.class, responseContainer = "List", tags={ "node", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "Returns all `Node` resources based on the provided parameters.", response = Node.class, responseContainer = "List"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "No nodes found with the specified query parameters", response = Node.class, responseContainer = "List") })
    public Response getNodes(@ApiParam(value = "Id of the network that needs to be fetched",required=true) @PathParam("networkId") String networkId
,@ApiParam(value = "Limit of returned objects") @QueryParam("limit") Integer limit
,@Context SecurityContext securityContext)
    throws NotFoundException {
        return delegate.getNodes(networkId,limit,securityContext);
    }
    @PUT
    @Path("/{networkId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Update an existing network", notes = "", response = Network.class, tags={ "network", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "Successful updated existing network", response = Network.class),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Invalid input", response = Network.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "The required resource was not found based on the provided query parameters.", response = Network.class),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "Server error", response = Network.class) })
    public Response updateNetwork(@ApiParam(value = "Id of the network that needs to be updated",required=true) @PathParam("networkId") String networkId
,@ApiParam(value = "Network resource that needs to be updated." ) Network body
,@Context SecurityContext securityContext)
    throws NotFoundException {
        return delegate.updateNetwork(networkId,body,securityContext);
    }
    @PUT
    @Path("/{networkId}/nodes/{nodeId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Update an existing node", notes = "", response = void.class, tags={ "node", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 400, message = "Invalid input", response = void.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "The required resource was not found based on the provided query parameters.", response = void.class),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "Server error", response = void.class) })
    public Response updateNode(@ApiParam(value = "Id of the network that needs to be fetched",required=true) @PathParam("networkId") String networkId
,@ApiParam(value = "Id of the node that needs to be fetched",required=true) @PathParam("nodeId") String nodeId
,@ApiParam(value = "Node object that needs to be updated." ) Node node
,@Context SecurityContext securityContext)
    throws NotFoundException {
        return delegate.updateNode(networkId,nodeId,node,securityContext);
    }
}

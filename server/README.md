# Code Generation with Swagger and 'org.detoeuf.swagger-codegen' Gradle Plugin
Due to missing support for configuration of the code generation process, we are copying all generated API-related files 
(except the model and factory classes) from 'build/generated-sources/swagger' to 
'src/main/java/io/swagger/trade/server/jersey/api' in order to enable their manual adaptation.

After changing the API specification and regenerating the resulting API Java classes all changes have to be 
integrated with the existing code base manually.

The following manual adaptations are done at the moment:

#### Supporting `UriInfo` for dynamic URL building on Resources 
* `, @Context UriInfo uriInfo` is added to all method signatures in all 'xxxApi' files 
(`src/main/java/io/swagger/trade/server/jersey/api`) and `uriInfo` has to be forwarded in `delegator` calls (e.g., `delegate.addDataValue(body,securityContext,uriInfo);`)

  Example: 
  ```
    @GET
    @Path("/{dataValueId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Find a data value by Id", notes = "", response = DataValue.class, tags={ "dataValue", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "Returns a `DataValue` resource based on the 
        provided parameters.", response = DataValue.class),
        @io.swagger.annotations.ApiResponse(code = 400, message = "Invalid input", response = DataValue.class),
        @io.swagger.annotations.ApiResponse(code = 404, message = "The required resource was not found based on the provided query parameters.", response = DataValue.class) })
    
    public Response getDataValueDirectly(@ApiParam(value = "Id of the data value that needs to be fetched",required=true) @PathParam("dataValueId") String dataValueId
    ,@ApiParam(value = "If the data should be returned within the response", defaultValue="false") @DefaultValue("false") @QueryParam("wrapDataInResponse") Boolean wrapDataInResponse
    ,@Context SecurityContext securityContext, @Context URIInfo uriInfo)
        throws NotFoundException {
            return delegate.getDataValueDirectly(dataValueId,wrapDataInResponse,securityContext,uriInfo);
    }
  ``` 

* `, UriInfo uriInfo` is added to all method signatures in all
   * 'xxxApiService' files (`src/main/java/io/swagger/trade/server/jersey/api`)
     
     Example: 
       ```
       public abstract Response addDataValue(DataValue body,SecurityContext securityContext, UriInfo uriInfo) throws NotFoundException;
       ```
   * and 'xxxApiServiceImpl' files (`src/main/java/io/swagger/trade/server/jersey/api/impl`)
     
     Example: 
       ```
       @Override
       public Response addDataValue(DataValue body, SecurityContext securityContext, UriInfo uriInfo) throws NotFoundException {
           return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
       }
       ```
* 
# Code Generation with Swagger and Gradle
We are using the **org.detoeuf.swagger-codegen** Gradle plugin to enable the generation of server code based on a 
provided Swagger specification (`swagger.json`).
All generated API-related files are placed under `build/generated-sources/swagger`. From there the classes 
contained in the `factories` and `ìmpl` packages located under 

`build/generated-sources/swagger/src/main/java/io/swagger/trade/server/jersey/api`

are manually copied once to `src/main/java` in order to enable their manual extension and adaptation.

After changing the API specification and regenerating the resulting API Java classes all breaking changes 
in the copied classes have to be integrated with the existing/generated code base manually.
All other generated model and API-related classes can be used and are compiled as they are.

#### Supporting `UriInfo` for dynamic URL building on Resources through customized *.mustache files
In order to enable the dynamic building of URLs (href, links, etc.) we added customized *.mustache (`/templates/*
.mustache`) that will be used during code generation to add corresponding UriInfo parameters to all relevant method 
signatures:

* `, @Context UriInfo uriInfo` is added to all method signatures in all 'xxxApi' files 
(`build/generated-sources/swagger/../src/main/java/io/swagger/trade/server/jersey/api`) and `uriInfo` has to be 
forwarded in `delegator` calls (e.g., `delegate.addDataValue(body,securityContext,uriInfo);`)

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
   * 'xxxApiService' files (`build/generated-sources/swagger/../src/main/java/io/swagger/trade/server/jersey/api`)
     
     Example: 
       ```
       public abstract Response addDataValue(DataValue body,SecurityContext securityContext, UriInfo uriInfo) throws NotFoundException;
       ```
   * and 'xxxApiServiceImpl' files (`build/generated-sources/swagger/../src/main/java/io/swagger/trade/server/jersey/api/impl`)
     
     Example: 
       ```
       @Override
       public Response addDataValue(DataValue body, SecurityContext securityContext, UriInfo uriInfo) throws NotFoundException {
           return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
       }
       ```
* 
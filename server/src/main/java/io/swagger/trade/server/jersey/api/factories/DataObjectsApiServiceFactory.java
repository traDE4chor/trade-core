package io.swagger.trade.server.jersey.api.factories;

import io.swagger.trade.server.jersey.api.DataObjectsApiService;
import io.swagger.trade.server.jersey.api.impl.DataObjectsApiServiceImpl;

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-01-25T16:21:34.105+01:00")
public class DataObjectsApiServiceFactory {
    private final static DataObjectsApiService service = new DataObjectsApiServiceImpl();

    public static DataObjectsApiService getDataObjectsApi() {
        return service;
    }
}

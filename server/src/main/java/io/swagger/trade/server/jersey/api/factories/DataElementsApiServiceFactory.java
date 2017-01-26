package io.swagger.trade.server.jersey.api.factories;

import io.swagger.trade.server.jersey.api.DataElementsApiService;
import io.swagger.trade.server.jersey.api.impl.DataElementsApiServiceImpl;

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-01-25T16:21:34.105+01:00")
public class DataElementsApiServiceFactory {
    private final static DataElementsApiService service = new DataElementsApiServiceImpl();

    public static DataElementsApiService getDataElementsApi() {
        return service;
    }
}

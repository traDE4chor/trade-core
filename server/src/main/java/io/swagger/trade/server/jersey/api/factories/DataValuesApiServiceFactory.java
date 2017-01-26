package io.swagger.trade.server.jersey.api.factories;

import io.swagger.trade.server.jersey.api.DataValuesApiService;
import io.swagger.trade.server.jersey.api.impl.DataValuesApiServiceImpl;

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-01-25T16:21:34.105+01:00")
public class DataValuesApiServiceFactory {
    private final static DataValuesApiService service = new DataValuesApiServiceImpl();

    public static DataValuesApiService getDataValuesApi() {
        return service;
    }
}

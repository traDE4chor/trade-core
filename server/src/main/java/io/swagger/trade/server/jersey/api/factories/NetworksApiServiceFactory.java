package io.swagger.trade.server.jersey.api.factories;

import io.swagger.trade.server.jersey.api.NetworksApiService;
import io.swagger.trade.server.jersey.api.impl.NetworksApiServiceImpl;

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-01-25T16:21:34.105+01:00")
public class NetworksApiServiceFactory {
    private final static NetworksApiService service = new NetworksApiServiceImpl();

    public static NetworksApiService getNetworksApi() {
        return service;
    }
}

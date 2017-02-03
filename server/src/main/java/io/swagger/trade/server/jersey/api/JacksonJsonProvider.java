package io.swagger.trade.server.jersey.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import io.swagger.util.Json;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;

@Provider
@Produces({MediaType.APPLICATION_JSON})
public class JacksonJsonProvider extends JacksonJaxbJsonProvider {
    private static ObjectMapper commonMapper = Json.mapper();

    public JacksonJsonProvider() {
        commonMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        commonMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        commonMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        commonMapper.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
        commonMapper.enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING);

        super.setMapper(commonMapper);
    }
}
package io.swagger.trade.server.jersey.api;

import io.swagger.trade.server.jersey.api.*;
import io.swagger.trade.server.jersey.model.*;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;

import io.swagger.trade.server.jersey.model.DataElement;
import io.swagger.trade.server.jersey.model.Error;
import io.swagger.trade.server.jersey.model.InvalidInput;
import io.swagger.trade.server.jersey.model.NotFound;

import java.util.List;
import io.swagger.trade.server.jersey.api.NotFoundException;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-01-31T14:32:55.744+01:00")
public abstract class DataElementsApiService {
    public abstract Response getDataElementDirectly(String dataElementId,SecurityContext securityContext) throws NotFoundException;
    public abstract Response getDataElementsDirectly(Integer limit,String status,SecurityContext securityContext) throws NotFoundException;
    public abstract Response pullDataElementData(String dataElementId,Boolean asReference,SecurityContext securityContext) throws NotFoundException;
    public abstract Response pushDataElementData(String dataElementId,byte[] data,Boolean replyReference,SecurityContext securityContext) throws NotFoundException;
    public abstract Response updateDataElementDirectly(String dataElementId,DataElement dataElement,SecurityContext securityContext) throws NotFoundException;
}

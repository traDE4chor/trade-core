package io.swagger.trade.server.jersey.api;

import io.swagger.trade.server.jersey.api.*;
import io.swagger.trade.server.jersey.model.*;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;

import io.swagger.trade.server.jersey.model.DataElement;
import io.swagger.trade.server.jersey.model.Error;
import io.swagger.trade.server.jersey.model.InvalidInput;
import io.swagger.trade.server.jersey.model.NotFound;
import io.swagger.trade.server.jersey.model.DataObject;

import java.util.List;
import io.swagger.trade.server.jersey.api.NotFoundException;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-01-31T14:32:55.744+01:00")
public abstract class DataObjectsApiService {
    public abstract Response addDataElement(String dataObjectId,DataElement body,SecurityContext securityContext) throws NotFoundException;
    public abstract Response addDataObject(DataObject body,SecurityContext securityContext) throws NotFoundException;
    public abstract Response deleteDataElement(String dataObjectId,String dataElementId,SecurityContext securityContext) throws NotFoundException;
    public abstract Response deleteDataObject(String dataObjectId,SecurityContext securityContext) throws NotFoundException;
    public abstract Response getDataElement(String dataObjectId,String dataElementId,SecurityContext securityContext) throws NotFoundException;
    public abstract Response getDataElements(String dataObjectId,Integer limit,String status,SecurityContext securityContext) throws NotFoundException;
    public abstract Response getDataObject(Integer limit,String name,String status,SecurityContext securityContext) throws NotFoundException;
    public abstract Response getDataObjectById(String dataObjectId,SecurityContext securityContext) throws NotFoundException;
    public abstract Response pullDataObjectData(String dataObjectId,String dataElementName,String dataElementId,String sourceTraDE,SecurityContext securityContext) throws NotFoundException;
    public abstract Response pushDataObjectData(String dataObjectId,String dataElement,String targetTraDE,DataObject body,SecurityContext securityContext) throws NotFoundException;
    public abstract Response updateDataElement(String dataObjectId,String dataElementId,DataElement dataElement,SecurityContext securityContext) throws NotFoundException;
    public abstract Response updateDataObject(String dataObjectId,DataObject body,SecurityContext securityContext) throws NotFoundException;
}

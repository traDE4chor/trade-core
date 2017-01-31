package io.swagger.trade.server.jersey.api;

import io.swagger.trade.server.jersey.api.*;
import io.swagger.trade.server.jersey.model.*;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;

import io.swagger.trade.server.jersey.model.Network;
import io.swagger.trade.server.jersey.model.Error;
import io.swagger.trade.server.jersey.model.InvalidInput;
import io.swagger.trade.server.jersey.model.Node;
import io.swagger.trade.server.jersey.model.NotFound;

import java.util.List;
import io.swagger.trade.server.jersey.api.NotFoundException;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-01-31T14:32:55.744+01:00")
public abstract class NetworksApiService {
    public abstract Response addNetwork(Network body,SecurityContext securityContext) throws NotFoundException;
    public abstract Response addNode(String networkId,Node body,SecurityContext securityContext) throws NotFoundException;
    public abstract Response deleteNetwork(String networkId,SecurityContext securityContext) throws NotFoundException;
    public abstract Response deleteNode(String networkId,String nodeId,SecurityContext securityContext) throws NotFoundException;
    public abstract Response getNetwork(Integer limit,String name,String status,SecurityContext securityContext) throws NotFoundException;
    public abstract Response getNetworkById(String networkId,SecurityContext securityContext) throws NotFoundException;
    public abstract Response getNode(String networkId,String nodeId,SecurityContext securityContext) throws NotFoundException;
    public abstract Response getNodes(String networkId,Integer limit,SecurityContext securityContext) throws NotFoundException;
    public abstract Response updateNetwork(String networkId,Network body,SecurityContext securityContext) throws NotFoundException;
    public abstract Response updateNode(String networkId,String nodeId,Node node,SecurityContext securityContext) throws NotFoundException;
}

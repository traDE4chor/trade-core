/*
 * Copyright 2017 Michael Hahn
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.swagger.trade.server.jersey.api;

import io.swagger.trade.server.jersey.api.*;
import io.swagger.trade.server.jersey.model.*;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;

import io.swagger.trade.server.jersey.model.DataValue;
import io.swagger.trade.server.jersey.model.DataValueRequest;
import io.swagger.trade.server.jersey.model.Error;
import io.swagger.trade.server.jersey.model.InvalidInput;
import io.swagger.trade.server.jersey.model.NotFound;

import java.util.List;
import io.swagger.trade.server.jersey.api.NotFoundException;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-01-31T17:07:23.956+01:00")
public abstract class DataValuesApiService {
    public abstract Response addDataValue(DataValueRequest body,SecurityContext securityContext,UriInfo uriInfo) throws NotFoundException;
    public abstract Response deleteDataValue(String dataValueId,SecurityContext securityContext) throws NotFoundException;    
    public abstract Response getDataValueDirectly(String dataValueId,SecurityContext securityContext,UriInfo uriInfo) throws NotFoundException;
    public abstract Response getDataValuesDirectly(Integer limit,String status,SecurityContext securityContext,UriInfo uriInfo) throws NotFoundException;
    public abstract Response pullDataValue(String dataValueId,SecurityContext securityContext) throws NotFoundException;
    public abstract Response pushDataValue(String dataValueId,Long contentLength,byte[] data,SecurityContext securityContext) throws NotFoundException;
    public abstract Response updateDataValueDirectly(String dataValueId,DataValueUpdateRequest dataValue,SecurityContext securityContext,UriInfo uriInfo) throws NotFoundException;
}

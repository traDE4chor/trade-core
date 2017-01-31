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

package io.swagger.trade.server.jersey.api.impl;

import de.slub.urn.URNSyntaxException;
import io.swagger.trade.server.jersey.api.ApiResponseMessage;
import io.swagger.trade.server.jersey.api.DataValuesApiService;
import io.swagger.trade.server.jersey.api.NotFoundException;
import io.swagger.trade.server.jersey.api.util.ResourceTransformationUtils;
import io.swagger.trade.server.jersey.model.DataValue;
import io.swagger.trade.server.jersey.model.DataValueRequest;
import org.trade.core.data.management.DataManager;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.net.URI;

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-01-31T17:07:23.956+01:00")
public class DataValuesApiServiceImpl extends DataValuesApiService {

    @Override
    public Response addDataValue(DataValueRequest body, SecurityContext securityContext, UriInfo uriInfo) throws NotFoundException {
        Response response = null;

        org.trade.core.model.data.DataValue value = null;
        try {
            value = DataManager.getInstance().registerDataValue
                    (ResourceTransformationUtils.resource2Model
                            (body));

            DataValue result = ResourceTransformationUtils.model2Resource(value);

            UriBuilder builder = uriInfo.getAbsolutePathBuilder();
            URI valueUri = builder.path(result.getId()).build();

            result.setHref(valueUri.toASCIIString());

            response = Response.created(valueUri).entity(result).build();
        } catch (URNSyntaxException e) {
            e.printStackTrace();

            response = Response.serverError().entity(e.getMessage()).build();
        }

        return response;
    }

    @Override
    public Response getDataValueDirectly(String dataValueId, SecurityContext securityContext, UriInfo uriInfo) throws NotFoundException {
        Response response = null;

        try {
            org.trade.core.model.data.DataValue value = DataManager.getInstance().getDataValue(dataValueId);

            DataValue result = ResourceTransformationUtils.model2Resource(value);

            UriBuilder builder = uriInfo.getAbsolutePathBuilder();
            URI valueUri = builder.path(result.getId()).build();

            result.setHref(valueUri.toASCIIString());

            response = Response.ok().entity(result).build();
        } catch (Exception e) {
            e.printStackTrace();

            response = Response.serverError().entity(e.getMessage()).build();
        }

        return response;
    }

    @Override
    public Response getDataValuesDirectly(Integer limit, String status, SecurityContext securityContext, UriInfo uriInfo) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override
    public Response pullDataValue(String dataValueId, SecurityContext securityContext, UriInfo uriInfo) throws NotFoundException {
        Response response = null;

        try {
            org.trade.core.model.data.DataValue value = DataManager.getInstance().getDataValue(dataValueId);

            response = Response.ok(value.getData(), value.getContentType()).header("Content-Length", value.getSize())
                    .build();
        } catch (Exception e) {
            e.printStackTrace();

            response = Response.serverError().entity(e.getMessage()).build();
        }

        // TODO: Handle data exchange with streams instead of byte[]
        // http://stackoverflow.com/questions/10326460/how-to-avoid-outofmemoryerror-when-uploading-a-large-file-using-jersey-client/31140433#31140433
        // http://stackoverflow.com/questions/23701106/how-jersey-2-client-can-send-input-output-binary-stream-to-server-and-vise-versa/23701359#23701359
        // http://stackoverflow.com/questions/10587561/password-protected-zip-file-in-java/32253028#32253028
        // http://stackoverflow.com/questions/3496209/input-and-output-binary-streams-using-jersey/28479669#28479669

        return response;
    }

    @Override
    public Response pushDataValue(String dataValueId, Long contentLength, byte[] data, SecurityContext securityContext, UriInfo uriInfo) throws NotFoundException {
        Response response = null;

        try {
            org.trade.core.model.data.DataValue value = DataManager.getInstance().getDataValue(dataValueId);

            value.setData(data, contentLength);

            response = Response.ok().build();
        } catch (Exception e) {
            e.printStackTrace();

            response = Response.serverError().entity(e.getMessage()).build();
        }

        return response;
    }

    @Override
    public Response updateDataValueDirectly(String dataValueId, DataValueRequest dataValue, SecurityContext securityContext, UriInfo uriInfo) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
}

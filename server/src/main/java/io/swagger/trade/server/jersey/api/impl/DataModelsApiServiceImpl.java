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

import io.swagger.trade.server.jersey.api.ApiResponseMessage;
import io.swagger.trade.server.jersey.api.DataModelsApiService;
import io.swagger.trade.server.jersey.api.NotFoundException;
import io.swagger.trade.server.jersey.model.DataModelData;
import org.trade.core.data.management.DataManager;
import org.trade.core.model.compiler.CompilationException;
import org.trade.core.model.compiler.CompilationIssue;
import org.trade.core.model.compiler.DDGCompiler;

import javax.validation.constraints.Min;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import java.util.List;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-04-04T17:08:04.791+02:00")
public class DataModelsApiServiceImpl extends DataModelsApiService {
    @Override
    public Response addDataModel(DataModelData dataModelData, SecurityContext securityContext, UriInfo uriInfo) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override
    public Response deleteDataModel(String dataModelId, SecurityContext securityContext, UriInfo uriInfo) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override
    public Response downloadDataModel(String dataModelId, SecurityContext securityContext, UriInfo uriInfo) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override
    public Response getDataModelDirectly(String dataModelId, SecurityContext securityContext, UriInfo uriInfo) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override
    public Response getDataModels(@Min(0) Integer start, @Min(0) Integer size, String name, SecurityContext securityContext, UriInfo uriInfo) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override
    public Response getDataObjects(String dataModelId, @Min(0) Integer start, @Min(0) Integer size, SecurityContext securityContext, UriInfo uriInfo) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override
    public Response uploadDataModel(String dataModelId, Long contentLength, byte[] model, SecurityContext securityContext, UriInfo uriInfo) throws NotFoundException {
        // do some magic!
        org.trade.core.model.data.DataModel dataModel = DataManager.getInstance().getDataModel(dataModelId);

        // Since we don't support the recompilation (updates) of data models, at the moment, this
        // method automatically invokes the compilation of the provided serialized data model.
        try {
            dataModel.setSerializedModel(model);

            // TODO: We need to add the list of issues as part of the response!
            List<CompilationIssue> issues = dataModel.compileDataModel(model);

            DataManager.getInstance().registerContentsOfDataModel(dataModelId);
        } catch (CompilationException e) {
            // TODO: We need some special response type that allows us to forward the list of CompilationIssue's!
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
}

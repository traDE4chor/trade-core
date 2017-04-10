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
import io.swagger.trade.server.jersey.api.DataDependencyGraphsApiService;
import io.swagger.trade.server.jersey.api.NotFoundException;
import io.swagger.trade.server.jersey.model.DataDependencyGraphData;
import io.swagger.trade.server.jersey.model.DataModel;
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
public class DataDependencyGraphsApiServiceImpl extends DataDependencyGraphsApiService {
    @Override
    public Response addDataDependencyGraph(DataDependencyGraphData dataDependencyGraphData, SecurityContext securityContext, UriInfo uriInfo) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override
    public Response deleteDataDependencyGraph(String graphId, SecurityContext securityContext, UriInfo uriInfo) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override
    public Response downloadGraphModel(String graphId, SecurityContext securityContext, UriInfo uriInfo) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override
    public Response getDataDependencyGraphDirectly(String graphId, SecurityContext securityContext, UriInfo uriInfo) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override
    public Response getDataDependencyGraphs(@Min(0) Integer start, @Min(0) Integer size, String name, SecurityContext securityContext, UriInfo uriInfo) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override
    public Response getDataModel(String graphId, SecurityContext securityContext, UriInfo uriInfo) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override
    public Response setDataModel(String graphId, DataModel dataModel, SecurityContext securityContext, UriInfo uriInfo) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override
    public Response uploadGraphModel(String graphId, Long contentLength, byte[] graph, SecurityContext securityContext, UriInfo uriInfo) throws NotFoundException {
        // do some magic!

        org.trade.core.model.data.DataDependencyGraph ddg = DataManager.getInstance().getDataDependencyGraph(graphId);

        // Since we don't support the recompilation (updates) of data dependency graphs, at the moment, this
        // method automatically invokes the compilation of the provided serialized data dependency graph.
        try {
            ddg.setSerializedModel(graph);

            // TODO: We need to add the list of issues as part of the response!
            List<CompilationIssue> issues = ddg.compileDataDependencyGraph(graph);

            DataManager.getInstance().registerContentsOfDataDependencyGraph(graphId);
        } catch (CompilationException e) {
            // TODO: We need some special response type that allows us to forward the list of CompilationIssue's!
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
}

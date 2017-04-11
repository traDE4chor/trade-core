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

import io.swagger.trade.server.jersey.api.DataDependencyGraphsApiService;
import io.swagger.trade.server.jersey.api.NotFoundException;
import io.swagger.trade.server.jersey.api.util.ResourceTransformationUtils;
import io.swagger.trade.server.jersey.model.*;
import org.trade.core.data.management.DataManager;
import org.trade.core.model.compiler.CompilationException;
import org.trade.core.model.compiler.CompilationIssue;

import javax.validation.constraints.Min;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.Collections;
import java.util.List;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-04-04T17:08:04.791+02:00")
public class DataDependencyGraphsApiServiceImpl extends DataDependencyGraphsApiService {
    @Override
    public Response addDataDependencyGraph(DataDependencyGraphData dataDependencyGraphData, SecurityContext securityContext, UriInfo uriInfo) throws NotFoundException {
        Response response = null;

        try {
            // Check if name is specified since this is a mandatory attribute because it can not be changed
            // after creation of the data dependency graph
            if (dataDependencyGraphData.getName() == null || dataDependencyGraphData.getName().isEmpty()) {
                response = Response.status(Response.Status.BAD_REQUEST).entity(new InvalidInput()
                        .message("The 'name' attribute in parameter 'body' is required but missing in the " +
                                "processed request.").example("{\n" +
                                "  \"name\": \"someName\",\n" +
                                "  \"entity\": \"someEntity\",\n" +
                                "}"))
                        .build();
            } else {
                org.trade.core.model.data.DataDependencyGraph graph = DataManager.getInstance().registerDataDependencyGraph(
                        (ResourceTransformationUtils.resource2Model
                                (dataDependencyGraphData)));

                DataDependencyGraphWithLinks result = new DataDependencyGraphWithLinks();

                result.setDataDependencyGraph(ResourceTransformationUtils.model2Resource(graph));

                UriBuilder builder = uriInfo.getAbsolutePathBuilder();
                URI valueUri = builder.path(result.getDataDependencyGraph().getId()).build();

                result.getDataDependencyGraph().setHref(valueUri.toASCIIString());

                response = Response.created(valueUri).entity(result).build();
            }
        } catch (Exception e) {
            e.printStackTrace();

            response = Response.serverError().entity(e.getMessage()).build();
        }

        return response;
    }

    @Override
    public Response deleteDataDependencyGraph(String graphId, SecurityContext securityContext, UriInfo uriInfo) throws NotFoundException {
        Response response = null;

        try {
            boolean exists = DataManager.getInstance().hasDataDependencyGraph(graphId);

            if (exists) {
                DataManager.getInstance().deleteDataDependencyGraph(graphId);

                response = Response.ok().build();
            } else {
                response = Response.status(Response.Status.NOT_FOUND).entity(new NotFound().properties(Collections
                        .singletonList(graphId)).message("A data dependency graph with id = '" + graphId + "' is " +
                        "not available."))
                        .build();
            }
        } catch (Exception e) {
            e.printStackTrace();

            response = Response.serverError().entity(e.getMessage()).build();
        }

        return response;
    }

    @Override
    public Response downloadGraphModel(String graphId, SecurityContext securityContext, UriInfo uriInfo) throws NotFoundException {
        Response response = null;

        try {
            org.trade.core.model.data.DataDependencyGraph graph = DataManager.getInstance().getDataDependencyGraph(graphId);

            if (graph != null) {
                response = Response.ok(graph.getSerializedModel()).build();
            } else {
                response = Response.status(Response.Status.NOT_FOUND).entity(new NotFound().properties(Collections
                        .singletonList(graphId)).message("A data dependency graph with id = '" + graphId + "' is " +
                        "not available."))
                        .build();
            }
        } catch (Exception e) {
            e.printStackTrace();

            response = Response.serverError().entity(e.getMessage()).build();
        }

        return response;
    }

    @Override
    public Response getDataDependencyGraphDirectly(String graphId, SecurityContext securityContext, UriInfo uriInfo) throws NotFoundException {
        Response response = null;

        org.trade.core.model.data.DataDependencyGraph graph = DataManager.getInstance().getDataDependencyGraph(graphId);

        try {
            if (graph != null) {
                DataDependencyGraphWithLinks result = new DataDependencyGraphWithLinks();

                result.setDataDependencyGraph(ResourceTransformationUtils.model2Resource(graph));

                // Set HREF and links to related resources
                UriBuilder builder = uriInfo.getAbsolutePathBuilder();
                URI valueUri = builder.build();

                result.getDataDependencyGraph().setHref(valueUri.toASCIIString());

                // TODO: Set links to related data element instances!?
                result.setLinks(LinkUtils.createDataDependencyGraphLinks(uriInfo));

                response = Response.ok().entity(result).build();
            } else {
                response = Response.status(Response.Status.NOT_FOUND).entity(new NotFound().properties(Collections
                        .singletonList(graphId)).message("A data dependency graph with id = '" + graphId + "' is " +
                        "not available."))
                        .build();
            }
        } catch (Exception e) {
            e.printStackTrace();

            response = Response.serverError().entity(e.getMessage()).build();
        }

        return response;
    }

    @Override
    public Response getDataDependencyGraphs(@Min(1) Integer start, @Min(1) Integer size, String targetNamespace, String
            name, String entity, SecurityContext securityContext, UriInfo uriInfo) throws NotFoundException {
        Response response = null;

        try {
            List<org.trade.core.model.data.DataDependencyGraph> dataDependencyGraphs = DataManager.getInstance()
                    .getAllDataDependencyGraphs
                            (targetNamespace, name, entity);
            int filteredListSize = dataDependencyGraphs.size();

            // Check if the start index and the size are in still the range of the filtered result list, if not
            // respond the whole filtered result list
            if (start > 0 && size > 0 && start <= dataDependencyGraphs.size()) {
                // Calculate the two index
                int toIndex = start - 1 + size;
                // Check if the index is still in bounds
                if (toIndex > dataDependencyGraphs.size()) {
                    toIndex = dataDependencyGraphs.size();
                }
                // Decrease start by one since the API starts counting indexes from 1
                dataDependencyGraphs = dataDependencyGraphs.subList(start - 1, toIndex);
            }

            DataDependencyGraphArrayWithLinks resultList = new DataDependencyGraphArrayWithLinks();
            resultList.setDataDependencyGraphs(new DataDependencyGraphArray());
            for (org.trade.core.model.data.DataDependencyGraph dataDependencyGraph : dataDependencyGraphs) {

                DataDependencyGraphWithLinks result = new DataDependencyGraphWithLinks();

                result.setDataDependencyGraph(ResourceTransformationUtils.model2Resource(dataDependencyGraph));

                // Set HREF and links to related resources
                UriBuilder builder = uriInfo.getAbsolutePathBuilder();
                URI valueUri = builder.path(result.getDataDependencyGraph().getId()).build();

                result.getDataDependencyGraph().setHref(valueUri.toASCIIString());

                // TODO: Set links to related data models!?
                result.setLinks(LinkUtils.createDataDependencyGraphLinks(uriInfo));

                resultList.getDataDependencyGraphs().add(result);
            }

            resultList.setLinks(LinkUtils.createPaginationLinks("data dependency graphs", uriInfo, start, size,
                    filteredListSize));

            response = Response.ok().entity(resultList).build();
        } catch (Exception e) {
            e.printStackTrace();

            response = Response.serverError().entity(e.getMessage()).build();
        }

        return response;
    }

    @Override
    public Response getDataModel(String graphId, SecurityContext securityContext, UriInfo uriInfo) throws NotFoundException {
        Response response = null;

        boolean exists = DataManager.getInstance().hasDataDependencyGraph(graphId);

        if (exists) {
            org.trade.core.model.data.DataModel model = DataManager.getInstance().getDataModelOfGraphWithId
                    (graphId);

            try {
                if (model != null) {
                    DataModelWithLinks result = new DataModelWithLinks();

                    result.setDataModel(ResourceTransformationUtils.model2Resource(model));

                    // Set HREF and links to related resources
                    UriBuilder builder = uriInfo.getAbsolutePathBuilder();
                    URI valueUri = builder.build();

                    result.getDataModel().setHref(valueUri.toASCIIString());

                    // TODO: Set links to related data objects!?
                    result.setLinks(LinkUtils.createDataDependencyGraphLinks(uriInfo));

                    response = Response.ok().entity(result).build();
                } else {
                    response = Response.status(Response.Status.NOT_FOUND).entity(new NotFound().properties(Collections
                            .singletonList(graphId)).message("A data model for data dependency graph with id = '" +
                            graphId + "' is not available.")).build();
                }
            } catch (Exception e) {
                e.printStackTrace();

                response = Response.serverError().entity(e.getMessage()).build();
            }
        } else {
            response = Response.status(Response.Status.NOT_FOUND).entity(new NotFound().properties(Collections
                    .singletonList(graphId)).message("A data dependency graph with id = '" + graphId + "' is " +
                    "not available."))
                    .build();
        }

        return response;
    }

    @Override
    public Response setDataModel(String graphId, String dataModelId, SecurityContext securityContext, UriInfo uriInfo)
            throws NotFoundException {
        Response response = null;

        boolean exists = DataManager.getInstance().hasDataDependencyGraph(graphId);

        if (exists) {

            exists = DataManager.getInstance().hasDataModel(dataModelId);
            if (exists) {
                org.trade.core.model.data.DataDependencyGraph graph = DataManager.getInstance().getDataDependencyGraph(graphId);
                org.trade.core.model.data.DataModel model = DataManager.getInstance().getDataModel(dataModelId);

                try {
                    // Set the data model to the graph
                    graph.setDataModel(model);
                } catch (Exception e) {
                    e.printStackTrace();

                    response = Response.serverError().entity(e.getMessage()).build();
                }
            } else {
                response = Response.status(Response.Status.NOT_FOUND).entity(new NotFound().properties(Collections
                        .singletonList(graphId)).message("A data model with id = '" + dataModelId + "' is " +
                        "not available."))
                        .build();
            }
        } else {
            response = Response.status(Response.Status.NOT_FOUND).entity(new NotFound().properties(Collections
                    .singletonList(graphId)).message("A data dependency graph with id = '" + graphId + "' is " +
                    "not available."))
                    .build();
        }

        return response;
    }

    @Override
    public Response uploadGraphModel(String graphId, Long contentLength, byte[] graph, SecurityContext securityContext, UriInfo uriInfo) throws NotFoundException {
        Response response = null;

        boolean exists = DataManager.getInstance().hasDataDependencyGraph(graphId);

        if (exists) {
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

                response = Response.serverError().entity(e.getMessage()).build();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            response = Response.status(Response.Status.NOT_FOUND).entity(new NotFound().properties(Collections
                    .singletonList(graphId)).message("A data dependency graph with id = '" + graphId + "' is " +
                    "not available."))
                    .build();
        }

        return response;
    }
}

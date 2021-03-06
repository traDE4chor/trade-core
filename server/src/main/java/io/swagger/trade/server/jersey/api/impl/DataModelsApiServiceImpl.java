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

import io.swagger.trade.server.jersey.api.DataModelsApiService;
import io.swagger.trade.server.jersey.api.NotFoundException;
import io.swagger.trade.server.jersey.api.util.ResourceTransformationUtils;
import io.swagger.trade.server.jersey.model.*;
import org.trade.core.data.management.DataManagerFactory;
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
public class DataModelsApiServiceImpl extends DataModelsApiService {
    @Override
    public Response addDataModel(DataModelData dataModelData, SecurityContext securityContext, UriInfo uriInfo) throws NotFoundException {
        Response response = null;

        try {
            // Check if name is specified since this is a mandatory attribute because it can not be changed
            // after creation of the data model
            if (dataModelData.getName() == null || dataModelData.getName().isEmpty()) {
                response = Response.status(Response.Status.BAD_REQUEST).entity(new InvalidInput()
                        .message("The 'name' attribute in parameter 'body' is required but missing in the " +
                                "processed request.").example("{\n" +
                                "  \"name\": \"someName\",\n" +
                                "  \"entity\": \"someEntity\",\n" +
                                "}"))
                        .build();
            } else {
                org.trade.core.model.data.DataModel graph = DataManagerFactory.createDataManager().registerDataModel(
                        (ResourceTransformationUtils.resource2Model
                                (dataModelData)));

                DataModel result = ResourceTransformationUtils.model2Resource(graph);

                UriBuilder builder = uriInfo.getAbsolutePathBuilder();
                URI valueUri = builder.path(result.getId()).build();

                result.setHref(valueUri.toASCIIString());

                response = Response.created(valueUri).entity(result).build();
            }
        } catch (Exception e) {
            e.printStackTrace();

            response = Response.serverError().entity(e.getMessage()).build();
        }

        return response;
    }

    @Override
    public Response deleteDataModel(String dataModelId, SecurityContext securityContext, UriInfo uriInfo) throws NotFoundException {
        Response response = null;

        try {
            boolean exists = DataManagerFactory.createDataManager().hasDataModel(dataModelId);

            if (exists) {
                DataManagerFactory.createDataManager().deleteDataModel(dataModelId);

                response = Response.status(Response.Status.NO_CONTENT).build();
            } else {
                response = Response.status(Response.Status.NOT_FOUND).entity(new NotFound().properties(Collections
                        .singletonList(dataModelId)).message("A data model with id = '" + dataModelId + "' is " +
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
    public Response downloadDataModel(String dataModelId, SecurityContext securityContext, UriInfo uriInfo) throws NotFoundException {
        Response response = null;

        try {
            org.trade.core.model.data.DataModel model = DataManagerFactory.createDataManager().getDataModel(dataModelId);

            if (model != null) {
                response = Response.ok(model.getSerializedModel()).build();
            } else {
                response = Response.status(Response.Status.NOT_FOUND).entity(new NotFound().properties(Collections
                        .singletonList(dataModelId)).message("A data model with id = '" + dataModelId + "' is " +
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
    public Response getDataModelDirectly(String dataModelId, SecurityContext securityContext, UriInfo uriInfo) throws NotFoundException {
        Response response = null;

        org.trade.core.model.data.DataModel model = DataManagerFactory.createDataManager().getDataModel(dataModelId);

        try {
            if (model != null) {
                DataModelWithLinks result = new DataModelWithLinks();

                result.setDataModel(ResourceTransformationUtils.model2Resource(model));

                // Set HREF and links to related resources
                UriBuilder builder = uriInfo.getAbsolutePathBuilder();
                URI valueUri = builder.build();

                result.getDataModel().setHref(valueUri.toASCIIString());

                // Set links to related resources
                result.setLinks(LinkUtils.createDataModelLinks(uriInfo, model, result
                        .getDataModel().getHref()));

                response = Response.ok().entity(result).build();
            } else {
                response = Response.status(Response.Status.NOT_FOUND).entity(new NotFound().properties(Collections
                        .singletonList(dataModelId)).message("A data dependency graph with id = '" + dataModelId + "' is " +
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
    public Response getDataModels(@Min(1) Integer start, @Min(1) Integer size, String targetNamespace, String name,
                                  String entity,
                                  SecurityContext securityContext, UriInfo uriInfo) throws NotFoundException {
        Response response = null;

        try {
            List<org.trade.core.model.data.DataModel> dataModels = DataManagerFactory.createDataManager()
                    .getAllDataModels
                            (targetNamespace, name, entity);
            int filteredListSize = dataModels.size();

            // Check if the start index and the size are in still the range of the filtered result list, if not
            // respond the whole filtered result list
            if (start > 0 && size > 0 && start <= dataModels.size()) {
                // Calculate the two index
                int toIndex = start - 1 + size;
                // Check if the index is still in bounds
                if (toIndex > dataModels.size()) {
                    toIndex = dataModels.size();
                }
                // Decrease start by one since the API starts counting indexes from 1
                dataModels = dataModels.subList(start - 1, toIndex);
            }

            DataModelArrayWithLinks resultList = new DataModelArrayWithLinks();
            resultList.setDataModels(new DataModelArray());
            for (org.trade.core.model.data.DataModel dataModel : dataModels) {

                DataModelWithLinks result = new DataModelWithLinks();

                result.setDataModel(ResourceTransformationUtils.model2Resource(dataModel));

                // Set HREF and links to related resources
                UriBuilder builder = uriInfo.getAbsolutePathBuilder();
                URI valueUri = builder.path(result.getDataModel().getId()).build();

                result.getDataModel().setHref(valueUri.toASCIIString());

                // Set links to related resources
                result.setLinks(LinkUtils.createDataModelLinks(uriInfo, dataModel, result
                        .getDataModel().getHref()));

                resultList.getDataModels().add(result);
            }

            resultList.setLinks(LinkUtils.createPaginationLinks("data models", uriInfo, start, size,
                    filteredListSize));

            response = Response.ok().entity(resultList).build();
        } catch (Exception e) {
            e.printStackTrace();

            response = Response.serverError().entity(e.getMessage()).build();
        }

        return response;
    }

    @Override
    public Response getDataObjects(String dataModelId, @Min(1) Integer start, @Min(1) Integer size, SecurityContext securityContext, UriInfo uriInfo) throws NotFoundException {
        Response response = null;

        boolean exists = DataManagerFactory.createDataManager().hasDataModel(dataModelId);

        if (exists) {
            try {
                List<org.trade.core.model.data.DataObject> dataObjects = DataManagerFactory.createDataManager()
                        .getAllDataObjectsOfDataModel(dataModelId);
                int filteredListSize = dataObjects.size();

                // Check if the start index and the size are in still the range of the filtered result list, if not
                // respond the whole filtered result list
                if (start > 0 && size > 0 && start <= dataObjects.size()) {
                    // Calculate the two index
                    int toIndex = start - 1 + size;
                    // Check if the index is still in bounds
                    if (toIndex > dataObjects.size()) {
                        toIndex = dataObjects.size();
                    }
                    // Decrease start by one since the API starts counting indexes from 1
                    dataObjects = dataObjects.subList(start - 1, toIndex);
                }

                DataObjectArrayWithLinks resultList = new DataObjectArrayWithLinks();
                resultList.setDataObjects(new DataObjectArray());
                for (org.trade.core.model.data.DataObject dataObject : dataObjects) {

                    DataObjectWithLinks result = new DataObjectWithLinks();

                    result.setDataObject(ResourceTransformationUtils.model2Resource(dataObject));

                    // Set HREF and links to related resources
                    result.getDataObject().setHref(uriInfo.getBaseUriBuilder().path(LinkUtils
                            .TEMPLATE_COLLECTION_RESOURCE).build(LinkUtils.COLLECTION_DATA_OBJECT, dataObject
                            .getIdentifier()).toASCIIString());

                    // Set links to related data elements, etc.
                    result.setLinks(LinkUtils.createDataObjectLinks(uriInfo, dataObject, result.getDataObject().getHref()));

                    resultList.getDataObjects().add(result);
                }

                resultList.setLinks(LinkUtils.createPaginationLinks("data objects", uriInfo, start, size,
                        filteredListSize));

                response = Response.ok().entity(resultList).build();
            } catch (Exception e) {
                e.printStackTrace();

                response = Response.serverError().entity(e.getMessage()).build();
            }
        } else {
            response = Response.status(Response.Status.NOT_FOUND).entity(new NotFound().properties(Collections
                    .singletonList(dataModelId)).message("A data model with id = '" + dataModelId + "' is " +
                    "not available."))
                    .build();
        }

        return response;
    }

    @Override
    public Response uploadDataModel(String dataModelId, Long contentLength, byte[] model, SecurityContext securityContext, UriInfo uriInfo) throws NotFoundException {
        Response response = null;

        boolean exists = DataManagerFactory.createDataManager().hasDataModel(dataModelId);
        if (exists) {
            // Since we don't support the recompilation (updates) of data models, at the moment, this
            // method automatically invokes the compilation of the provided serialized data model.
            try {
                DataManagerFactory.createDataManager().setSerializedModelOfDataModel(dataModelId, model);

                // TODO: We need to add the list of issues as part of the response!
                List<CompilationIssue> issues = DataManagerFactory.createDataManager().compileDataModel(dataModelId, model);
            } catch (CompilationException e) {
                // TODO: We need some special response type that allows us to forward the list of CompilationIssue's!
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            response = Response.status(Response.Status.NOT_FOUND).entity(new NotFound().properties(Collections
                    .singletonList(dataModelId)).message("A data model with id = '" + dataModelId + "' is " +
                    "not available."))
                    .build();
        }

        return response;
    }
}

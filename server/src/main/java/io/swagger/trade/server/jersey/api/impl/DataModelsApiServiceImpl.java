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
    public Response getDataModels( @Min(1) Integer start,  @Min(1) Integer size,  String targetNamespace,  String name, SecurityContext securityContext, UriInfo uriInfo) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override
    public Response getDataObjects(String dataModelId,  @Min(1) Integer start,  @Min(1) Integer size, SecurityContext securityContext, UriInfo uriInfo) throws NotFoundException {
        Response response = null;

        boolean exists = DataManager.getInstance().hasDataModel(dataModelId);

        if (exists) {
        try {
            List<org.trade.core.model.data.DataObject> dataObjects = DataManager.getInstance()
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
                UriBuilder builder = uriInfo.getAbsolutePathBuilder();
                URI valueUri = builder.path(result.getDataObject().getId()).build();

                result.getDataObject().setHref(valueUri.toASCIIString());

                // TODO: Set links to related data elements, etc.!?
                result.setLinks(LinkUtils.createDataObjectLinks(uriInfo));

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

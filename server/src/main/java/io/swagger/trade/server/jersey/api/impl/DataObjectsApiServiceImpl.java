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
import io.swagger.trade.server.jersey.api.DataObjectsApiService;
import io.swagger.trade.server.jersey.api.NotFoundException;
import io.swagger.trade.server.jersey.api.util.ResourceTransformationUtils;
import io.swagger.trade.server.jersey.model.*;
import io.swagger.trade.server.jersey.model.DataObject;
import org.trade.core.data.management.DataManager;
import org.trade.core.model.data.*;

import javax.validation.constraints.Min;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.Collections;
import java.util.List;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-04-04T17:08:04.791+02:00")
public class DataObjectsApiServiceImpl extends DataObjectsApiService {

    @Override
    public Response addDataObject(DataObjectData dataObjectData, SecurityContext securityContext, UriInfo uriInfo) throws NotFoundException {
        Response response = null;

        try {
            // Check if name is specified since this is a mandatory attribute because it can not be changed
            // after creation of the data object
            if (dataObjectData.getName() == null || dataObjectData.getName().isEmpty()) {
                response = Response.status(Response.Status.BAD_REQUEST).entity(new InvalidInput()
                        .message("The 'name' attribute in parameter 'body' is required but missing in the " +
                                "processed request.").example("{\n" +
                                "  \"name\": \"someName\",\n" +
                                "  \"entity\": \"someEntity\",\n" +
                                "}"))
                        .build();
            } else {
                org.trade.core.model.data.DataObject dataObject = DataManager.INSTANCE.registerDataObject(
                        (ResourceTransformationUtils.resource2Model
                                (dataObjectData)));

                DataObject result = new DataObject();

                result = ResourceTransformationUtils.model2Resource(dataObject);

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
    public Response deleteDataObject(String dataObjectId, SecurityContext securityContext, UriInfo uriInfo) throws NotFoundException {
        Response response = null;

        try {
            boolean exists = DataManager.INSTANCE.hasDataObject(dataObjectId);

            if (exists) {
                DataManager.INSTANCE.deleteDataObject(dataObjectId);

                response = Response.ok().build();
            } else {
                response = Response.status(Response.Status.NOT_FOUND).entity(new NotFound().properties(Collections
                        .singletonList(dataObjectId)).message("A data object with id = '" + dataObjectId + "' " +
                        "is not available."))
                        .build();
            }
        } catch (Exception e) {
            e.printStackTrace();

            response = Response.serverError().entity(e.getMessage()).build();
        }

        return response;
    }
    @Override
    public Response getAllDataObjects( @Min(1) Integer start,  @Min(1) Integer size,  String name, String entity,
                                       String
                                       status, SecurityContext securityContext, UriInfo uriInfo) throws NotFoundException {
        Response response = null;

        try {
            List<org.trade.core.model.data.DataObject> dataObjects = DataManager.INSTANCE
                    .getAllDataObjects
                            (name, entity, status);
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

                // Set links to related resources
                result.setLinks(LinkUtils.createDataObjectLinks(uriInfo, dataObject, result
                        .getDataObject().getHref()));

                resultList.getDataObjects().add(result);
            }

            resultList.setLinks(LinkUtils.createPaginationLinks("data objects", uriInfo, start, size,
                    filteredListSize));

            response = Response.ok().entity(resultList).build();
        } catch (Exception e) {
            e.printStackTrace();

            response = Response.serverError().entity(e.getMessage()).build();
        }

        return response;
    }
    @Override
    public Response getDataElements(String dataObjectId,  @Min(1) Integer start,  @Min(1) Integer size,  String name,  String status, SecurityContext securityContext, UriInfo uriInfo) throws NotFoundException {
        Response response = null;

        boolean exists = DataManager.INSTANCE.hasDataObject(dataObjectId);

        if (exists) {
            try {
                List<org.trade.core.model.data.DataElement> dataElements = DataManager.INSTANCE
                        .getAllDataElementsOfDataObject(dataObjectId);
                int filteredListSize = dataElements.size();

                // Check if the start index and the size are in still the range of the filtered result list, if not
                // respond the whole filtered result list
                if (start > 0 && size > 0 && start <= dataElements.size()) {
                    // Calculate the two index
                    int toIndex = start - 1 + size;
                    // Check if the index is still in bounds
                    if (toIndex > dataElements.size()) {
                        toIndex = dataElements.size();
                    }
                    // Decrease start by one since the API starts counting indexes from 1
                    dataElements = dataElements.subList(start - 1, toIndex);
                }

                DataElementArrayWithLinks resultList = new DataElementArrayWithLinks();
                resultList.setDataElements(new DataElementArray());
                for (org.trade.core.model.data.DataElement dataElement : dataElements) {

                    DataElementWithLinks result = new DataElementWithLinks();

                    result.setDataElement(ResourceTransformationUtils.model2Resource(dataElement));

                    // Set HREF and links to related resources
                    result.getDataElement().setHref(uriInfo.getBaseUriBuilder().path(LinkUtils
                            .TEMPLATE_COLLECTION_RESOURCE).build(LinkUtils.COLLECTION_DATA_ELEMENT, dataElement
                            .getIdentifier()).toASCIIString());

                    // Set links to related data elements, etc.
                    result.setLinks(LinkUtils.createDataElementLinks(uriInfo, dataElement, result.getDataElement()
                            .getHref()));

                    resultList.getDataElements().add(result);
                }

                resultList.setLinks(LinkUtils.createPaginationLinks("data elements", uriInfo, start, size,
                        filteredListSize));

                response = Response.ok().entity(resultList).build();
            } catch (Exception e) {
                e.printStackTrace();

                response = Response.serverError().entity(e.getMessage()).build();
            }
        } else {
            response = Response.status(Response.Status.NOT_FOUND).entity(new NotFound().properties(Collections
                    .singletonList(dataObjectId)).message("A data object with id = '" + dataObjectId + "' is " +
                    "not available."))
                    .build();
        }

        return response;
    }

    @Override
    public Response addDataElement(String dataObjectId, DataElementData dataElementData, SecurityContext securityContext, UriInfo uriInfo) throws NotFoundException {
        Response response = null;

        try {
            boolean exists = DataManager.INSTANCE.hasDataObject(dataObjectId);

            if (exists) {
                org.trade.core.model.data.DataElement dataElement = DataManager.INSTANCE
                        .addDataElementToDataObject(
                                dataObjectId, dataElementData.getEntity(), dataElementData.getName(), dataElementData
                                        .getContentType(), dataElementData.getType());

                DataElementWithLinks result = new DataElementWithLinks();

                result.setDataElement(ResourceTransformationUtils.model2Resource(dataElement));

                // Set HREF and links to related resources
                UriBuilder builder = uriInfo.getAbsolutePathBuilder();
                URI valueUri = builder.build();

                result.getDataElement().setHref(valueUri.toASCIIString());

                // Set links to realted data element instances
                result.setLinks(LinkUtils.createDataElementLinks(uriInfo, dataElement, result.getDataElement().getHref
                        ()));

                response = Response.ok().entity(result).build();
            } else {
                response = Response.status(Response.Status.NOT_FOUND).entity(new NotFound().properties(Collections
                        .singletonList(dataObjectId)).message("A data object with id='" + dataObjectId + "' is " +
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
    public Response getDataObjectById(String dataObjectId, SecurityContext securityContext, UriInfo uriInfo) throws NotFoundException {
        Response response = null;

        org.trade.core.model.data.DataObject dataObject = DataManager.INSTANCE.getDataObject(dataObjectId);

        try {
            if (dataObject != null) {
                DataObjectWithLinks result = new DataObjectWithLinks();

                result.setDataObject(ResourceTransformationUtils.model2Resource(dataObject));

                // Set HREF and links to related resources
                UriBuilder builder = uriInfo.getAbsolutePathBuilder();
                URI valueUri = builder.build();

                result.getDataObject().setHref(valueUri.toASCIIString());

                // Set links to related resources
                result.setLinks(LinkUtils.createDataObjectLinks(uriInfo, dataObject, result
                        .getDataObject().getHref()));

                response = Response.ok().entity(result).build();
            } else {
                response = Response.status(Response.Status.NOT_FOUND).entity(new NotFound().properties(Collections
                        .singletonList(dataObjectId)).message("A data object with id = '" + dataObjectId + "' is " +
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
    public Response addDataObjectInstance(String dataObjectId, DataObjectInstanceData dataObjectInstanceData, SecurityContext securityContext, UriInfo uriInfo) throws NotFoundException {
        Response response = null;

        try {
            boolean exists = DataManager.INSTANCE.hasDataObject(dataObjectId);

            if (exists) {
                org.trade.core.model.data.instance.DataObjectInstance dataObjectInstance = DataManager.INSTANCE
                        .instantiateDataObject(
                                dataObjectId, dataObjectInstanceData.getCreatedBy(), ResourceTransformationUtils.resource2Model(dataObjectInstanceData
                .getCorrelationProperties()));

                DataObjectInstanceWithLinks result = new DataObjectInstanceWithLinks();

                result.setInstance(ResourceTransformationUtils.model2Resource(dataObjectInstance));

                // Set HREF and links to related resources
                UriBuilder builder = uriInfo.getAbsolutePathBuilder();
                URI valueUri = builder.build();

                result.getInstance().setHref(valueUri.toASCIIString());

                // Set links to realted data element instances
                result.setLinks(LinkUtils.createDataObjectInstanceLinks(uriInfo, dataObjectInstance, result
                        .getInstance().getHref()));

                response = Response.ok().entity(result).build();
            } else {
                response = Response.status(Response.Status.NOT_FOUND).entity(new NotFound().properties(Collections
                        .singletonList(dataObjectId)).message("A data object with id='" + dataObjectId + "' is " +
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
    public Response getDataObjectInstances(String dataObjectId,  @Min(1) Integer start,  @Min(1) Integer size,  String status, SecurityContext securityContext, UriInfo uriInfo) throws NotFoundException {
        Response response = null;

        boolean exists = DataManager.INSTANCE.hasDataObject(dataObjectId);

        if (exists) {
            try {
                List<org.trade.core.model.data.instance.DataObjectInstance> dataObjectInstances = DataManager.INSTANCE
                        .getAllDataObjectInstancesOfDataObject(dataObjectId);
                int filteredListSize = dataObjectInstances.size();

                // Check if the start index and the size are in still the range of the filtered result list, if not
                // respond the whole filtered result list
                if (start > 0 && size > 0 && start <= dataObjectInstances.size()) {
                    // Calculate the two index
                    int toIndex = start - 1 + size;
                    // Check if the index is still in bounds
                    if (toIndex > dataObjectInstances.size()) {
                        toIndex = dataObjectInstances.size();
                    }
                    // Decrease start by one since the API starts counting indexes from 1
                    dataObjectInstances = dataObjectInstances.subList(start - 1, toIndex);
                }

                DataObjectInstanceArrayWithLinks resultList = new DataObjectInstanceArrayWithLinks();
                resultList.setInstances(new DataObjectInstanceArray());
                for (org.trade.core.model.data.instance.DataObjectInstance dataObjectInstance : dataObjectInstances) {

                    DataObjectInstanceWithLinks result = new DataObjectInstanceWithLinks();

                    result.setInstance(ResourceTransformationUtils.model2Resource(dataObjectInstance));

                    // Set HREF and links to related resources
                    result.getInstance().setHref(uriInfo.getBaseUriBuilder().path(LinkUtils
                            .TEMPLATE_COLLECTION_RESOURCE).build(LinkUtils.COLLECTION_DATA_OBJECT_INSTANCE, dataObjectInstance
                            .getIdentifier()).toASCIIString());

                    // Set links to related data elements, etc.
                    result.setLinks(LinkUtils.createDataObjectInstanceLinks(uriInfo, dataObjectInstance, result
                            .getInstance().getHref()));

                    resultList.getInstances().add(result);
                }

                resultList.setLinks(LinkUtils.createPaginationLinks("data object instances", uriInfo, start, size,
                        filteredListSize));

                response = Response.ok().entity(resultList).build();
            } catch (Exception e) {
                e.printStackTrace();

                response = Response.serverError().entity(e.getMessage()).build();
            }
        } else {
            response = Response.status(Response.Status.NOT_FOUND).entity(new NotFound().properties(Collections
                    .singletonList(dataObjectId)).message("A data object with id='" + dataObjectId + "' is " +
                    "not available."))
                    .build();
        }

        return response;
    }
    @Override
    public Response updateDataObject(String dataObjectId, DataObject dataObject, SecurityContext securityContext, UriInfo uriInfo) throws NotFoundException {
        Response response = null;

        try {
            boolean exists = DataManager.INSTANCE.hasDataValue(dataObjectId);

            if (exists) {
                org.trade.core.model.data.DataObject value = DataManager.INSTANCE.updateDataObject(
                        dataObjectId, dataObject.getName(), dataObject.getEntity());

                DataObjectWithLinks result = new DataObjectWithLinks();

                result.setDataObject(ResourceTransformationUtils.model2Resource(value));

                // Set HREF and links to related resources
                UriBuilder builder = uriInfo.getAbsolutePathBuilder();
                URI valueUri = builder.build();

                result.getDataObject().setHref(valueUri.toASCIIString());

                // Set links to related resources
                result.setLinks(LinkUtils.createDataObjectLinks(uriInfo, value, result.getDataObject().getHref()));

                response = Response.ok().entity(result).build();
            } else {
                response = Response.status(Response.Status.NOT_FOUND).entity(new NotFound().properties(Collections
                        .singletonList(dataObjectId)).message("A data object with id='" + dataObjectId + "' is " +
                        "not available."))
                        .build();
            }
        } catch (Exception e) {
            e.printStackTrace();

            response = Response.serverError().entity(e.getMessage()).build();
        }

        return response;
    }
}

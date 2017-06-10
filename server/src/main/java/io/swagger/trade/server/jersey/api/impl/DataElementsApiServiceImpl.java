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

import io.swagger.trade.server.jersey.api.DataElementsApiService;
import io.swagger.trade.server.jersey.api.NotFoundException;
import io.swagger.trade.server.jersey.api.util.ResourceTransformationUtils;
import io.swagger.trade.server.jersey.model.*;
import org.trade.core.data.management.DataManagerFactory;

import javax.validation.constraints.Min;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.Collections;
import java.util.List;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-04-04T17:08:04.791+02:00")
public class DataElementsApiServiceImpl extends DataElementsApiService {
    @Override
    public Response deleteDataElement(String dataElementId, SecurityContext securityContext, UriInfo uriInfo) throws NotFoundException {
        Response response = null;

        try {
            boolean exists = DataManagerFactory.createDataManager().hasDataElement(dataElementId);

            if (exists) {
                DataManagerFactory.createDataManager().deleteDataElement(dataElementId);

                response = Response.status(Response.Status.NO_CONTENT).build();
            } else {
                response = Response.status(Response.Status.NOT_FOUND).entity(new NotFound().properties(Collections
                        .singletonList(dataElementId)).message("A data element with id = '" + dataElementId + "' " +
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
    public Response getAllDataElements( @Min(1) Integer start,  @Min(1) Integer size,  String name, String status, SecurityContext securityContext, UriInfo uriInfo) throws NotFoundException {
        Response response = null;

        try {
            List<org.trade.core.model.data.DataElement> dataElements = DataManagerFactory.createDataManager()
                    .getAllDataElements
                            (name, status);
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
                UriBuilder builder = uriInfo.getAbsolutePathBuilder();
                URI valueUri = builder.path(result.getDataElement().getId()).build();

                result.getDataElement().setHref(valueUri.toASCIIString());

                // Set links to related resources
                result.setLinks(LinkUtils.createDataElementLinks(uriInfo, dataElement, result
                        .getDataElement().getHref()));

                resultList.getDataElements().add(result);
            }

            resultList.setLinks(LinkUtils.createPaginationLinks("data elements", uriInfo, start, size,
                    filteredListSize));

            response = Response.ok().entity(resultList).build();
        } catch (Exception e) {
            e.printStackTrace();

            response = Response.serverError().entity(e.getMessage()).build();
        }

        return response;
    }
    @Override
    public Response getDataElementDirectly(String dataElementId, SecurityContext securityContext, UriInfo uriInfo) throws NotFoundException {
        Response response = null;

        org.trade.core.model.data.DataElement dataElement = DataManagerFactory.createDataManager().getDataElement(dataElementId);

        try {
            if (dataElement != null) {
                DataElementWithLinks result = new DataElementWithLinks();

                result.setDataElement(ResourceTransformationUtils.model2Resource(dataElement));

                // Set HREF and links to related resources
                UriBuilder builder = uriInfo.getAbsolutePathBuilder();
                URI valueUri = builder.build();

                result.getDataElement().setHref(valueUri.toASCIIString());

                // Set links to related resources
                result.setLinks(LinkUtils.createDataElementLinks(uriInfo, dataElement, result
                        .getDataElement().getHref()));

                response = Response.ok().entity(result).build();
            } else {
                response = Response.status(Response.Status.NOT_FOUND).entity(new NotFound().properties(Collections
                        .singletonList(dataElementId)).message("A data element with id = '" + dataElementId + "' is " +
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
    public Response getDataElementInstancesOfDataElement(String dataElementId,  @Min(1) Integer start,  @Min(1) Integer size,  String status, SecurityContext securityContext, UriInfo uriInfo) throws NotFoundException {
        Response response = null;

        boolean exists = DataManagerFactory.createDataManager().hasDataElement(dataElementId);

        if (exists) {
            try {
                List<org.trade.core.model.data.instance.DataElementInstance> dataElementInstances = DataManagerFactory.createDataManager()
                        .getAllDataElementInstancesOfDataElement(dataElementId);
                int filteredListSize = dataElementInstances.size();

                // Check if the start index and the size are in still the range of the filtered result list, if not
                // respond the whole filtered result list
                if (start > 0 && size > 0 && start <= dataElementInstances.size()) {
                    // Calculate the two index
                    int toIndex = start - 1 + size;
                    // Check if the index is still in bounds
                    if (toIndex > dataElementInstances.size()) {
                        toIndex = dataElementInstances.size();
                    }
                    // Decrease start by one since the API starts counting indexes from 1
                    dataElementInstances = dataElementInstances.subList(start - 1, toIndex);
                }

                DataElementInstanceArrayWithLinks resultList = new DataElementInstanceArrayWithLinks();
                resultList.setInstances(new DataElementInstanceArray());
                for (org.trade.core.model.data.instance.DataElementInstance dataElementInstance : dataElementInstances) {

                    DataElementInstanceWithLinks result = new DataElementInstanceWithLinks();

                    result.setInstance(ResourceTransformationUtils.model2Resource(dataElementInstance));

                    // Set HREF and links to related resources
                    result.getInstance().setHref(uriInfo.getBaseUriBuilder().path(LinkUtils
                            .TEMPLATE_COLLECTION_RESOURCE).build(LinkUtils.COLLECTION_DATA_ELEMENT_INSTANCE, dataElementInstance
                            .getIdentifier()).toASCIIString());

                    // Set links to related data elements, etc.
                    result.setLinks(LinkUtils.createDataElementInstanceLinks(uriInfo, dataElementInstance, result
                            .getInstance().getHref()));

                    resultList.getInstances().add(result);
                }

                resultList.setLinks(LinkUtils.createPaginationLinks("data element instances", uriInfo, start, size,
                        filteredListSize));

                response = Response.ok().entity(resultList).build();
            } catch (Exception e) {
                e.printStackTrace();

                response = Response.serverError().entity(e.getMessage()).build();
            }
        } else {
            response = Response.status(Response.Status.NOT_FOUND).entity(new NotFound().properties(Collections
                    .singletonList(dataElementId)).message("A data element with id='" + dataElementId + "' is " +
                    "not available."))
                    .build();
        }

        return response;
    }

    @Override
    public Response updateDataElement(String dataElementId, DataElement dataElement, SecurityContext securityContext, UriInfo uriInfo) throws NotFoundException {
        Response response = null;

        try {
            boolean exists = DataManagerFactory.createDataManager().hasDataElement(dataElementId);

            if (exists) {
                org.trade.core.model.data.DataElement value = DataManagerFactory.createDataManager().updateDataElement(
                        dataElementId, dataElement.getName(), dataElement.getType(), dataElement.getContentType());

                DataElementWithLinks result = new DataElementWithLinks();

                result.setDataElement(ResourceTransformationUtils.model2Resource(value));

                // Set HREF and links to related resources
                UriBuilder builder = uriInfo.getAbsolutePathBuilder();
                URI valueUri = builder.build();

                result.getDataElement().setHref(valueUri.toASCIIString());

                // Set links to related resources
                result.setLinks(LinkUtils.createDataElementLinks(uriInfo, value, result.getDataElement().getHref()));

                response = Response.ok().entity(result).build();
            } else {
                response = Response.status(Response.Status.NOT_FOUND).entity(new NotFound().properties(Collections
                        .singletonList(dataElementId)).message("A data element with id='" + dataElementId + "' is " +
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

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

import io.swagger.trade.server.jersey.api.NotFoundException;
import io.swagger.trade.server.jersey.api.ResourceEventFiltersApiService;
import io.swagger.trade.server.jersey.api.util.ResourceTransformationUtils;
import io.swagger.trade.server.jersey.model.NotFound;
import io.swagger.trade.server.jersey.model.ResourceEventFilterDescription;
import io.swagger.trade.server.jersey.model.ResourceEventFilterDescriptionArray;
import io.swagger.trade.server.jersey.model.ResourceEventFilterDescriptionArrayWithLinks;
import org.trade.core.auditing.events.ATraDEEvent;
import org.trade.core.auditing.events.EventFilterInformation;

import javax.validation.constraints.Min;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-05-16T14:58:47.393+02:00")
public class ResourceEventFiltersApiServiceImpl extends ResourceEventFiltersApiService {
    @Override
    public Response getResourceEventFilterDirectly(String resourceEventFilterId, String eventType, SecurityContext
            securityContext, UriInfo uriInfo) throws NotFoundException {
        Response response = null;

        List<EventFilterInformation> filters = ATraDEEvent.getAllPossibleEventFilters();

        // The provided resourceEventFilterId is the name of an event filter. Since there will be an overlap
        // in the sets of event filters (their names) of different model events, the result might be a list of event
        // filer descriptions
        if (filters != null && !filters.isEmpty()) {
            try {
                Stream<EventFilterInformation> stream = filters.stream();

                // By default, filter the list with the given event filter id (name of the filter)
                stream = stream.filter(e -> e.getFilterName()
                        .toUpperCase().equals(resourceEventFilterId.toUpperCase()));

                // If an eventType is specified, additionaly filter the list regarding this value
                if (eventType != null && !eventType.isEmpty()) {
                    stream = stream.filter(e -> e.getEventType()
                            .toUpperCase().equals(eventType.toUpperCase()));
                }

                List<EventFilterInformation> matchingFilters = stream.collect(Collectors.toList());

                if (!matchingFilters.isEmpty()) {
                    ResourceEventFilterDescriptionArray result = ResourceTransformationUtils.model2Resource(matchingFilters);

                    response = Response.ok().entity(result).build();
                } else {
                    response = Response.status(Response.Status.NOT_FOUND).entity(new NotFound().properties(Collections
                            .singletonList(resourceEventFilterId)).message("A resource filter with id='" + resourceEventFilterId +
                            "' is not provided by the middleware.")).build();
                }
            } catch (Exception e) {
                e.printStackTrace();

                response = Response.serverError().entity(e.getMessage()).build();
            }
        } else {
            response = Response.status(Response.Status.NOT_FOUND).entity(new NotFound().properties(Collections
                    .singletonList(resourceEventFilterId)).message("A resource filter with id='" + resourceEventFilterId +
                    "' is not provided by the middleware.")).build();
        }

        return response;
    }

    @Override
    public Response getResourceEventFilters(@Min(1) Integer start, @Min(1) Integer size, String eventType,
                                            SecurityContext securityContext, UriInfo uriInfo) throws NotFoundException {
        Response response = null;

        try {
            // Get all available filters
            List<EventFilterInformation> filters = ATraDEEvent.getAllPossibleEventFilters();
            // This list is used to hold the reduced list of filters, if an eventType query param is set
            List<EventFilterInformation> matchingFilters = new ArrayList<>();

            int filteredListSize = 0;

            if (filters != null && !filters.isEmpty()) {
                Stream<EventFilterInformation> stream = filters.stream();

                // If an eventType is specified filter the list regarding this value
                if (eventType != null && !eventType.isEmpty()) {
                    stream = stream.filter(e -> e.getEventType()
                            .toUpperCase().equals(eventType.toUpperCase()));
                }

                matchingFilters = stream.collect(Collectors.toList());

                filteredListSize = matchingFilters.size();

                // Check if the start index and the size are in still the range of the filtered result list, if not
                // respond the whole filtered result list
                if (start > 0 && size > 0 && start <= filters.size()) {
                    // Calculate the two index
                    int toIndex = start - 1 + size;
                    // Check if the index is still in bounds
                    if (toIndex > filters.size()) {
                        toIndex = filters.size();
                    }
                    // Decrease start by one since the API starts counting indexes from 1
                    filters = filters.subList(start - 1, toIndex);
                }
            }

            ResourceEventFilterDescriptionArrayWithLinks resultList = new ResourceEventFilterDescriptionArrayWithLinks();
            resultList.setResourceFilterDescriptions(new ResourceEventFilterDescriptionArray());
            for (EventFilterInformation filter : matchingFilters) {

                ResourceEventFilterDescription result = ResourceTransformationUtils.model2Resource(filter);

                // Set HREF and links to related resources
                UriBuilder builder = uriInfo.getAbsolutePathBuilder();
                URI valueUri = builder.path(result.getFilterName()).replaceQueryParam("eventType", filter.getEventType())
                        .build();

                result.setHref(valueUri.toASCIIString());

                resultList.getResourceFilterDescriptions().add(result);
            }

            resultList.setLinks(LinkUtils.createPaginationLinks("resourceEventFilters", uriInfo, start, size,
                    filteredListSize));

            response = Response.ok().entity(resultList).build();
        } catch (Exception e) {
            e.printStackTrace();

            response = Response.serverError().entity(e.getMessage()).build();
        }

        return response;
    }
}

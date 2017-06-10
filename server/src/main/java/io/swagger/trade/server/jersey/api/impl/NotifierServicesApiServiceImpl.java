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
import io.swagger.trade.server.jersey.api.NotifierServicesApiService;
import io.swagger.trade.server.jersey.api.util.ResourceTransformationUtils;
import io.swagger.trade.server.jersey.model.NotFound;
import io.swagger.trade.server.jersey.model.NotifierService;
import io.swagger.trade.server.jersey.model.NotifierServiceArray;
import io.swagger.trade.server.jersey.model.NotifierServiceArrayWithLinks;
import org.trade.core.notifiers.NotifierServiceFactory;

import javax.validation.constraints.Min;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.Collections;
import java.util.List;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-05-16T14:58:47.393+02:00")
public class NotifierServicesApiServiceImpl extends NotifierServicesApiService {
    @Override
    public Response getNotifierServiceDirectly(String notifierServiceId, SecurityContext securityContext, UriInfo uriInfo) throws NotFoundException {
        Response response = null;

        boolean exists = NotifierServiceFactory.isNotifierServiceAvailable(notifierServiceId);

        if (exists) {
            try {
                NotifierService result = ResourceTransformationUtils.model2Resource(NotifierServiceFactory.createNotifierService(notifierServiceId));

                response = Response.ok().entity(result).build();
            } catch (Exception e) {
                e.printStackTrace();

                response = Response.serverError().entity(e.getMessage()).build();
            }
        } else {
            response = Response.status(Response.Status.NOT_FOUND).entity(new NotFound().properties(Collections
                    .singletonList(notifierServiceId)).message("A notifier service with id='" + notifierServiceId +
                    "' is not available/registered to the middleware.")).build();
        }

        return response;
    }
    @Override
    public Response getNotifierServices( @Min(1) Integer start,  @Min(1) Integer size, SecurityContext securityContext, UriInfo uriInfo) throws NotFoundException {
        Response response = null;

        try {
            List<String> notifierServiceIds = NotifierServiceFactory.getAvailableNotifierServiceIds();
            int filteredListSize = notifierServiceIds.size();

            // Check if the start index and the size are in still the range of the filtered result list, if not
            // respond the whole filtered result list
            if (start > 0 && size > 0 && start <= notifierServiceIds.size()) {
                // Calculate the two index
                int toIndex = start - 1 + size;
                // Check if the index is still in bounds
                if (toIndex > notifierServiceIds.size()) {
                    toIndex = notifierServiceIds.size();
                }
                // Decrease start by one since the API starts counting indexes from 1
                notifierServiceIds = notifierServiceIds.subList(start - 1, toIndex);
            }

            NotifierServiceArrayWithLinks resultList = new NotifierServiceArrayWithLinks();
            resultList.setNotifierServices(new NotifierServiceArray());
            for (String notifierId : notifierServiceIds) {

                NotifierService result = ResourceTransformationUtils.model2Resource(NotifierServiceFactory.createNotifierService(notifierId));

                // Set HREF and links to related resources
                UriBuilder builder = uriInfo.getAbsolutePathBuilder();
                URI valueUri = builder.path(result.getId()).build();

                result.setHref(valueUri.toASCIIString());

                resultList.getNotifierServices().add(result);
            }

            resultList.setLinks(LinkUtils.createPaginationLinks("notifierServices", uriInfo, start, size,
                    filteredListSize));

            response = Response.ok().entity(resultList).build();
        } catch (Exception e) {
            e.printStackTrace();

            response = Response.serverError().entity(e.getMessage()).build();
        }

        return response;
    }
}

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

import io.swagger.trade.server.jersey.model.Link;
import io.swagger.trade.server.jersey.model.LinkArray;

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.net.URI;

/**
 * Created by hahnml on 11.04.2017.
 */
public class LinkUtils {

    public static LinkArray createPaginationLinks(String typeOfCollection, UriInfo uriInfo, Integer start, Integer size,
                                            Integer sizeOfCollection) {
        LinkArray links = new LinkArray();

        UriBuilder builder = uriInfo.getAbsolutePathBuilder();

        URI nextUri = null;
        if (start + size <= sizeOfCollection) {
            // Check if there are enough elements after the current selection, else we do not show a next link since
            // everything available is already presented through the current response.
            nextUri = builder.replaceQueryParam("start", start + size).replaceQueryParam("size", size).build();
        }

        if (nextUri != null) {
            Link next = new Link();
            next.setHref(nextUri.toASCIIString());
            next.setRel("next");
            next.setTitle("Provides the next " + typeOfCollection + " of the collection matching the defined " +
                    "filter values.");
            links.add(next);
        }

        URI previousUri = null;
        if (start - size > 0) {
            // Check if there are enough elements before the current selection
            previousUri = builder.replaceQueryParam("start", start - size).replaceQueryParam("size", size).build();
        } else {
            // Start from the beginning, if the current start value is not already the first index
            if (start > 0 && start != 1) {
                previousUri = builder.replaceQueryParam("start", 1).build();
            }
        }

        if (previousUri != null) {
            Link prev = new Link();
            prev.setHref(previousUri.toASCIIString());
            prev.setRel("prev");
            prev.setTitle("Provides the previous " + typeOfCollection + " of the collection matching the defined " +
                    "filter values.");
            links.add(prev);
        }

        return links;
    }

    public static LinkArray createDataDependencyGraphLinks(UriInfo uriInfo) {
        // TODO: Add link to data model used by this DDG, etc.!
        LinkArray links = new LinkArray();


        return links;
    }

    public static LinkArray createDataModelLinks(UriInfo uriInfo) {
        // TODO: Add links to related data objects that are used/specified by this data model, etc.!
        LinkArray links = new LinkArray();


        return links;
    }

    public static LinkArray createDataValueLinks(UriInfo uriInfo) {
        // TODO: Add links to data element instances that use this data value, etc.!
        LinkArray links = new LinkArray();


        return links;
    }

    public static LinkArray createDataObjectLinks(UriInfo uriInfo) {
        // TODO: Add links to data elements, etc. that relate to this data object!
        LinkArray links = new LinkArray();


        return links;
    }
}

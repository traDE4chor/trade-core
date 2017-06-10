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
import io.swagger.trade.server.jersey.model.ResourceTypeEnum;
import org.trade.core.model.ABaseResource;
import org.trade.core.model.data.*;
import org.trade.core.model.data.instance.DataElementInstance;
import org.trade.core.model.data.instance.DataObjectInstance;
import org.trade.core.model.notification.Notification;

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.net.URI;

/**
 * Created by hahnml on 11.04.2017.
 */
public class LinkUtils {
    // Using relation types defined at 'https://www.iana.org/assignments/link-relations/link-relations.xhtml'
    // wherever possible.
    private static final String IANA_RELS = "https://www.iana.org/assignments/link-relations/link-relations.xhtml";
    public static final String RELATION_PREVIOUS = IANA_RELS + "#" + "prev";
    public static final String RELATION_NEXT = IANA_RELS + "#" + "next";
    public static final String RELATION_SELF = IANA_RELS + "#" + "self";
    public static final String RELATION_COLLECTION = IANA_RELS + "#" + "collection";

    // TODO: Change simple strings to URIs under which also a HTML page is provided that describes the concrete
    // semantics of each of the custom relations, e.g., "http://www.rels.com/orderAPI/POST/cancel"
    public static final String RELATION_DATA_DEPENDENCY_GRAPH = "dataDependencyGraph";
    public static final String RELATION_DATA_MODEL = "dataModel";
    public static final String RELATION_DATA_MODEL_ALTERNATE = "dataModel-alternate";
    public static final String RELATION_DATA_OBJECTS = "dataObjects";
    public static final String RELATION_DATA_OBJECT = "dataObject";
    public static final String RELATION_DATA_ELEMENTS = "dataElements";
    public static final String RELATION_DATA_ELEMENT = "dataElement";
    public static final String RELATION_DATA_OBJECT_INSTANCES = "dataObjectInstances";
    public static final String RELATION_DATA_OBJECT_INSTANCE = "dataObjectInstance";
    public static final String RELATION_DATA_ELEMENT_INSTANCES = "dataElementInstances";
    public static final String RELATION_DATA_ELEMENT_INSTANCE = "dataElementInstance";
    public static final String RELATION_DATA_VALUE = "dataValue";
    public static final String RELATION_DATA_VALUE_ALTERNATE = "dataValue-alternate";
    public static final String RELATION_NOTIFIER_SERVICE = "notifierService";

    // The names of all collection resources
    public static final String COLLECTION_DATA_DEPENDENCY_GRAPH = "dataDependencyGraphs";
    public static final String COLLECTION_DATA_MODEL = "dataModels";
    public static final String COLLECTION_DATA_OBJECT = "dataObjects";
    public static final String COLLECTION_DATA_ELEMENT = "dataElements";
    public static final String COLLECTION_DATA_OBJECT_INSTANCE = "dataObjectInstances";
    public static final String COLLECTION_DATA_ELEMENT_INSTANCE = "dataElementInstances";
    public static final String COLLECTION_DATA_VALUE = "dataValues";
    public static final String COLLECTION_NOTIFICATIONS = "notifications";
    public static final String COLLECTION_NOTIFIER_SERVICES = "notifierServices";
    public static final String COLLECTION_RESOURCE_FILTERS = "resourceFilters";

    // A list of special URI templates we use to build links
    public static final String TEMPLATE_COLLECTION_RESOURCE = "{collectionName}/{resourceId}";
    public static final String TEMPLATE_DDG__DATA_MODEL = COLLECTION_DATA_DEPENDENCY_GRAPH + "/{resourceId}/dataModel";
    public static final String TEMPLATE_DATA_MODEL__DATA_OBJECTS = COLLECTION_DATA_MODEL
            + "/{modelId}/dataObjects";
    public static final String TEMPLATE_DATA_OBJECT__INSTANCES = COLLECTION_DATA_OBJECT
            + "/{dataObjectId}/instances";
    public static final String TEMPLATE_DATA_OBJECT__ELEMENTS = COLLECTION_DATA_OBJECT
            + "/{dataObjectId}/dataElements";
    public static final String TEMPLATE_DATA_OBJECT_INSTANCES__ELEMENT_INSTANCE = COLLECTION_DATA_OBJECT_INSTANCE
            + "/{objectInstanceId}/elementInstances";
    public static final String TEMPLATE_DATA_ELEMENT__INSTANCES = COLLECTION_DATA_ELEMENT
            + "/{dataElementId}/instances";
    public static final String TEMPLATE_DATA_ELEMENT_INSTANCE__DATA_VALUE = COLLECTION_DATA_ELEMENT_INSTANCE
            + "/{elementInstanceId}/dataValue";
    public static final String TEMPLATE_DATA_VALUE__ELEMENT_INSTANCES = COLLECTION_DATA_VALUE +
            "/{dataValueId}/elementInstances";
    public static final String TEMPLATE_NOTIFICATION__NOTIFIER = COLLECTION_NOTIFICATIONS +
            "/{notificationId}/notifierService";

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
            next.setRel(RELATION_NEXT);
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
            prev.setRel(RELATION_PREVIOUS);
            prev.setTitle("Provides the previous " + typeOfCollection + " of the collection matching the defined " +
                    "filter values.");
            links.add(prev);
        }

        Link self = new Link();
        self.setHref(builder.replaceQueryParam("start", start).replaceQueryParam("size", size).build().toASCIIString());
        self.setRel(RELATION_SELF);
        links.add(self);

        return links;
    }

    public static LinkArray createDataDependencyGraphLinks(UriInfo uriInfo, DataDependencyGraph graph, String selfUri) {
        LinkArray links = new LinkArray();

        // Add link to data model used by this DDG
        if (graph != null && graph.getDataModel() != null) {
            Link link = new Link();
            link.setHref(calculateURI(uriInfo, graph.getDataModel()));
            link.setRel(RELATION_DATA_MODEL);
            link.setTitle("Provides the data model associated to this data dependency graph.");
            links.add(link);

            // Add an alternate link to the model which supports to change the association
            link = new Link();
            link.setHref(uriInfo.getBaseUriBuilder().path(LinkUtils
                    .TEMPLATE_DDG__DATA_MODEL).build(graph.getIdentifier()).toASCIIString());
            link.setRel(RELATION_DATA_MODEL_ALTERNATE);
            link.setTitle("Provides an alternative link to access and set the associated data model of this data " +
                    "dependency graph.");
            links.add(link);
        } else if (graph != null) {
            // Add the alternate link to the model which supports to change the association
            Link link = new Link();
            link.setHref(uriInfo.getBaseUriBuilder().path(LinkUtils
                    .TEMPLATE_DDG__DATA_MODEL).build(graph.getIdentifier()).toASCIIString());
            link.setRel(RELATION_DATA_MODEL);
            link.setTitle("Use this link to set/associate a data model to this data dependency graph.");
            links.add(link);
        }

        // TODO: What about the serialized model?

        // Add link to collection
        links.add(createLinkToCollection(uriInfo, graph));

        // Add self link
        links.add(createSelfLink(selfUri));

        return links;
    }

    public static LinkArray createDataModelLinks(UriInfo uriInfo, DataModel dataModel, String selfUri) {
        LinkArray links = new LinkArray();

        // Add link to related data objects that are used/specified by this data model
        if (dataModel != null && dataModel.getDataObjects() != null && !dataModel.getDataObjects().isEmpty()) {
            Link link = new Link();
            link.setHref(uriInfo.getBaseUriBuilder().path(LinkUtils
                    .TEMPLATE_DATA_MODEL__DATA_OBJECTS).build(dataModel.getIdentifier()).toASCIIString());
            link.setRel(RELATION_DATA_OBJECTS);
            link.setTitle("Provides the collection of data objects defined by this data model.");
            links.add(link);
        }

        // TODO: What about the serialized model?

        // Add link to collection
        links.add(createLinkToCollection(uriInfo, dataModel));

        // Add self link
        links.add(createSelfLink(selfUri));

        return links;
    }

    public static LinkArray createDataObjectLinks(UriInfo uriInfo, DataObject dataObject, String selfUri) {
        LinkArray links = new LinkArray();

        // Add link to data model, if there is one
        if (dataObject != null && dataObject.getDataModel() != null) {
            Link link = new Link();
            link.setHref(calculateURI(uriInfo, dataObject.getDataModel()));
            link.setRel(RELATION_DATA_MODEL);
            link.setTitle("Provides the data model that defines this data object.");
            links.add(link);
        }

        // Add link to data elements that relate to this data object
        if (dataObject != null && dataObject.getDataElements() != null && !dataObject.getDataElements().isEmpty()) {
            Link link = new Link();
            link.setHref(uriInfo.getBaseUriBuilder().path(LinkUtils
                    .TEMPLATE_DATA_OBJECT__ELEMENTS).build(dataObject.getIdentifier()).toASCIIString());
            link.setRel(RELATION_DATA_ELEMENTS);
            link.setTitle("Provides the collection of data elements that belong to this data object.");
            links.add(link);
        }

        // Add link to data object instance that belong to this data object
        if (dataObject != null && dataObject.getDataObjectInstances() != null &&
                !dataObject.getDataObjectInstances().isEmpty()) {
            Link link = new Link();
            link.setHref(uriInfo.getBaseUriBuilder().path(LinkUtils
                    .TEMPLATE_DATA_OBJECT__INSTANCES).build(dataObject.getIdentifier()).toASCIIString());
            link.setRel(RELATION_DATA_OBJECT_INSTANCES);
            link.setTitle("Provides the collection of data object instances that belong to this data object.");
            links.add(link);
        }

        // Add link to collection
        links.add(createLinkToCollection(uriInfo, dataObject));

        // Add self link
        links.add(createSelfLink(selfUri));

        return links;
    }

    public static LinkArray createDataElementLinks(UriInfo uriInfo, DataElement dataElement, String selfUri) {
        LinkArray links = new LinkArray();

        // Add link to parent data object, if there is one
        if (dataElement != null && dataElement.getParent() != null) {
            Link link = new Link();
            link.setHref(calculateURI(uriInfo, dataElement.getParent()));
            link.setRel(RELATION_DATA_OBJECT);
            link.setTitle("Provides the parent data object of this data element.");
            links.add(link);
        }

        // Add link to data element instances that relate to this data element
        if (dataElement != null && dataElement.getDataElementInstances() != null && !dataElement
                .getDataElementInstances().isEmpty()) {
            Link link = new Link();
            link.setHref(uriInfo.getBaseUriBuilder().path(LinkUtils
                    .TEMPLATE_DATA_ELEMENT__INSTANCES).build(dataElement.getIdentifier()).toASCIIString());
            link.setRel(RELATION_DATA_ELEMENT_INSTANCES);
            link.setTitle("Provides the collection of data element instances that belong to this data element.");
            links.add(link);
        }

        // Add link to collection
        links.add(createLinkToCollection(uriInfo, dataElement));

        // Add self link
        links.add(createSelfLink(selfUri));

        return links;
    }

    public static LinkArray createDataValueLinks(UriInfo uriInfo, DataValue dataValue, String selfUri) {
        LinkArray links = new LinkArray();

        // Add link to data element instances that use this data value
        if (dataValue != null && dataValue.getDataElementInstances() != null && !dataValue.getDataElementInstances()
                .isEmpty()) {
            Link link = new Link();
            link.setHref(uriInfo.getBaseUriBuilder().path(LinkUtils
                    .TEMPLATE_DATA_VALUE__ELEMENT_INSTANCES).build(dataValue.getIdentifier()).toASCIIString());
            link.setRel(RELATION_DATA_ELEMENT_INSTANCES);
            link.setTitle("Provides the collection of data element instances that use this data value.");
            links.add(link);
        }

        // Add link to collection
        links.add(createLinkToCollection(uriInfo, dataValue));

        // Add self link
        links.add(createSelfLink(selfUri));

        return links;
    }

    public static LinkArray createDataElementInstanceLinks(UriInfo uriInfo, DataElementInstance dataElementInstance, String selfUri) {
        LinkArray links = new LinkArray();

        // Add link to data element (model)
        if (dataElementInstance != null && dataElementInstance.getDataElement() != null) {
            Link link = new Link();
            link.setHref(calculateURI(uriInfo, dataElementInstance.getDataElement()));
            link.setRel(RELATION_DATA_ELEMENT);
            link.setTitle("Provides the underlying data element which defines the model for this data element " +
                    "instance.");
            links.add(link);
        }

        // Add link to parent data object instance
        if (dataElementInstance != null && dataElementInstance.getDataObjectInstance() != null) {
            Link link = new Link();
            link.setHref(calculateURI(uriInfo, dataElementInstance.getDataObjectInstance()));
            link.setRel(RELATION_DATA_OBJECT_INSTANCE);
            link.setTitle("Provides the parent data object instance to which the data element instance belongs.");
            links.add(link);
        }

        // Add link to used data value, if there is one
        if (dataElementInstance != null && dataElementInstance.getDataValue() != null) {
            Link link = new Link();
            link.setHref(calculateURI(uriInfo, dataElementInstance.getDataValue()));
            link.setRel(RELATION_DATA_VALUE);
            link.setTitle("Provides the data value used by this data element instance.");
            links.add(link);

            // Add an alternate link to the data value which supports to change the association
            link = new Link();
            link.setHref(uriInfo.getBaseUriBuilder().path(LinkUtils
                    .TEMPLATE_DATA_ELEMENT_INSTANCE__DATA_VALUE).build(dataElementInstance.getIdentifier()).toASCIIString());
            link.setRel(RELATION_DATA_VALUE_ALTERNATE);
            link.setTitle("Provides an alternative link to access and set the associated data value of this data " +
                    "element instance.");
            links.add(link);
        } else if (dataElementInstance != null) {
            // Add the alternate link to the data value which supports to change the association
            Link link = new Link();
            link.setHref(uriInfo.getBaseUriBuilder().path(LinkUtils
                    .TEMPLATE_DATA_ELEMENT_INSTANCE__DATA_VALUE).build(dataElementInstance.getIdentifier()).toASCIIString());
            link.setRel(RELATION_DATA_VALUE);
            link.setTitle("Use this link to set/associate a data value to this data element instance.");
            links.add(link);
        }

        // Add link to collection
        links.add(createLinkToCollection(uriInfo, dataElementInstance));

        // Add self link
        links.add(createSelfLink(selfUri));

        return links;
    }

    public static LinkArray createDataObjectInstanceLinks(UriInfo uriInfo, DataObjectInstance dataObjectInstance, String selfUri) {
        LinkArray links = new LinkArray();

        // Add link to data object (model)
        if (dataObjectInstance != null && dataObjectInstance.getDataObject() != null) {
            Link link = new Link();
            link.setHref(calculateURI(uriInfo, dataObjectInstance.getDataObject()));
            link.setRel(RELATION_DATA_OBJECT);
            link.setTitle("Provides the underlying data object which defines the model for this data object " +
                    "instance.");
            links.add(link);
        }

        // Add link to data element instances
        if (dataObjectInstance != null && dataObjectInstance.getDataElementInstances() != null && !dataObjectInstance.getDataElementInstances()
                .isEmpty()) {
            Link link = new Link();
            link.setHref(uriInfo.getBaseUriBuilder().path(LinkUtils
                    .TEMPLATE_DATA_OBJECT_INSTANCES__ELEMENT_INSTANCE).build(dataObjectInstance.getIdentifier()).toASCIIString());
            link.setRel(RELATION_DATA_ELEMENT_INSTANCES);
            link.setTitle("Provides the collection of data element instances that belong to this data object instance" +
                    ".");
            links.add(link);
        }

        // Add link to collection
        links.add(createLinkToCollection(uriInfo, dataObjectInstance));

        // Add self link
        links.add(createSelfLink(selfUri));

        return links;
    }

    public static LinkArray createNotificationLinks(UriInfo uriInfo, Notification notification, String selfUri) {
        LinkArray links = new LinkArray();

        // Add link to the notifier service which is used by this notification
        if (notification != null && notification.getSelectedNotifierServiceId() != null) {
            Link link = new Link();
            link.setHref(uriInfo.getBaseUriBuilder().path(LinkUtils
                    .TEMPLATE_NOTIFICATION__NOTIFIER).build(notification.getIdentifier()).toASCIIString());
            link.setRel(RELATION_NOTIFIER_SERVICE);
            link.setTitle("Provides the notifier service that is used by this notification.");
            links.add(link);
        }

        // Add link to collection
        links.add(createLinkToCollection(uriInfo, notification));

        // Add self link
        links.add(createSelfLink(selfUri));

        return links;
    }

    public static String resolveResourceURI(UriInfo uriInfo, ABaseResource modelToGetUriFor) {
        return calculateURI(uriInfo, modelToGetUriFor);
    }

    public static String resolveResourceCollectionURI(UriInfo uriInfo, ResourceTypeEnum resourceType) {
        // Use the corresponding template to build the URI
        return uriInfo.getBaseUriBuilder().path(getCollection(resourceType)).build()
                .toASCIIString();
    }

    private static Link createSelfLink(String selfURI) {
        Link self = new Link();
        self.setHref(selfURI);
        self.setRel(RELATION_SELF);

        return self;
    }

    private static Link createLinkToCollection(UriInfo uriInfo, ABaseResource modelElementOfCollection) {
        Link self = new Link();
        self.setHref(uriInfo.getBaseUriBuilder().path(getCollection(modelElementOfCollection)).build().toASCIIString());
        self.setTitle("Provides the whole collection to which this resource belongs.");
        self.setRel(RELATION_COLLECTION);

        return self;
    }

    private static String calculateURI(UriInfo uriInfo, ABaseResource modelToGetUriFor) {
        // Use the corresponding template to build the URI
        return uriInfo.getBaseUriBuilder().path(TEMPLATE_COLLECTION_RESOURCE).build(getCollection
                (modelToGetUriFor), modelToGetUriFor
                .getIdentifier()).toASCIIString();
    }

    private static String getCollection(ABaseResource modelToGetUriFor) {
        // Identify the 'main' collection to which the provided element belongs
        String result = null;

        if (modelToGetUriFor instanceof DataDependencyGraph) {
            result = COLLECTION_DATA_DEPENDENCY_GRAPH;
        } else if (modelToGetUriFor instanceof DataModel) {
            result = COLLECTION_DATA_MODEL;
        } else if (modelToGetUriFor instanceof DataObject) {
            result = COLLECTION_DATA_OBJECT;
        } else if (modelToGetUriFor instanceof DataElement) {
            result = COLLECTION_DATA_ELEMENT;
        } else if (modelToGetUriFor instanceof DataValue) {
            result = COLLECTION_DATA_VALUE;
        } else if (modelToGetUriFor instanceof DataObjectInstance) {
            result = COLLECTION_DATA_OBJECT_INSTANCE;
        } else if (modelToGetUriFor instanceof DataElementInstance) {
            result = COLLECTION_DATA_ELEMENT_INSTANCE;
        } else if (modelToGetUriFor instanceof Notification) {
            result = COLLECTION_NOTIFICATIONS;
        }

        return result;
    }

    private static String getCollection(ResourceTypeEnum resourceType) {
        // Identify the 'main' collection to which the provided element belongs
        String result = null;

        switch (resourceType) {
            case DATADEPENDENCYGRAPH:
                result = COLLECTION_DATA_DEPENDENCY_GRAPH;
                break;
            case DATAMODEL:
                result = COLLECTION_DATA_MODEL;
                break;
            case DATAOBJECT:
                result = COLLECTION_DATA_OBJECT;
                break;
            case DATAELEMENT:
                result = COLLECTION_DATA_ELEMENT;
                break;
            case DATAOBJECTINSTANCE:
                result = COLLECTION_DATA_OBJECT_INSTANCE;
                break;
            case DATAELEMENTINSTANCE:
                result = COLLECTION_DATA_ELEMENT_INSTANCE;
                break;
            case DATAVALUE:
                result = COLLECTION_DATA_VALUE;
                break;
        }

        return result;
    }
}

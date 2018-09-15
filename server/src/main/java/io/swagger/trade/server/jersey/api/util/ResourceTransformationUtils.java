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

package io.swagger.trade.server.jersey.api.util;

import io.swagger.trade.server.jersey.api.impl.LinkUtils;
import io.swagger.trade.server.jersey.model.*;
import org.trade.core.auditing.events.EventFilterInformation;
import org.trade.core.data.management.DataManagerFactory;
import org.trade.core.data.management.IDataManager;
import org.trade.core.model.ABaseResource;
import org.trade.core.notifiers.INotifierService;

import javax.ws.rs.core.UriInfo;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by hahnml on 26.01.2017.
 */
public class ResourceTransformationUtils {

    public static DataValue model2Resource(org.trade.core.model.data.DataValue dataValue) {
        DataValue value = new DataValue();

        value.setId(dataValue.getIdentifier());
        value.setName(dataValue.getName());

        value.setType(dataValue.getType());
        value.setContentType(dataValue.getContentType());

        value.setCreated(dataValue.getCreationTimestamp());
        value.setLastModified(dataValue.getLastModified());

        value.setCreatedBy(dataValue.getOwner());
        value.setSize(dataValue.getSize());

        value.setStatus(string2InstanceStatus(dataValue.getState()));

        return value;
    }

    public static org.trade.core.model.data.DataValue resource2Model(DataValueData dataValue) {
        org.trade.core.model.data.DataValue value = new org.trade.core.model.data.DataValue(dataValue.getCreatedBy(),
                dataValue.getName());

        value.setContentType(dataValue.getContentType());
        value.setType(dataValue.getType());

        return value;
    }

    public static org.trade.core.model.data.DataValue resource2Model(DataValue dataValue) {
        org.trade.core.model.data.DataValue value = new org.trade.core.model.data.DataValue(dataValue.getCreatedBy(),
                dataValue.getName());

        value.setContentType(dataValue.getContentType());
        value.setType(dataValue.getType());

        return value;
    }

    public static org.trade.core.model.data.DataDependencyGraph resource2Model(DataDependencyGraphData dataDependencyGraphData) {
        return new org.trade.core.model.data.DataDependencyGraph
                (dataDependencyGraphData.getEntity(), dataDependencyGraphData.getName());
    }

    public static org.trade.core.model.data.DataModel resource2Model(DataModelData dataModelData) {
        return new org.trade.core.model.data.DataModel
                (dataModelData.getEntity(), dataModelData.getName());
    }

    public static org.trade.core.model.data.DataObject resource2Model(DataObjectData dataObjectData) {
        return new org.trade.core.model.data.DataObject(dataObjectData
                .getEntity(), dataObjectData.getName());
    }

    public static org.trade.core.model.notification.Notification resource2Model(UriInfo uriInfo, NotificationData
            notificationData) {

        ABaseResource modelResource = null;
        String resourceURL = null;
        if (notificationData.getIdOfResourceToObserve() != null) {
            String resourceId = notificationData.getIdOfResourceToObserve();

            modelResource = resolveModelResource(resourceId, notificationData.getTypeOfResourceToObserve());

            // Try to resolve the correct resource URL
            resourceURL = LinkUtils.resolveResourceURI(uriInfo, modelResource);
        } else if (notificationData.getTypeOfResourceToObserve() != null) {
            // Try to resolve the URL of the collection of the specified resource type, e.g., the collection of data
            // objects
            resourceURL = LinkUtils.resolveResourceCollectionURI(uriInfo, notificationData.getTypeOfResourceToObserve());
        }

        org.trade.core.model.notification.Notification notification = null;

        if (modelResource != null) {
            notification = new org.trade.core.model.notification
                    .Notification(notificationData.getName(), modelResource, resourceURL);
        } else {
            notification = new org.trade.core.model.notification
                    .Notification(notificationData.getName());
            notification.setResourceURL(resourceURL);
        }

        notification.setSelectedNotifierServiceId(notificationData.getSelectedNotifierServiceId());
        notification.setNotifierParameters(resource2Model(notificationData.getNotifierParameterValues()));
        notification.setResourceFilters(resource2Model(notificationData.getResourceFilters()));

        return notification;
    }

    public static org.trade.core.model.ABaseResource resolveModelResource(String resourceId, ResourceTypeEnum
            resourceType) {
        ABaseResource modelResource = null;

        if (resourceId != null) {
            // Resolve model object through the id and the type of object (enum constants equal model class names)
            IDataManager mgr = DataManagerFactory.createDataManager();

            switch (resourceType) {
                case DATADEPENDENCYGRAPH:
                    modelResource = mgr.getDataDependencyGraph(resourceId);
                    break;
                case DATAMODEL:
                    modelResource = mgr.getDataModel(resourceId);
                    break;
                case DATAOBJECT:
                    modelResource = mgr.getDataObject(resourceId);
                    break;
                case DATAELEMENT:
                    modelResource = mgr.getDataElement(resourceId);
                    break;
                case DATAOBJECTINSTANCE:
                    modelResource = mgr.getDataObjectInstance(resourceId);
                    break;
                case DATAELEMENTINSTANCE:
                    modelResource = mgr.getDataElementInstance(resourceId);
                    break;
                case DATAVALUE:
                    modelResource = mgr.getDataValue(resourceId);
                    break;
            }
        }

        return modelResource;
    }

    public static Map<String, String> resource2Model(NotifierServiceParameterArray notifierParameterValues) {
        Map<String, String> result = new HashMap<>();

        for (NotifierServiceParameter notifierParameterValue : notifierParameterValues) {
            result.put(notifierParameterValue.getParameterName(), notifierParameterValue.getValue());
        }

        return result;
    }

    public static Map<String, String> resource2Model(ResourceEventFilterArray resourceFilters) {
        Map<String, String> result = new HashMap<>();

        for (ResourceEventFilter resourceFilter : resourceFilters) {
            result.put(resourceFilter.getFilterName(), resourceFilter.getFilterValue());
        }

        return result;
    }

    public static DataDependencyGraph model2Resource(org.trade.core.model.data.DataDependencyGraph graph) {
        DataDependencyGraph result = new DataDependencyGraph();

        result.setName(graph.getName());
        result.setEntity(graph.getEntity());
        result.setTargetNamespace(graph.getTargetNamespace());
        result.setId(graph.getIdentifier());

        if (graph.getDataTransformations() != null) {
            for (org.trade.core.model.dataTransformation.DataTransformation transformation : graph.getDataTransformations()) {
                DataTransformation transf = new DataTransformation();
                transf.setName(transformation.getName());
                transf.setTransformerQName(transformation.getTransformerQName());

                if (transformation.getTransformerParameters() != null) {
                    for (String parameterName : transformation.getTransformerParameters().keySet()) {
                        DataTransformationTransformerParameters param = new DataTransformationTransformerParameters();
                        param.setParameterName(parameterName);

                        String parameterValue = transformation.getTransformerParameters().get(parameterName);
                        param.setParameterValue(parameterValue);

                        transf.addTransformerParametersItem(param);
                    }
                }

                result.addTransformationsItem(transf);
            }
        }

        return result;
    }

    public static DataModel model2Resource(org.trade.core.model.data.DataModel model) {
        DataModel result = new DataModel();

        result.setName(model.getName());
        result.setEntity(model.getEntity());
        result.setTargetNamespace(model.getTargetNamespace());
        result.setId(model.getIdentifier());
        result.setStatus(string2Status(model.getState()));

        return result;
    }

    public static DataObject model2Resource(org.trade.core.model.data.DataObject dataObject) {
        DataObject result = new DataObject();

        result.setEntity(dataObject.getEntity());
        result.setId(dataObject.getIdentifier());
        result.setName(dataObject.getName());
        result.setStatus(string2Status(dataObject.getState()));

        if (dataObject.getDataModel() != null) {
            result.setDataModelName(dataObject.getDataModel().getName());
        }

        return result;
    }

    public static DataElement model2Resource(org.trade.core.model.data.DataElement dataElement) {
        DataElement result = new DataElement();

        result.setName(dataElement.getName());
        result.setContentType(dataElement.getContentType());
        result.setType(dataElement.getType());
        result.setId(dataElement.getIdentifier());
        result.setDataObjectName(dataElement.getParent().getName());
        result.setEntity(dataElement.getEntity());
        result.setStatus(string2Status(dataElement.getState()));
        result.setIsCollectionElement(dataElement.isCollectionElement());

        return result;
    }

    public static DataObjectInstance model2Resource(org.trade.core.model.data.instance.DataObjectInstance instance) {
        DataObjectInstance result = new DataObjectInstance();

        result.setId(instance.getIdentifier());
        result.setCreatedBy(instance.getCreatedBy());
        result.setStatus(string2InstanceStatus(instance.getState()));
        result.setDataObjectName(instance.getDataObject().getName());
        result.setCorrelationProperties(model2Resource(instance.getCorrelationProperties()));

        return result;
    }

    public static DataElementInstance model2Resource(
            org.trade.core.model.data.instance.DataElementInstance instance) {
        DataElementInstance result = new DataElementInstance();

        result.setId(instance.getIdentifier());
        result.setCreatedBy(instance.getCreatedBy());
        result.setStatus(string2InstanceStatus(instance.getState()));
        result.setDataElementName(instance.getDataElement().getName());
        result.setCorrelationProperties(model2Resource(instance.getCorrelationProperties()));
        result.setNumberOfDataValues(instance.getNumberOfDataValues());

        return result;
    }

    public static Notification model2Resource(org.trade.core.model.notification.Notification notification) {
        Notification result = new Notification();

        result.setId(notification.getIdentifier());
        result.setIdOfResourceToObserve(notification.getResource() != null ? notification.getResource().getIdentifier
                () : null);
        result.setName(notification.getName());
        result.setNotifierParameterValues(model2ResourceNotifierParams(notification.getNotifierParameters()));
        result.setResourceFilters(model2ResourceResourceFilters(notification.getResourceFilters()));
        result.setSelectedNotifierServiceId(notification.getSelectedNotifierServiceId());

        return result;
    }

    private static NotifierServiceParameterArray model2ResourceNotifierParams(Map<String, String> notifierParameters) {
        NotifierServiceParameterArray result = new NotifierServiceParameterArray();

        for (String key : notifierParameters.keySet()) {
            NotifierServiceParameter param = new NotifierServiceParameter();

            param.setParameterName(key);
            param.setValue(notifierParameters.get(key));

            result.add(param);
        }

        return result;
    }

    private static ResourceEventFilterArray model2ResourceResourceFilters(Map<String, String> resourceFilters) {
        ResourceEventFilterArray result = new ResourceEventFilterArray();

        for (String key : resourceFilters.keySet()) {
            ResourceEventFilter param = new ResourceEventFilter();

            param.setFilterName(key);
            param.setFilterValue(resourceFilters.get(key));

            result.add(param);
        }

        return result;
    }

    public static NotifierService model2Resource(INotifierService notifierService) {
        NotifierService service = new NotifierService();

        service.setId(notifierService.getNotifierServiceId());
        service.setParameters(model2ResourceNotifierServiceParams(notifierService
                .getAvailableNotifierServiceParametersAndDescriptions()));

        return service;
    }

    public static ResourceEventFilterDescriptionArray model2Resource(List<EventFilterInformation> matchingFilters) {
        ResourceEventFilterDescriptionArray result = new ResourceEventFilterDescriptionArray();

        for (EventFilterInformation matchingFilter : matchingFilters) {
            result.add(model2Resource(matchingFilter));
        }

        return result;
    }

    public static ResourceEventFilterDescription model2Resource(EventFilterInformation matchingFilter) {
        ResourceEventFilterDescription result = new ResourceEventFilterDescription();

        result.setEventType(matchingFilter.getEventType());
        result.setFilterName(matchingFilter.getFilterName());
        result.setDescription(matchingFilter.getDescription());
        result.setValueDomainConstraints(matchingFilter.getConstrainedValueDomain());

        return result;
    }

    private static NotifierServiceParameterDescriptionArray model2ResourceNotifierServiceParams(Map<String, String>
                                                                                                        parameterDescriptions) {
        NotifierServiceParameterDescriptionArray result = new NotifierServiceParameterDescriptionArray();

        for (String key : parameterDescriptions.keySet()) {
            NotifierServiceParameterDescription param = new NotifierServiceParameterDescription();

            param.setParameterName(key);
            param.setDescription(parameterDescriptions.get(key));

            result.add(param);
        }

        return result;
    }

    public static HashMap<String, String> resource2Model(CorrelationPropertyArray correlationProperties) {
        HashMap<String, String> result = new HashMap<>();

        for (CorrelationProperty prop : correlationProperties) {
            result.put(prop.getKey(), prop.getValue());
        }

        return result;
    }

    private static CorrelationPropertyArray model2Resource(HashMap<String, String> correlationProperties) {
        CorrelationPropertyArray result = new CorrelationPropertyArray();

        for (String key : correlationProperties.keySet()) {
            CorrelationProperty prop = new CorrelationProperty();

            prop.setKey(key);
            prop.setValue(correlationProperties.get(key));

            result.add(prop);
        }

        return result;
    }

    public static StatusEnum string2Status(String value) {
        if (value != null && !value.isEmpty()) {
            if (value.toUpperCase().equals(StatusEnum.CREATED.toString().toUpperCase())) {
                return StatusEnum.CREATED;
            } else if (value.toUpperCase().equals(StatusEnum.ARCHIVED.toString().toUpperCase())) {
                return StatusEnum.ARCHIVED;
            } else if (value.toUpperCase().equals(StatusEnum.READY.toString().toUpperCase())) {
                return StatusEnum.READY;
            }
        }

        return StatusEnum.CREATED;
    }

    public static InstanceStatusEnum string2InstanceStatus(String value) {
        if (value != null && !value.isEmpty()) {
            if (value.toUpperCase().equals(InstanceStatusEnum.CREATED.toString().toUpperCase())) {
                return InstanceStatusEnum.CREATED;
            } else if (value.toUpperCase().equals(InstanceStatusEnum.ARCHIVED.toString().toUpperCase())) {
                return InstanceStatusEnum.ARCHIVED;
            } else if (value.toUpperCase().equals(InstanceStatusEnum.INITIALIZED.toString().toUpperCase())) {
                return InstanceStatusEnum.INITIALIZED;
            }
        }

        return InstanceStatusEnum.CREATED;
    }
}

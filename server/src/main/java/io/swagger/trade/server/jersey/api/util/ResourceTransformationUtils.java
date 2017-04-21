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

import io.swagger.trade.server.jersey.model.*;

import java.util.HashMap;

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
        org.trade.core.model.data.DataDependencyGraph graph = new org.trade.core.model.data.DataDependencyGraph
                (dataDependencyGraphData.getEntity(), dataDependencyGraphData.getName());

        return graph;
    }

    public static org.trade.core.model.data.DataModel resource2Model(DataModelData dataModelData) {
        org.trade.core.model.data.DataModel model = new org.trade.core.model.data.DataModel
                (dataModelData.getEntity(), dataModelData.getName());

        return model;
    }

    public static org.trade.core.model.data.DataObject resource2Model(DataObjectData dataObjectData) {
        org.trade.core.model.data.DataObject object = new org.trade.core.model.data.DataObject(dataObjectData
                .getEntity(), dataObjectData.getName());

        return object;
    }

    public static DataDependencyGraph model2Resource(org.trade.core.model.data.DataDependencyGraph graph) {
        DataDependencyGraph result = new DataDependencyGraph();

        result.setName(graph.getName());
        result.setEntity(graph.getEntity());
        result.setTargetNamespace(graph.getTargetNamespace());
        result.setId(graph.getIdentifier());

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

        result.setContentType(dataElement.getContentType());
        result.setType(dataElement.getType());
        result.setId(dataElement.getIdentifier());
        result.setDataObjectName(dataElement.getParent().getName());
        result.setEntity(dataElement.getEntity());
        result.setStatus(string2Status(dataElement.getState()));

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
        if (value.toUpperCase().equals(StatusEnum.CREATED.toString().toUpperCase())) {
            return StatusEnum.CREATED;
        } else if (value.toUpperCase().equals(StatusEnum.ARCHIVED.toString().toUpperCase())) {
            return StatusEnum.ARCHIVED;
        } else if (value.toUpperCase().equals(StatusEnum.READY.toString().toUpperCase())) {
            return StatusEnum.READY;
        }

        return null;
    }

    public static InstanceStatusEnum string2InstanceStatus(String value) {
        if (value.toUpperCase().equals(InstanceStatusEnum.CREATED.toString().toUpperCase())) {
            return InstanceStatusEnum.CREATED;
        } else if (value.toUpperCase().equals(InstanceStatusEnum.ARCHIVED.toString().toUpperCase())) {
            return InstanceStatusEnum.ARCHIVED;
        } else if (value.toUpperCase().equals(InstanceStatusEnum.INITIALIZED.toString().toUpperCase())) {
            return InstanceStatusEnum.INITIALIZED;
        }

        return null;
    }
}

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

    public static org.trade.core.model.data.DataDependencyGraph resource2Model(DataDependencyGraphData dataDependencyGraphData) {
        org.trade.core.model.data.DataDependencyGraph graph = new org.trade.core.model.data.DataDependencyGraph
                (dataDependencyGraphData.getEntity(), dataDependencyGraphData.getName());

        return graph;
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

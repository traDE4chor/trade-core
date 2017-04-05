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

import de.slub.urn.URNSyntaxException;
import io.swagger.trade.server.jersey.model.DataValue;
import io.swagger.trade.server.jersey.model.DataValueData;
import io.swagger.trade.server.jersey.model.StatusEnum;

/**
 * Created by hahnml on 26.01.2017.
 */
public class ResourceTransformationUtils {

    public static DataValue model2Resource(org.trade.core.model.data.DataValue dataValue) {
        DataValue value = new DataValue();

        /* TODO: Using URNs as identifiers of resources (maybe inside URL paths/queries) is not a good idea!?
        * Therefore, the question is if we should change the underlying model so that each model element has a UUID
        * as identifier and the URN is only used for internal purposes?
        */

        //value.setId(dataValue.getIdentifier());
        value.setId(dataValue.getName());
        value.setName(dataValue.getHumanReadableName());

        value.setType(dataValue.getType());
        value.setContentType(dataValue.getContentType());

        value.setCreated(dataValue.getCreationTimestamp());
        value.setLastModified(dataValue.getLastModified());

        value.setCreatedBy(dataValue.getOwner());
        value.setSize(dataValue.getSize());

        value.setStatus(model2resource(dataValue.getState()));

        return value;
    }

    public static org.trade.core.model.data.DataValue resource2Model(DataValueData dataValue) throws
            URNSyntaxException {
        org.trade.core.model.data.DataValue value = new org.trade.core.model.data.DataValue(dataValue.getCreatedBy());

        value.setHumanReadableName(dataValue.getName());
        value.setContentType(dataValue.getContentType());
        value.setType(dataValue.getType());

        return value;
    }

    public static StatusEnum model2resource(String value) {
        if (value.equals(StatusEnum.CREATED.toString())) {
            return StatusEnum.CREATED;
        } else if (value.equals(StatusEnum.ARCHIVED.toString())) {
            return StatusEnum.ARCHIVED;
        } else if (value.equals(StatusEnum.READY.toString())) {
            return StatusEnum.READY;
        }

        return null;
    }
}

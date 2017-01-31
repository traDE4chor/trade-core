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
import io.swagger.trade.server.jersey.model.DataValueRequest;
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
        value.setName(dataValue.getName());

        value.setType(dataValue.getType());
        value.setContentType(dataValue.getContentType());

        value.setCreated(dataValue.getCreationTimestamp());
        value.setLastModified(dataValue.getLastModified());

        value.setCreatedBy(dataValue.getOwner());
        value.setSize(dataValue.getSize());

        value.setStatus(StatusEnum.valueOf(dataValue.getState()));

        return value;
    }

    public static org.trade.core.model.data.DataValue resource2Model(DataValueRequest dataValue) throws
            URNSyntaxException {
        org.trade.core.model.data.DataValue value = new org.trade.core.model.data.DataValue(dataValue.getCreatedBy(),
                null);

        value.setContentType(dataValue.getContentType());
        value.setType(dataValue.getType());

        return value;
    }
}

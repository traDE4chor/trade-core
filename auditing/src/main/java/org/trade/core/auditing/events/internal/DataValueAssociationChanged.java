/*
 * Copyright 2018 Michael Hahn
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

package org.trade.core.auditing.events.internal;

import org.trade.core.auditing.events.ATraDEEvent;

/**
 * This class represent an internal event which is thrown if the associations to data element instances of a data
 * value change.
 *
 * Created by hahnml on 26.01.2018.
 */
public class DataValueAssociationChanged extends ATraDEEvent {

    private static final long serialVersionUID = -8614264082841776543L;

    @Override
    public TYPE getType() {
        return TYPE.internal;
    }

    public DataValueAssociationChanged(String dataValueIdentifier, Class modelClass, Object dataValueObject) {
        this.identifier = dataValueIdentifier;
        this.modelClass = modelClass;
        this.eventSource = dataValueObject;
    }

}

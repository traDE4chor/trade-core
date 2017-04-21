/* Copyright 2017 Michael Hahn
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

package org.trade.core.auditing.events;

import org.trade.core.utils.InstanceEvents;
import org.trade.core.utils.InstanceStates;

/**
 * Created by hahnml on 21.04.2017.
 */
public class InstanceStateChangeEvent extends TraDEEvent {

    private String identifier;
    private Class modelClass;
    private InstanceStates newState;
    private InstanceEvents event;

    public InstanceStateChangeEvent(String identifier, Class modelClass, String newState, String event) {
        this.identifier = identifier;
        this.modelClass = modelClass;
        this.newState = InstanceStates.valueOf(newState);
        this.event = InstanceEvents.valueOf(event);
    }

    @Override
    public TYPE getType() {
        return TYPE.instanceLifecycle;
    }

    public String getIdentifier() {
        return identifier;
    }

    public Class getModelClass() {
        return modelClass;
    }

    public InstanceStates getNewState() {
        return newState;
    }

    public InstanceEvents getEvent() {
        return event;
    }
}

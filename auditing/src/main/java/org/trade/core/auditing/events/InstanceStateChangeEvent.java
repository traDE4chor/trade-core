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

import org.trade.core.utils.events.InstanceEvents;
import org.trade.core.utils.states.InstanceStates;

import java.util.List;

/**
 * This class represents the event of a state change of a model instance object within the middleware.
 * <p>
 * Created by hahnml on 21.04.2017.
 */
public class InstanceStateChangeEvent extends ATraDEEvent {

    private static final long serialVersionUID = -2370986189631041880L;

    private static final String EVENT_FILTER__OLD_STATE = "OldState";
    private static final String EVENT_FILTER__NEW_STATE = "NewState";
    private static final String EVENT_FILTER__EVENT = "Event";

    private InstanceStates oldState;
    private InstanceStates newState;
    private InstanceEvents event;

    public InstanceStateChangeEvent(String identifier, Class modelClass, String oldState, String newState, String
            event) {
        this.identifier = identifier;
        this.modelClass = modelClass;
        this.oldState = InstanceStates.valueOf(oldState);
        this.newState = InstanceStates.valueOf(newState);
        this.event = InstanceEvents.valueOf(event);
    }

    @Override
    public TYPE getType() {
        return TYPE.instanceLifecycle;
    }

    public InstanceStates getOldState() {
        return oldState;
    }

    public InstanceStates getNewState() {
        return newState;
    }

    public InstanceEvents getEvent() {
        return event;
    }

    protected static List<EventFilterInformation> getPossibleEventFilters() {
        // Add the generic filters
        List<EventFilterInformation> result = ATraDEEvent.getPossibleEventFilters(InstanceStateChangeEvent.class);

        // Add the specific filters of this event class
        EventFilterInformation oldState = new EventFilterInformation(InstanceStateChangeEvent.class
                .getSimpleName(), EVENT_FILTER__OLD_STATE, "With the event filter key [" + EVENT_FILTER__OLD_STATE +
                "] the " +
                "old state value of model instance objects can be specified. This is useful if someone is " +
                "interested in events emitted as a reason of a specific state of model instance objects, e.g., the " +
                "state of a data value changed from 'CREATED' to 'INITIALIZED' after data was uploaded for its. By" +
                " specifying a filter with '" + EVENT_FILTER__OLD_STATE + "=INITIALIZED' only event messages with a" +
                " matching old state are forwarded.\n" +
                "The following list contains all valid state values that can be specified: CREATED, INITIALIZED, " +
                "ARCHIVED, DELETED.");
        oldState.setConstrainedValueDomain(getEnumValues(InstanceStates.class));
        result.add(oldState);

        EventFilterInformation newState = new EventFilterInformation(InstanceStateChangeEvent.class
                .getSimpleName(), EVENT_FILTER__NEW_STATE, "With the event filter key [" + EVENT_FILTER__NEW_STATE +
                "] the " +
                "new state value of model instance objects can be specified. This is useful if someone is " +
                "interested in events emitted as a reason of a specific state of model instance objects, e.g., the " +
                "state of a data value changed to 'INITIALIZED' after data was uploaded for its. By" +
                " specifying a filter with '" + EVENT_FILTER__NEW_STATE + "=INITIALIZED' only event messages with a" +
                " matching new state are forwarded.\n" +
                "The following list contains all valid state values that can be specified: CREATED, INITIALIZED, " +
                "ARCHIVED, DELETED.");
        newState.setConstrainedValueDomain(getEnumValues(InstanceStates.class));
        result.add(newState);

        EventFilterInformation event = new EventFilterInformation(InstanceStateChangeEvent.class.getSimpleName(),
                EVENT_FILTER__EVENT,
                "With the event filter key [" +
                EVENT_FILTER__EVENT + "] the " +
                "event which triggers a state change of a model instance object can be specified. This is useful if " +
                "someone " +
                "is interested in events emitted as a reason of a specific event trigger, e.g., the 'initialize' trigger" +
                " was called for a data value (which will lead to a state change to 'INITIALIZED'). By" +
                " specifying a filter with '" + EVENT_FILTER__EVENT + "=initialize' only event messages with a" +
                " matching event trigger are forwarded.\n" +
                "The following list contains all valid event values that can be specified: create, initialize, " +
                "archive, unarchive, delete.");
        event.setConstrainedValueDomain(getEnumValues(InstanceEvents.class));
        result.add(event);

        return result;
    }
}

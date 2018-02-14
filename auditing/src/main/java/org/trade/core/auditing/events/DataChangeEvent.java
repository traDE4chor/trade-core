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

package org.trade.core.auditing.events;

import org.trade.core.utils.events.DataEvents;
import org.trade.core.utils.states.DataStates;

import java.util.List;

/**
 * This class represents the event of a change (state and value) of a data object within the middleware. For example,
 * regarding the data associated to a data value model object.
 * <p>
 * Created by hahnml on 11.05.2017.
 */
public class DataChangeEvent extends ATraDEEvent {

    private static final long serialVersionUID = 6482354215019713061L;

    public static final String EVENT_FILTER__OLD_STATE = "OldState";
    public static final String EVENT_FILTER__NEW_STATE = "NewState";
    public static final String EVENT_FILTER__EVENT = "Event";

    private DataStates oldState;
    private DataStates newState;
    private DataEvents event;

    public DataChangeEvent(String identifier, Class modelClass, Object eventSource, String oldState, String
            newState, String event) {
        this.identifier = identifier;
        this.modelClass = modelClass;
        this.eventSource = eventSource;
        this.oldState = oldState != null ? DataStates.valueOf(oldState) : null;
        this.newState = newState != null ? DataStates.valueOf(newState) : null;
        this.event = event != null ? DataEvents.valueOf(event) : null;
    }

    @Override
    public ATraDEEvent.TYPE getType() {
        return TYPE.data;
    }

    public DataStates getOldState() {
        return oldState;
    }

    public DataStates getNewState() {
        return newState;
    }

    public DataEvents getEvent() {
        return event;
    }

    protected static List<EventFilterInformation> getPossibleEventFilters() {
        // Add the generic filters
        List<EventFilterInformation> result = ATraDEEvent.getPossibleEventFilters(DataChangeEvent.class);

        // Add the specific filters of this event class
        EventFilterInformation oldState = new EventFilterInformation(DataChangeEvent.class.getSimpleName(),
                EVENT_FILTER__OLD_STATE,
                "With the event filter key [" + EVENT_FILTER__OLD_STATE + "] the " +
                        "old state value of data state changes can be specified. This is useful if someone is " +
                        "interested in events emitted as a reason of a specific state of data, e.g., the " +
                        "state of data associated to a data value changed from 'NO_VALUE' to 'INITIALIZED' " +
                        "after a write attempt. " +
                        "By specifying a filter with '" + EVENT_FILTER__OLD_STATE + "=NO_VALUE' only event messages with a" +
                        " matching old state are forwarded.\n" +
                        "The following list contains all valid state values that can be specified: NO_VALUE, INITIALIZED, ARCHIVED.");
        oldState.setConstrainedValueDomain(getEnumValues(DataStates.class));
        result.add(oldState);

        EventFilterInformation newState = new EventFilterInformation(DataChangeEvent.class.getSimpleName(),
                EVENT_FILTER__NEW_STATE,
                "With the event filter key [" + EVENT_FILTER__NEW_STATE + "] the " +
                        "new state value of data state changes can be specified. This is useful if someone is " +
                        "interested in events emitted as a reason of a specific state of data, e.g., the " +
                        "state of data associated to a data value changed to 'INITIALIZED' after a write attempt. " +
                        "By specifying a filter with '" + EVENT_FILTER__NEW_STATE + "=INITIALIZED' only event messages with a" +
                        " matching new state are forwarded.\n" +
                        "The following list contains all valid state values that can be specified: NO_VALUE, INITIALIZED, ARCHIVED.");
        newState.setConstrainedValueDomain(getEnumValues(DataStates.class));
        result.add(newState);

        EventFilterInformation event = new EventFilterInformation(DataChangeEvent.class.getSimpleName(),
                EVENT_FILTER__EVENT,
                "With the event filter key [" +
                        EVENT_FILTER__EVENT + "] the " +
                        "event which triggers a state change of data can be specified. This is useful if someone " +
                        "is interested in events emitted as a reason of a specific event trigger, e.g., the 'initialize' trigger" +
                        " was called for data of a data value (which will lead to a state change to 'INITIALIZED')." +
                        " By specifying a filter with '" + EVENT_FILTER__EVENT + "=initialize' only event messages with a" +
                        " matching event trigger are forwarded.\n" +
                        "The following list contains all valid event values that can be specified: initialize, update, delete, archive, unarchive.");
        event.setConstrainedValueDomain(getEnumValues(DataEvents.class));
        result.add(event);

        return result;
    }
}

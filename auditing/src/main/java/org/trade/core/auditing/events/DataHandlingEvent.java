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

import org.trade.core.utils.states.DataStates;

import java.util.List;

/**
 * This class represents the event of a change (state and value) of a data object within the middleware. For example,
 * regarding the data associated to a data value model object.
 * <p>
 * Created by hahnml on 11.05.2017.
 */
public class DataHandlingEvent extends ATraDEEvent {

    private static final long serialVersionUID = 6482354215019713061L;

    private static final String EVENT_FILTER__OLD_STATE = "OldState";
    private static final String EVENT_FILTER__NEW_STATE = "NewState";

    private DataStates oldState;
    private DataStates newState;

    // TODO: 11.05.2017 Add required properties for data handling events

    public DataHandlingEvent(String identifier, Class modelClass, String oldState, String newState) {
        this.identifier = identifier;
        this.modelClass = modelClass;
        this.oldState = oldState != null ? DataStates.valueOf(oldState) : null;
        this.newState = newState != null ? DataStates.valueOf(newState) : null;
    }

    @Override
    public ATraDEEvent.TYPE getType() {
        return TYPE.dataHandling;
    }

    public DataStates getOldState() {
        return oldState;
    }

    public DataStates getNewState() {
        return newState;
    }

    protected static List<EventFilterInformation> getPossibleEventFilters() {
        // Add the generic filters
        List<EventFilterInformation> result = ATraDEEvent.getPossibleEventFilters(DataHandlingEvent.class);

        // Add the specific filters of this event class
        EventFilterInformation oldState = new EventFilterInformation(DataHandlingEvent.class.getSimpleName(),
                EVENT_FILTER__OLD_STATE,
                "With the event filter key [" + EVENT_FILTER__OLD_STATE + "] the " +
                        "old state value of data can be specified. This is useful if someone is " +
                        "interested in events emitted as a reason of a specific state of data, e.g., the " +
                        "state of data associated to a data value changed from 'WRITING' to 'PROCESSING_FAULTED' " +
                        "after a write attempt. " +
                        "By specifying a filter with '" + EVENT_FILTER__OLD_STATE + "=PROCESSING_FAULTED' only event messages with a" +
                        " matching old state are forwarded.\n" +
                        "The following list contains all valid state values that can be specified: INITIALIZED, WRITING, " +
                        "READING, PROCESSING_FAULTED, ARCHIVED, UNARCHIVED.");
        oldState.setConstrainedValueDomain(getEnumValues(DataStates.class));
        result.add(oldState);

        EventFilterInformation newState = new EventFilterInformation(DataHandlingEvent.class.getSimpleName(),
                EVENT_FILTER__NEW_STATE,
                "With the event filter key [" + EVENT_FILTER__NEW_STATE + "] the " +
                "new state value of data objects can be specified. This is useful if someone is " +
                "interested in events emitted as a reason of a specific state of data objects, e.g., the " +
                "state of data associated to a data value changed to 'PROCESSING_FAULTED' after a write attempt. " +
                "By specifying a filter with '" + EVENT_FILTER__NEW_STATE + "=PROCESSING_FAULTED' only event messages with a" +
                " matching new state are forwarded.\n" +
                "The following list contains all valid state values that can be specified: INITIALIZED, WRITING, " +
                "READING, PROCESSING_FAULTED, ARCHIVED, UNARCHIVED.");
        newState.setConstrainedValueDomain(getEnumValues(DataStates.class));
        result.add(newState);

        return result;
    }
}

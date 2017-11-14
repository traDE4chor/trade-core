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

import org.trade.core.utils.events.ModelEvents;
import org.trade.core.utils.states.ModelStates;

import java.util.List;

/**
 * This class represents the event of a state change of a model object within the middleware.
 * <p>
 * Created by hahnml on 21.04.2017.
 */
public class ModelStateChangeEvent extends ATraDEEvent {

    private static final long serialVersionUID = -1975865994826066258L;

    public static final String EVENT_FILTER__OLD_STATE = "OldState";
    public static final String EVENT_FILTER__NEW_STATE = "NewState";
    public static final String EVENT_FILTER__EVENT = "Event";

    private ModelStates oldState;
    private ModelStates newState;
    private ModelEvents event;

    public ModelStateChangeEvent(String identifier, Class modelClass, Object eventSource, String oldState, String
            newState, String event) {
        this.identifier = identifier;
        this.modelClass = modelClass;
        this.eventSource = eventSource;
        this.oldState = oldState != null ? ModelStates.valueOf(oldState) : null;
        this.newState = newState != null ? ModelStates.valueOf(newState) : null;
        this.event = ModelEvents.valueOf(event);
    }

    @Override
    public TYPE getType() {
        return TYPE.modelLifecycle;
    }

    public ModelStates getNewState() {
        return newState;
    }

    public ModelStates getOldState() {
        return oldState;
    }

    public ModelEvents getEvent() {
        return event;
    }

    protected static List<EventFilterInformation> getPossibleEventFilters() {
        // Add the generic filters
        List<EventFilterInformation> result = ATraDEEvent.getPossibleEventFilters(ModelStateChangeEvent.class);

        // Add the specific filters of this event class
        EventFilterInformation oldState = new EventFilterInformation(ModelStateChangeEvent.class.getSimpleName(),
                EVENT_FILTER__OLD_STATE, "With the " +
                "event filter key [" + EVENT_FILTER__OLD_STATE + "] the " +
                "old state value of model objects can be specified. This is useful if someone is " +
                "interested in events emitted as a reason of a specific state of model objects, e.g., the " +
                "state of a data element changed from 'INITIAL' to 'READY' after its successful initialization. " +
                "By specifying a filter with '" + EVENT_FILTER__OLD_STATE + "=READY' only event messages with a" +
                " matching old state are forwarded.\n" +
                "The following list contains all valid state values that can be specified: INITIAL, " +
                "READY, ARCHIVED, DELETED.");
        oldState.setConstrainedValueDomain(getEnumValues(ModelStates.class));
        result.add(oldState);

        EventFilterInformation newState = new EventFilterInformation(ModelStateChangeEvent.class.getSimpleName(),
                EVENT_FILTER__NEW_STATE, "With the " +
                "event filter key [" + EVENT_FILTER__NEW_STATE + "] the " +
                "new state value of model objects can be specified. This is useful if someone is " +
                "interested in events emitted as a reason of a specific state of model objects, e.g., the " +
                "state of a data element changed to 'READY' after its successful initialization. By" +
                " specifying a filter with '" + EVENT_FILTER__NEW_STATE + "=READY' only event messages with a" +
                " matching new state are forwarded.\n" +
                "The following list contains all valid state values that can be specified: INITIAL, " +
                "READY, ARCHIVED, DELETED.");
        newState.setConstrainedValueDomain(getEnumValues(ModelStates.class));
        result.add(newState);

        EventFilterInformation event = new EventFilterInformation(ModelStateChangeEvent.class.getSimpleName(),
                EVENT_FILTER__EVENT, "With the event filter key [" +
                EVENT_FILTER__EVENT + "] the " +
                "event which triggers a state change of a model object can be specified. This is useful if someone " +
                "is interested in events emitted as a reason of a specific event trigger, e.g., the 'archive' trigger" +
                " was called for a data element (which will lead to a state change to 'ARCHIVED'). By" +
                " specifying a filter with '" + EVENT_FILTER__EVENT + "=archive' only event messages with a" +
                " matching event trigger are forwarded.\n" +
                "The following list contains all valid event values that can be specified: initial, ready, archive, " +
                "unarchive, delete.");
        event.setConstrainedValueDomain(getEnumValues(ModelEvents.class));
        result.add(event);

        return result;
    }
}

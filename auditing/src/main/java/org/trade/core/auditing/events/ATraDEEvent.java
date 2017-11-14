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

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.*;

/**
 * This abstract class defines basic properties and required methods for events within the TraDE middleware used for
 * auditing and event-based processing of changes within the middleware, e.g., to enable the notification of
 * interested parties.
 * <p>
 * Created by hahnml on 21.04.2017.
 */
public abstract class ATraDEEvent implements Serializable {

    private static final long serialVersionUID = 6896931663625198230L;

    public static final String EVENT_FILTER__IDENTIFIER = "Identifier";

    public static final String EVENT_FILTER__MODEL_CLASS = "ModelClass";

    public static final String EVENT_FILTER__EVENT_SOURCE = "EventSource";

    protected String identifier;

    protected Class modelClass;

    protected Object eventSource;

    /**
     * This enum provides the list of possible event types.
     */
    public enum TYPE {
        /**
         * Data handling type.
         */
        dataHandling, /**
         * Model lifecycle type.
         */
        modelLifecycle, /**
         * Instance lifecycle type.
         */
        instanceLifecycle
    }

    private Date timestamp = new Date();

    /**
     * Gets creation timestamp of an event.
     *
     * @return the timestamp
     */
    public Date getTimestamp() {
        return timestamp;
    }

    public String getIdentifier() {
        return identifier;
    }

    /**
     * Provides the model class of the object which is the source of this event.
     *
     * @return The class of the model object.
     */
    public Class getModelClass() {
        return modelClass;
    }

    /**
     * Provides the source object of this event.
     *
     * @return The source object.
     */
    public Object getEventSource() {
        return eventSource;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(eventName(this) + ":");

        Method[] methods = getClass().getMethods();
        for (Method method : methods) {
            // Stringify all public getter methods without arguments of the clazz and its supertype while ignoring the
            // getter for all event filters
            if (!(method.getName().startsWith("getPossibleEventFilters") || method.getName().startsWith
                    ("getAllPossibleEventFilters")) && method.getName()
                    .startsWith("get") && method.getParameterTypes().length == 0) {
                try {
                    String field = method.getName().substring(3);
                    Object value = method.invoke(this);
                    if (value == null) {
                        continue;
                    }

                    // Special handling for the event source object for which we just add the hashCode
                    if (field.startsWith
                            ("EventSource")) {
                        sb.append("\n\t").append(field).append(" = ")
                                .append(value.hashCode());
                    } else {
                        sb.append("\n\t").append(field).append(" = ")
                                .append(value.toString());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return sb.toString();
    }

    /**
     * Provides the event name as string.
     *
     * @param event the event
     * @return the name of the event as string
     */
    public static String eventName(ATraDEEvent event) {
        String name = event.getClass().getName();
        return name.substring(name.lastIndexOf('.') + 1);
    }

    /**
     * Gets the type of the event.
     *
     * @return the event type
     */
    public abstract TYPE getType();

    // Keep this method (especially the descriptions in the map) and the constrained modelClass values always in sync
    // with the actual implementation(s)...
    protected static List<EventFilterInformation> getPossibleEventFilters(Class<? extends ATraDEEvent> eventClass) {
        List<EventFilterInformation> result = new ArrayList<>();

        // The following list of keys and values can be used to specify a set of filters on different types of events
        // as a basis for triggering notifications on events matching the specified filters. Therefore, possible
        // filter keys and examples for valid filter values are provided in the following in form of
        // EventFilterInformation objects.

        EventFilterInformation identifier = new EventFilterInformation(eventClass.getSimpleName(),
                EVENT_FILTER__IDENTIFIER,
                "With the " +
                        "event filter key [" + EVENT_FILTER__IDENTIFIER + "] the " +
                        "identifier value of model objects can be specified. This can be used if someone is " +
                        "interested in events emitted for one specific model object, e.g., a data value with " +
                        "identifier 'dataValueId'. By specifying a filter with '" + EVENT_FILTER__IDENTIFIER + "=dataValueId' " +
                        "only event messages with a matching identifier are forwarded.");
        result.add(identifier);

        EventFilterInformation modelClass = new EventFilterInformation(eventClass.getSimpleName(),
                EVENT_FILTER__MODEL_CLASS, "With the event filter key [" + EVENT_FILTER__MODEL_CLASS + "] the " +
                "class of model objects can be specified. This is useful if someone is " +
                "interested in events emitted for a specific type of model object, e.g., events about data values. By" +
                " specifying a filter with '" + EVENT_FILTER__MODEL_CLASS + "=class org.trade.core.model.data" +
                ".DataValue' only event messages with a matching model class are forwarded.");
        modelClass.addValueToConstrainedValueDomain("class org.trade.core.model.data.DataDependencyGraph");
        modelClass.addValueToConstrainedValueDomain("class org.trade.core.model.data.DataModel");
        modelClass.addValueToConstrainedValueDomain("class org.trade.core.model.data.DataObject");
        modelClass.addValueToConstrainedValueDomain("class org.trade.core.model.data.DataElement");
        modelClass.addValueToConstrainedValueDomain("class org.trade.core.model.data.DataValue");
        modelClass.addValueToConstrainedValueDomain("class org.trade.core.model.data.instance.DataObjectInstance");
        modelClass.addValueToConstrainedValueDomain("class org.trade.core.model.data.instance.DataElementInstance");
        result.add(modelClass);

        EventFilterInformation eventSource = new EventFilterInformation(eventClass.getSimpleName(),
                EVENT_FILTER__EVENT_SOURCE, "With the event filter key [" + EVENT_FILTER__EVENT_SOURCE + "] the " +
                "actual model object can be referenced. This is useful if someone is " +
                "interested in actual properties of a model object for which an event was emitted, e.g., " +
                "the name of a data object. Therefore, the event filter key has to be extended with a 'nested query'," +
                " i.e., by adding a '#' and then the actual method identifier of the model object to get the field of" +
                " the object which should be compared with the specified filter value." +
                " By specifying a filter with '" + EVENT_FILTER__EVENT_SOURCE + "#Name=inputDataObject' for a " +
                "data object only event messages with for data objects with the name 'inputDataObject' are forwarded.");
        result.add(eventSource);

        return result;
    }

    /**
     * Provides a list of all possible event filters. Therefore, the possible event filters
     * of all classes extending {@link ATraDEEvent} are collected to an overall list. New subclasses (event types)
     * should be added to the resulting list, so that the overall map of possible event filters is resolved correctly.
     *
     * @return the names and descriptions of all possible event filters.
     */
    public static List<EventFilterInformation> getAllPossibleEventFilters() {
        List<EventFilterInformation> result = new ArrayList<>();

        result.addAll(ModelStateChangeEvent.getPossibleEventFilters());

        result.addAll(InstanceStateChangeEvent.getPossibleEventFilters());

        result.addAll(DataHandlingEvent.getPossibleEventFilters());

        return result;
    }

    protected static <E extends Enum<E>> List<String> getEnumValues(Class<E> clazz) {
        List<String> result = new ArrayList<>();

        for (E en : EnumSet.allOf(clazz)) {
            result.add(en.name());
        }

        return result;
    }
}

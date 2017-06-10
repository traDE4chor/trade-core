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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * This class represents information about an event filter. This comprises the name of the filter, a description of
 * the filter, the type of event the filter can be applied to and the set of allowed values, if the value domain is
 * constrained, e.g., a filter with enum constants as values.
 * <p>
 * Created by hahnml on 15.05.2017.
 */
public class EventFilterInformation {

    private String eventType;

    private String description;

    private String filterName;

    private List<String> constrainedValueDomain = new ArrayList<>();

    public EventFilterInformation(String eventType, String filterName, String description) {
        this.eventType = eventType;
        this.description = description;
        this.filterName = filterName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFilterName() {
        return filterName;
    }

    public void setFilterName(String filterName) {
        this.filterName = filterName;
    }

    public List<String> getConstrainedValueDomain() {
        return constrainedValueDomain;
    }

    public void addValueToConstrainedValueDomain(String value) {
        this.constrainedValueDomain.add(value);
    }

    public void setConstrainedValueDomain(List<String> constrainedValueDomain) {
        this.constrainedValueDomain = constrainedValueDomain;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("EventFilterInformation:");

        Method[] methods = getClass().getMethods();
        for (Method method : methods) {
            // Stringify all public getter methods without arguments of the clazz and its supertype while ignoring the
            // getter for all event filters
            if (method.getName()
                    .startsWith("get") && method.getParameterTypes().length == 0) {
                try {
                    String field = method.getName().substring(3);
                    Object value = method.invoke(this);
                    if (value == null) {
                        continue;
                    }
                    sb.append("\n\t").append(field).append(" = ")
                            .append(value.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return sb.toString();
    }
}

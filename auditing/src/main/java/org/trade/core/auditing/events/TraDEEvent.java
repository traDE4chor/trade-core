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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Date;

/**
 * Created by hahnml on 21.04.2017.
 */
public abstract class TraDEEvent implements Serializable {

    Logger logger = LoggerFactory.getLogger("org.trade.core.auditing.events.TraDEEvent");

    public enum TYPE {
        dataHandling, modelLifecycle, instanceLifecycle
    }

    private Date _timestamp = new Date();

    public Date getTimestamp() {
        return _timestamp;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("\n" + eventName(this) + ":");

        Method[] methods = getClass().getMethods();
        for (Method method : methods) {
            if (method.getName().startsWith("get")
                    && method.getParameterTypes().length == 0) {
                try {
                    String field = method.getName().substring(3);
                    Object value = method.invoke(this,
                            new Object[]{});
                    if (value == null) {
                        continue;
                    }
                    sb.append("\n\t").append(field).append(" = ")
                            .append(value == null ? "null" : value.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return sb.toString();
    }

    public static String eventName(TraDEEvent event) {
        String name = event.getClass().getName();
        return name.substring(name.lastIndexOf('.') + 1);
    }

    public abstract TYPE getType();
}

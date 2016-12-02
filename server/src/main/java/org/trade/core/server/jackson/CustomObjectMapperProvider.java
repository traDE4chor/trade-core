/*
 * Copyright 2016 Michael Hahn
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

package org.trade.core.server.jackson;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import java.lang.reflect.Field;

/**
 * Created by hahnml on 02.12.2016.
 */
@Provider
public class CustomObjectMapperProvider implements ContextResolver<ObjectMapper> {

    final ObjectMapper defaultObjectMapper;
    final ObjectMapper enumObjectMapper;

    public CustomObjectMapperProvider() {
        defaultObjectMapper = createDefaultMapper();
        enumObjectMapper = createEnumMapper();
    }

    @Override
    public ObjectMapper getContext(final Class<?> type) {

        if (type.isEnum() || containsEnum(type)) {
            return enumObjectMapper;
        } else {
            return defaultObjectMapper;
        }

    }

    private boolean containsEnum(Class<?> type) {
        boolean found = false;

        Field[] fields = type.getDeclaredFields();
        int i = 0;
        while (!found && i < fields.length) {
            found = fields[i].getType().isEnum();
            i++;
        }

        return found;
    }

    private static ObjectMapper createDefaultMapper() {
        final ObjectMapper result = new ObjectMapper();
        result.enable(SerializationFeature.INDENT_OUTPUT);

        return result;
    }

    private static ObjectMapper createEnumMapper() {
        final ObjectMapper result = new ObjectMapper();
        result.enable(SerializationFeature.INDENT_OUTPUT);

        // Uses Enum.toString() for serialization of an Enum
        result.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
        result.enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING);

        return result;
    }
}

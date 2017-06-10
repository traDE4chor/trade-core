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

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapperBuilder;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import org.junit.Before;
import org.junit.Test;
import org.trade.core.auditing.events.InstanceStateChangeEvent;
import org.trade.core.auditing.events.ModelStateChangeEvent;
import org.trade.core.model.data.DataElement;
import org.trade.core.model.notification.Notification;
import org.trade.core.utils.events.ModelEvents;
import org.trade.core.utils.states.ModelStates;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by hahnml on 12.05.2017.
 */
public class MessageGenerationTest {

    private Configuration configuration;

    @Before
    public void createConfiguration() {
        String messageTemplateDir = "/notificationMsgTemplates";

        Path dir = Paths.get(messageTemplateDir);

        // Check if the path is relative to make it absolute if required
        if (!dir.isAbsolute()) {
            dir = Paths.get("..", messageTemplateDir).toAbsolutePath().normalize();
        }

        configuration = new Configuration(Configuration.VERSION_2_3_26);
        try {
            configuration.setDirectoryForTemplateLoading(dir.toFile());
        } catch (IOException e) {
            e.printStackTrace();
        }

        DefaultObjectWrapperBuilder owb = new DefaultObjectWrapperBuilder(Configuration.VERSION_2_3_26);

        configuration.setDefaultEncoding("UTF-8");
        configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        configuration.setLogTemplateExceptions(false);
        configuration.setObjectWrapper(owb.build());
    }

    @Test
    public void providedMessageShouldBeUsed() throws Exception {
        String message = "This is a test message.";
        Notification notification = new Notification("test", null, "http://localhost:8081/api/dataElements");
        ModelStateChangeEvent event = new ModelStateChangeEvent(UUID.randomUUID().toString(), DataElement.class,
                ModelStates.INITIAL.name(),
                ModelStates.READY.name(), ModelEvents.ready.name());

        String result = createMessage(message, notification, event, "plainMsg.ftl");

        assertEquals(message, result);
    }

    @Test
    public void messageShouldBeGenerated() throws Exception {
        Notification notification = new Notification("firstTest", null, "http://localhost:8081/api/dataElements");
        ModelStateChangeEvent event = new ModelStateChangeEvent(UUID.randomUUID().toString(), DataElement.class,
                ModelStates.INITIAL.name(), ModelStates.READY.name(), ModelEvents.ready.name());

        String result = createMessage(null, notification, event, "plainMsg.ftl");

        System.out.println(result);

        assertNotNull(result);
    }

    @Test
    public void messageForResourceShouldBeGenerated() throws Exception {
        DataElement element = new DataElement(null, "entity", "dataElementA");
        Notification notification = new Notification("secondTest", element,
                "http://localhost:8081/api/dataElements/" + element.getIdentifier());
        ModelStateChangeEvent event = new ModelStateChangeEvent(element.getIdentifier(), DataElement.class,
                ModelStates.INITIAL.name(), ModelStates.READY.name(), ModelEvents.ready.name());

        String result = createMessage(null, notification, event, "plainMsg.ftl");

        System.out.println(result);

        assertNotNull(result);
    }

    @Test
    public void minimalMessageShouldBeGenerated() throws Exception {
        Notification notification = new Notification("thirdTest", null, null);
        ModelStateChangeEvent event = new ModelStateChangeEvent(UUID.randomUUID().toString(), DataElement.class,
                ModelStates.INITIAL.name(), ModelStates.READY.name(), ModelEvents.ready.name());

        String result = createMessage(null, notification, event, "plainMsg.ftl");

        System.out.println(result);

        assertNotNull(result);
    }

    private String createMessage(String message, Notification notification, Object notificationSource, String
            templateName) throws Exception {
        String result = "";

        String resourceURL = notification.getResourceURL();

        if (message == null || message.isEmpty()) {
            // Generate a message
            if (notificationSource instanceof ModelStateChangeEvent || notificationSource instanceof
                    InstanceStateChangeEvent) {
                // Resolve the correct template
                Template temp = this.configuration.getTemplate(templateName);

                Map<String, Object> data = new HashMap<>();
                data.put("notification", notification);
                data.put("notificationSource", notificationSource);

                Writer out = new StringWriter();
                temp.process(data, out);
                result = out.toString();
            } else {
                result = notificationSource.toString() + "\n" + "TraDE URL: " + resourceURL;
            }
        } else {
            result = message;
        }

        return result;
    }
}

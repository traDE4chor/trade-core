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

import org.junit.Test;
import org.trade.core.auditing.events.*;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Tests for TraDE events.
 * <p>
 * Created by hahnml on 15.05.2017.
 */
public class ATraDEEventTest {

    @Test
    public void allTraDEEventClassesShouldBeReturned() throws Exception {
        List<EventFilterInformation> list = ATraDEEvent.getAllPossibleEventFilters();

        assertNotNull(list);

        List<EventFilterInformation> dataHandling = list.stream().filter(e -> e.getEventType().equals
                (DataHandlingEvent.class.getSimpleName())).collect(Collectors.toList());

        assertNotNull(dataHandling);
        assertEquals(5, dataHandling.size());

        List<EventFilterInformation> instanceState = list.stream().filter(e -> e.getEventType().equals
                (InstanceStateChangeEvent.class.getSimpleName())).collect(Collectors.toList());

        assertNotNull(instanceState);
        assertEquals(6, instanceState.size());

        List<EventFilterInformation> modelState = list.stream().filter(e -> e.getEventType().equals
                (ModelStateChangeEvent.class.getSimpleName())).collect(Collectors.toList());

        assertNotNull(modelState);
        assertEquals(6, modelState.size());

        for (EventFilterInformation filter : list) {
            System.out.println(filter.toString());
        }
    }

}

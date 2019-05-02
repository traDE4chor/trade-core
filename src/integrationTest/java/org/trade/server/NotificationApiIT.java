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

package org.trade.server;

import io.swagger.trade.client.jersey.ApiException;
import io.swagger.trade.client.jersey.model.*;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import static org.junit.Assert.*;

/**
 * Created by hahnml on 10.05.2017.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class NotificationApiIT {

    private static IntegrationTestEnvironment env;

    @BeforeClass
    public static void setupEnvironment() {
        env = new IntegrationTestEnvironment();
        env.setupEnvironment(true);
    }

    @Test
    public void createNotificationTest() throws Exception {
        NotificationData test = new NotificationData();
        test.setName("notifyDataValueInitialized");
        test.setTypeOfResourceToObserve(ResourceTypeEnum.DATAVALUE);
        test.setSelectedNotifierServiceId("http");

        NotifierServiceParameterArray params = new NotifierServiceParameterArray();

        NotifierServiceParameter param = new NotifierServiceParameter();
        param.setParameterName("hostname");
        param.setValue("127.0.0.1");
        params.add(param);

        param = new NotifierServiceParameter();
        param.setParameterName("port");
        param.setValue("8085");
        params.add(param);

        param = new NotifierServiceParameter();
        param.setParameterName("resourceUri");
        param.setValue("/testResource");
        params.add(param);

        test.setNotifierParameterValues(params);

        ResourceEventFilterArray filters = new ResourceEventFilterArray();

        ResourceEventFilter filter = new ResourceEventFilter();
        filter.setFilterName("ModelClass");
        filter.setFilterValue("class org.trade.core.model.data.DataValue");
        filters.add(filter);

        filter = new ResourceEventFilter();
        filter.setFilterName("NewState");
        filter.setFilterValue("INITIALIZED");
        filters.add(filter);

        test.setResourceFilters(filters);

        try {
            Notification response = env.getNotificationApi().addNotification(test);
            assertNotNull(response);

            env.getNotificationApi().deleteNotification(response.getId());
        } catch (ApiException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void shouldRejectAddNotificationTest() {
        // Try to add a new notification without 'name' value
        NotificationData test = new NotificationData();

        test.setTypeOfResourceToObserve(ResourceTypeEnum.DATAVALUE);
        test.setSelectedNotifierServiceId("http");

        try {
            env.getNotificationApi().addNotification(test);
        } catch (ApiException e) {
            e.printStackTrace();

            assertEquals(400, e.getCode());
        }
    }

    @Test
    public void shouldRejectDeleteNotificationTest() {
        try {
            // Try to delete a non existing notification
            env.getNotificationApi().deleteNotification("Not-Existing-Id");
        } catch (ApiException e) {
            e.printStackTrace();

            assertEquals(404, e.getCode());
        }
    }

    @Test
    public void updateNotificationTest() throws Exception {
        NotificationData test = new NotificationData();
        test.setName("notifyDataValueInitialized");
        test.setTypeOfResourceToObserve(ResourceTypeEnum.DATAVALUE);
        test.setSelectedNotifierServiceId("http");

        NotifierServiceParameterArray params = new NotifierServiceParameterArray();

        NotifierServiceParameter param = new NotifierServiceParameter();
        param.setParameterName("hostname");
        param.setValue("127.0.0.1");
        params.add(param);

        param = new NotifierServiceParameter();
        param.setParameterName("port");
        param.setValue("8085");
        params.add(param);

        param = new NotifierServiceParameter();
        param.setParameterName("resourceUri");
        param.setValue("/testResource");
        params.add(param);

        test.setNotifierParameterValues(params);

        ResourceEventFilterArray filters = new ResourceEventFilterArray();

        ResourceEventFilter filter = new ResourceEventFilter();
        filter.setFilterName("ModelClass");
        filter.setFilterValue("class org.trade.core.model.data.DataValue");
        filters.add(filter);

        filter = new ResourceEventFilter();
        filter.setFilterName("NewState");
        filter.setFilterValue("INITIALIZED");
        filters.add(filter);

        test.setResourceFilters(filters);

        Notification response = env.getNotificationApi().addNotification(test);
        assertNotNull(response);

        Notification updateRequest = new Notification();
        updateRequest.setName("changedName");
        updateRequest.setTypeOfResourceToObserve(ResourceTypeEnum.DATAVALUE);
        updateRequest.setSelectedNotifierServiceId("http");

        // Add an additional parameter
        param = new NotifierServiceParameter();
        param.setParameterName("messageFormat");
        param.setValue("XML");
        params.add(param);

        updateRequest.setNotifierParameterValues(params);

        updateRequest.setResourceFilters(filters);

        env.getNotificationApi().updateNotificationDirectly(response.getId(), updateRequest);

        NotificationWithLinks updated = env.getNotificationApi().getNotificationDirectly(response.getId());
        // Check unchanged properties
        assertEquals(response.getId(), updated.getNotification().getId());
        assertEquals(response.getResourceFilters(), updated.getNotification().getResourceFilters());
        assertEquals(response.getTypeOfResourceToObserve(), updated.getNotification().getTypeOfResourceToObserve());
        assertEquals(response.getSelectedNotifierServiceId(), updated.getNotification().getSelectedNotifierServiceId());

        TestUtils.printLinkArray(updated.getLinks());

        // Check changed properties
        assertNotEquals(response.getName(), updated.getNotification().getName());
        assertNotEquals(response.getNotifierParameterValues(), updated.getNotification().getNotifierParameterValues());

        env.getNotificationApi().deleteNotification(response.getId());

        try {
            // Try to retrieve the deleted notification
            env.getNotificationApi().getNotificationDirectly(response.getId());
        } catch (ApiException e) {
            e.printStackTrace();

            assertEquals(404, e.getCode());
        }
    }

    @AfterClass
    public static void destroy() {
        env.destroyEnvironment();
        env = null;
    }
}

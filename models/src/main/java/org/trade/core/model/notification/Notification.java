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

package org.trade.core.model.notification;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Transient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trade.core.auditing.events.ATraDEEvent;
import org.trade.core.model.ABaseResource;
import org.trade.core.persistence.IPersistenceProvider;
import org.trade.core.persistence.local.LocalPersistenceProviderFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * This class represents a notification within the middleware.
 * <p>
 * Created by hahnml on 07.04.2017.
 */
@Entity("notifications")
public class Notification extends ABaseResource {

    private static final long serialVersionUID = 4958320313665548748L;

    @Transient
    private Logger logger = LoggerFactory.getLogger("org.trade.core.model.notification.Notification");

    @JsonProperty("name")
    private String name = null;

    // The resource for which the notification is registered. Could be null, if the notification is defined based on
    // a collection of resources using corresponding filters instead of one concrete resource.
    @JsonProperty("resource")
    private ABaseResource resource = null;

    // The URL of the resource for which the notification is registered at the TraDE middleware. Could be null, if
    // the notification is defined based on a collection of resources using corresponding filters instead of one
    // concrete resource.
    @JsonProperty("resourceURL")
    private String resourceURL = null;

    // The id of a notifier service which will be used in order to conduct the notification.
    @JsonProperty("selectedNotifierServiceId")
    private String selectedNotifierServiceId = null;

    // A collection of notifier parameters (key, value) which are required to configure the selected notifier service.
    @JsonProperty("notifierParameters")
    private Map<String, String> notifierParameters = new HashMap<>();

    // A collection of resource filters (key, value) which are used to identify and filter changes on relevant resources
    // (based on events) (e.g., a data value is initialized) before forwarding them to the selected
    // notifier to trigger the provided notification logic.
    @JsonProperty("resourceFilters")
    private Map<String, String> resourceFilters = new HashMap<>();

    private transient IPersistenceProvider<Notification> persistProv;

    /**
     * This constructor is only used by Morphia to load objects from the database.
     */
    private Notification() {
        this.persistProv = LocalPersistenceProviderFactory.createLocalPersistenceProvider(Notification.class);
    }

    /**
     * Instantiates a new notification with the given name.
     *
     * @param name the name
     */
    public Notification(String name) {
        this(name, null, null);
    }

    /**
     * Instantiates a new notification with the given name, resource and resource URL.
     *
     * @param name        the name
     * @param resource    the resource for which the notification is registered. Could be null, if the notification
     *                    is defined based on a collection of resources using corresponding filters.
     * @param resourceURL the URL of the resource the notification is registered for, could be null
     */
    public Notification(String name, ABaseResource resource, String resourceURL) {
        this.name = name;
        this.resource = resource;

        if (resource != null) {
            // We translate the specified resource into a filter, so that we only have to take care of them during
            // filtering.
            addResourceFilter(ATraDEEvent.EVENT_FILTER__IDENTIFIER, resource.getIdentifier());
        }

        this.resourceURL = resourceURL;
        this.persistProv = LocalPersistenceProviderFactory.createLocalPersistenceProvider(Notification.class);
    }

    /**
     * Gets the name of a notification.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the notification.
     *
     * @param name the name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the URL of the resource or resource collection for which this notification is registered.
     *
     * @return the resource url
     */
    public String getResourceURL() {
        return resourceURL;
    }

    /**
     * Sets the URL of the resource or resource collection for which this notification is registered.
     *
     * @param resourceURL the resource url
     */
    public void setResourceURL(String resourceURL) {
        this.resourceURL = resourceURL;
    }

    /**
     * Gets the resource for which this notification is registered. Could be null, if the notification
     * is defined based on a collection of resources using corresponding filters.
     *
     * @return the resource
     */
    public ABaseResource getResource() {
        return resource;
    }

    /**
     * Sets the resource for which this notification is registered. Could be null, if the notification
     * is defined based on a collection of resources using corresponding filters.
     *
     * @param resource the resource
     */
    public void setResource(ABaseResource resource) {
        this.resource = resource;
    }

    /**
     * Gets the id of the notifier service which will be used in order to conduct the notification.
     *
     * @return the selected notifier service id
     */
    public String getSelectedNotifierServiceId() {
        return selectedNotifierServiceId;
    }

    /**
     * Sets the id of the notifier service which should be used in order to conduct the notification.
     *
     * @param selectedNotifierServiceId the id of the notifier service to use
     */
    public void setSelectedNotifierServiceId(String selectedNotifierServiceId) {
        this.selectedNotifierServiceId = selectedNotifierServiceId;
    }

    /**
     * Gets the collection of notifier parameters (key, value) which are used to configure the selected notifier
     * service.
     *
     * @return the notifier parameters
     */
    public Map<String, String> getNotifierParameters() {
        return notifierParameters;
    }

    /**
     * Adds a new notifier parameter to the map.
     *
     * @param key   the key (property of a notifier service) to add a new parameter for
     * @param value the value (of the property) for the specified key
     */
    public void addNotifierParameter(String key, String value) {
        notifierParameters.put(key, value);
    }

    /**
     * Sets the collection of notifier parameters (key, value) which should be used to configure the selected notifier
     * service.
     *
     * @param notifierParameters the notifier parameters
     */
    public void setNotifierParameters(Map<String, String> notifierParameters) {
        this.notifierParameters = notifierParameters;
    }

    /**
     * Gets all resource filters (key, value) which are used to identify and filter changes on relevant resources
     * (based on events) (e.g., a data value is initialized) before forwarding them to the selected
     * notifier to trigger the provided notification logic.
     * <p>
     * These resource filters are applied by comparing the values of all specified properties (key's) with the
     * values provided through the map. For example, a resource filter ("status", "READY") does only forward events
     * of resources which specify the property "status" and where the value of status equals "READY".
     *
     * @return the map of resource filters
     */
    public Map<String, String> getResourceFilters() {
        return resourceFilters;
    }

    /**
     * Adds a new resource filter.
     *
     * @param key   the key (property of a resource) to check
     * @param value the value (of the property) expected for the specified key
     */
    public void addResourceFilter(String key, String value) {
        resourceFilters.put(key, value);
    }

    /**
     * Sets a complete map of resource filters (key, value) which are used to identify and filter changes on relevant
     * resources (based on events) (e.g., a data value is initialized) before forwarding them to the selected
     * notifier to trigger the provided notification logic.
     * <p>
     * These resource filters are applied by comparing the values of all specified properties (key's) with the
     * values provided through the map. For example, a resource filter ("status", "READY") does only forward events
     * of resources which specify the property "status" and where the value of status equals "READY".
     *
     * @param resourceFilters the resource filters to set
     */
    public void setResourceFilters(Map<String, String> resourceFilters) {
        this.resourceFilters = resourceFilters;
    }

    @Override
    public void storeToDS() {
        if (this.persistProv != null) {
            try {
                this.persistProv.storeObject(this);
            } catch (Exception e) {
                logger.error("Storing notification '" + this.getIdentifier() + "' caused an exception.", e);
            }
        }
    }

    @Override
    public void deleteFromDS() {
        if (this.persistProv != null) {
            try {
                this.persistProv.deleteObject(this.getIdentifier());
            } catch (Exception e) {
                logger.error("Deleting notification '" + this.getIdentifier() + "' caused an exception.", e);
            }
        }
    }

    private void readObject(ObjectInputStream ois) throws IOException {
        try {
            ois.defaultReadObject();

            this.persistProv = LocalPersistenceProviderFactory.createLocalPersistenceProvider(Notification.class);
        } catch (ClassNotFoundException e) {
            logger.error("Class not found during deserialization of notification '{}'", getIdentifier());
            throw new IOException("Class not found during deserialization of notification.");
        }
    }
}

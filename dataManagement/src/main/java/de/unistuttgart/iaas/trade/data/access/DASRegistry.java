package de.unistuttgart.iaas.trade.data.access;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by hahnml on 25.10.2016.
 */
public class DASRegistry {

    private static DASRegistry ourInstance = new DASRegistry();

    /**
     * Provides access to the single instance of the data access service registry.
     *
     * @return The registry to register or query for data access services.
     */
    public static DASRegistry getInstance() {
        return ourInstance;
    }

    private DASRegistry() {
    }

    private HashMap<String, List<IDataAccessService>> accessServices = new HashMap<String, List<IDataAccessService>>();

    /**
     * Register a new data access service implementation for a given protocol.
     *
     * @param protocol The protocol the data access service provides an implementation for.
     * @param service  The instance of the service which should be registered.
     */
    public void registerDataAccessService(String protocol, IDataAccessService service) {
        if (this.accessServices.get(protocol) == null) {
            this.accessServices.put(protocol, new ArrayList<IDataAccessService>());
        }

        this.accessServices.get(protocol).add(service);
    }

    /**
     * Unregister a data access service implementation for a given protocol.
     *
     * @param protocol The protocol the data access service provides an implementation for.
     * @param service  The instance of the service which should be unregistered.
     * @return True, if the un-registration was successful, false otherwise.
     */
    public boolean unregisterDataAccessService(String protocol, IDataAccessService service) {
        boolean success = false;

        if (this.accessServices.containsKey(protocol)) {
            this.accessServices.get(protocol).remove(service);
            success = true;
        }

        return success;
    }

    /**
     * Provides a registered data access service for the given protocol.
     *
     * @param protocol The protocol for which a data access service implementation should be provided.
     * @return A corresponding data access service or NULL, if no service is registered for the requested protocol.
     */
    public IDataAccessService getDataAccessService(String protocol) {
        IDataAccessService result = null;

        if (this.accessServices.get(protocol) != null && !this.accessServices.get(protocol).isEmpty()) {
            result = this.accessServices.get(protocol).get(0);
        }

        return result;
    }

    /**
     * Provides all registered data access services for a given protocol.
     *
     * @param protocol The protocol for which data access service implementations should be provided.
     * @return A corresponding list of data access service or an empty list, if no services are registered for the requested protocol.
     */
    public List<IDataAccessService> getDataAccessServices(String protocol) {
        return this.accessServices.get(protocol);
    }
}

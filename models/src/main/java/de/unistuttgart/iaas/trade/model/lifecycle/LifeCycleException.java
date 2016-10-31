package de.unistuttgart.iaas.trade.model.lifecycle;

/**
 * Created by hahnml on 31.10.2016.
 */
public class LifeCycleException extends Exception {

    public LifeCycleException(String message) {
        super(message);
    }

    public LifeCycleException(String message, Throwable cause) {
        super(message, cause);
    }

    public LifeCycleException(Throwable cause) {
        super(cause);
    }
}

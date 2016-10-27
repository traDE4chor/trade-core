package de.unistuttgart.iaas.trade.model.lifecycle.actions;

import de.unistuttgart.iaas.trade.model.data.DataElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.statefulj.fsm.RetryException;
import org.statefulj.fsm.model.Action;

/**
 * Created by hahnml on 28.10.2016.
 */
public class DELogAction implements Action<DataElement> {

    @Override
    public void execute(DataElement stateful, String event, Object... args) throws RetryException {
        Logger logger = LoggerFactory.getLogger(stateful.getClass().getCanonicalName());

        logger.info("State of data element ({}) changed to '{}' on event '{}'.", stateful.getUrn(), stateful.getState()
                , event);
    }
}

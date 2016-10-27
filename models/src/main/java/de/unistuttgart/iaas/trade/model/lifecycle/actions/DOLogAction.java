package de.unistuttgart.iaas.trade.model.lifecycle.actions;

import de.unistuttgart.iaas.trade.model.data.DataObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.statefulj.fsm.RetryException;
import org.statefulj.fsm.model.Action;

/**
 * Created by hahnml on 28.10.2016.
 */
public class DOLogAction implements Action<DataObject> {

    @Override
    public void execute(DataObject stateful, String event, Object... args) throws RetryException {
        Logger logger = LoggerFactory.getLogger(stateful.getClass().getCanonicalName());

        logger.info("State of data object ({}) changed to '{}' on event '{}'.", stateful.getUrn(), stateful.getState()
                , event);
    }

}

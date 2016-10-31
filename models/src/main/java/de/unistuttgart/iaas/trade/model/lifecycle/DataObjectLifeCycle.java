package de.unistuttgart.iaas.trade.model.lifecycle;

import de.unistuttgart.iaas.trade.model.data.DataObject;
import de.unistuttgart.iaas.trade.model.lifecycle.actions.DOLogAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.statefulj.fsm.FSM;
import org.statefulj.fsm.TooBusyException;
import org.statefulj.fsm.model.State;

/**
 * Created by hahnml on 25.10.2016.
 */
public class DataObjectLifeCycle {

    Logger logger = LoggerFactory.getLogger("de.unistuttgart.trade.model.lifecycle.DataObjectLifeCycle");

    // defining states
    public enum States {
        INITIAL, READY, ARCHIVED, DELETED
    }

    // defining events
    public enum Events {
        initial, ready, archive, unarchive, delete
    }

    private FSM<DataObject> fsm = null;

    public DataObjectLifeCycle(DataObject dataObject) {
        buildFSM();

        init(dataObject);
    }

    private void buildFSM() {
        FSM.FSMBuilder<DataObject> fsmBuilder = FSM.FSMBuilder.newBuilder(DataObject.class);

        DOLogAction action = new DOLogAction();

        fsmBuilder.
                buildState(States.INITIAL.name(), true)
                .addTransition(Events.initial.name(), States.INITIAL.name())
                .addTransition(Events.ready.name(), States.READY.name(), action)
                .addTransition(Events.delete.name(), States.DELETED.name(), action)
                .done()
                .buildState(States.READY.name())
                .addTransition(Events.initial.name(), States.INITIAL.name(), action)
                .addTransition(Events.archive.name(), States.ARCHIVED.name(), action)
                .addTransition(Events.delete.name(), States.DELETED.name(), action)
                .done()
                .buildState(States.ARCHIVED.name())
                .addTransition(Events.unarchive.name(), States.READY.name(), action)
                .addTransition(Events.delete.name(), States.DELETED.name(), action)
                .done()
                .buildState(States.DELETED.name())
                .setEndState(true)
                .done();

        this.fsm = fsmBuilder.build();
    }

    private void init(DataObject dataObject) {
        if (dataObject.getState() == null) {
            try {
                this.fsm.onEvent(dataObject, Events.initial.name());
            } catch (TooBusyException e) {
                e.printStackTrace();
            }
        }
    }

    public States triggerEvent(DataObject obj, Events event) throws TooBusyException {
        State state = this.fsm.onEvent(obj, event.name());

        return States.valueOf(state.getName());
    }
}

package de.unistuttgart.iaas.trade.model.lifecycle;

import de.unistuttgart.iaas.trade.model.data.DataElement;
import de.unistuttgart.iaas.trade.model.lifecycle.actions.DELogAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.statefulj.fsm.FSM;
import org.statefulj.fsm.TooBusyException;
import org.statefulj.fsm.model.State;

/**
 * Created by hahnml on 25.10.2016.
 */
public class DataElementLifeCycle {

    Logger logger = LoggerFactory.getLogger("de.unistuttgart.trade.model.lifecycle.DataElementLifeCycle");

    // defining states
    public enum States {
        INITIAL, READY, ARCHIVED, DELETED
    }

    // defining events
    public enum Events {
        initial, ready, archive, unarchive, delete
    }

    private FSM<DataElement> fsm = null;

    public DataElementLifeCycle() {
        buildFSM();
    }

    private void buildFSM() {
        FSM.FSMBuilder<DataElement> fsmBuilder = FSM.FSMBuilder.newBuilder(DataElement.class);

        DELogAction action = new DELogAction();

        fsmBuilder.
                buildState(States.INITIAL.name(), true)
                .addTransition(Events.initial.name(), States.INITIAL.name())
                .addTransition(Events.ready.name(), States.READY.name(), action)
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

    public void init(DataElement dataElement) {
        if (this.fsm.getCurrentState(dataElement) == null) {
            try {
                this.fsm.onEvent(dataElement, Events.initial.name());
            } catch (TooBusyException e) {
                e.printStackTrace();
            }
        }
    }

    public States triggerEvent(DataElement obj, Events event) throws TooBusyException {
        State state = this.fsm.onEvent(obj, event.name());

        return States.valueOf(state.getName());
    }
}

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

package org.trade.core.model.lifecycle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.statefulj.fsm.FSM;
import org.statefulj.fsm.TooBusyException;
import org.statefulj.fsm.model.State;
import org.trade.core.model.data.DataValue;
import org.trade.core.model.lifecycle.actions.DataValueLogAction;

/**
 * Created by hahnml on 07.04.2017.
 */
public class DataValueLifeCycle {

    Logger logger = LoggerFactory.getLogger("org.trade.core.model.lifecycle.DataValueLifeCycle");

    // defining states
    public enum States {
        CREATED, INITIALIZED, ARCHIVED, DELETED
    }

    // defining events
    public enum Events {
        create, initialize, archive, unarchive, delete
    }

    private FSM<DataValue> fsm = null;

    public DataValueLifeCycle(DataValue dataValue) {
        this(dataValue, true);
    }

    public DataValueLifeCycle(DataValue dataValue, boolean initialize) {
        buildFSM();

        if (initialize) {
            init(dataValue);
        }
    }

    private void buildFSM() {
        FSM.FSMBuilder<DataValue> fsmBuilder = FSM.FSMBuilder.newBuilder(DataValue.class);

        DataValueLogAction action = new DataValueLogAction();

        fsmBuilder.
                buildState(States.CREATED.name(), true)
                .addTransition(Events.create.name(), States.CREATED.name())
                .addTransition(Events.initialize.name(), States.INITIALIZED.name(), action)
                .addTransition(Events.delete.name(), States.DELETED.name(), action)
                .done()
                .buildState(States.INITIALIZED.name())
                .addTransition(Events.initialize.name(), States.INITIALIZED.name(), action)
                .addTransition(Events.archive.name(), States.ARCHIVED.name(), action)
                .addTransition(Events.delete.name(), States.DELETED.name(), action)
                .done()
                .buildState(States.ARCHIVED.name())
                .addTransition(Events.unarchive.name(), States.INITIALIZED.name(), action)
                .addTransition(Events.delete.name(), States.DELETED.name(), action)
                .done()
                .buildState(States.DELETED.name())
                .setEndState(true)
                .done();

        this.fsm = fsmBuilder.build();
    }

    private void init(DataValue dataValue) {
        if (dataValue.getState() == null) {
            try {
                this.fsm.onEvent(dataValue, Events.create.name());
            } catch (TooBusyException e) {
                e.printStackTrace();
            }
        }
    }

    public States triggerEvent(DataValue dataValue, Events event) throws TooBusyException {
        State state = this.fsm.onEvent(dataValue, event.name());

        return States.valueOf(state.getName());
    }
}

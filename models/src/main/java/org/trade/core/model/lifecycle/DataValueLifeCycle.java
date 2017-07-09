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
import org.trade.core.auditing.AuditingServiceFactory;
import org.trade.core.auditing.events.InstanceStateChangeEvent;
import org.trade.core.model.data.DataValue;
import org.trade.core.model.lifecycle.actions.DataValueLogAction;
import org.trade.core.utils.events.InstanceEvents;
import org.trade.core.utils.states.InstanceStates;

/**
 * This class implements the lifecycle of a {@link DataValue} using a finite state machine in order to reflect the
 * currents state of the object through its {@link DataValue#state} attribute. The lifecycle can be managed by
 * triggering corresponding events which result in state changes of the managed object, if the underlying
 * state transition is allowed or lead to a corresponding {@link LifeCycleException}, if the transition is not
 * allowed.
 * <p>
 * Created by hahnml on 07.04.2017.
 */
public class DataValueLifeCycle {

    Logger logger = LoggerFactory.getLogger("org.trade.core.model.lifecycle.DataValueLifeCycle");

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

        fsmBuilder.
                buildState(InstanceStates.CREATED.name(), true)
                .addTransition(InstanceEvents.create.name(), InstanceStates.CREATED.name())
                .addTransition(InstanceEvents.initialize.name(), InstanceStates.INITIALIZED.name(), new DataValueLogAction(InstanceStates.CREATED.name()))
                .addTransition(InstanceEvents.delete.name(), InstanceStates.DELETED.name(), new DataValueLogAction(InstanceStates.CREATED.name()))
                .done()
                .buildState(InstanceStates.INITIALIZED.name())
                .addTransition(InstanceEvents.create.name(), InstanceStates.CREATED.name(), new DataValueLogAction(InstanceStates.INITIALIZED.name()))
                .addTransition(InstanceEvents.archive.name(), InstanceStates.ARCHIVED.name(), new DataValueLogAction(InstanceStates.INITIALIZED.name()))
                .addTransition(InstanceEvents.delete.name(), InstanceStates.DELETED.name(), new DataValueLogAction(InstanceStates.INITIALIZED.name()))
                .done()
                .buildState(InstanceStates.ARCHIVED.name())
                .addTransition(InstanceEvents.unarchive.name(), InstanceStates.INITIALIZED.name(), new DataValueLogAction(InstanceStates.ARCHIVED.name()))
                .addTransition(InstanceEvents.delete.name(), InstanceStates.DELETED.name(), new DataValueLogAction(InstanceStates.ARCHIVED.name()))
                .done()
                .buildState(InstanceStates.DELETED.name())
                .setEndState(true)
                .done();

        this.fsm = fsmBuilder.build();
    }

    private void init(DataValue dataValue) {
        if (dataValue.getState() == null) {
            try {
                this.fsm.onEvent(dataValue, InstanceEvents.create.name());

                Logger resourceLogger = LoggerFactory.getLogger(dataValue.getClass().getCanonicalName());

                // Log the creation of the resource and trigger a corresponding event
                resourceLogger.info("State of data value ({}) changed from '{}' to '{}' on event '{}'.", dataValue.getIdentifier(),
                        null, dataValue.getState(), InstanceEvents.create.name());

                AuditingServiceFactory.createAuditingService().fireEvent(new InstanceStateChangeEvent(dataValue.getIdentifier(),
                        DataValue.class, null, dataValue.getState(), InstanceEvents.create.name()));
            } catch (TooBusyException e) {
                e.printStackTrace();
            }
        }
    }

    public InstanceStates triggerEvent(DataValue dataValue, InstanceEvents event) throws TooBusyException {
        State state = this.fsm.onEvent(dataValue, event.name());

        return InstanceStates.valueOf(state.getName());
    }
}

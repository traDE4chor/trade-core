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
import org.trade.core.auditing.events.ModelStateChangeEvent;
import org.trade.core.model.data.DataModel;
import org.trade.core.model.lifecycle.actions.DataModelLogAction;
import org.trade.core.utils.events.ModelEvents;
import org.trade.core.utils.states.ModelStates;

/**
 * This class implements the lifecycle of a {@link DataModel} using a finite state machine in order to reflect the
 * currents state of the object through its {@link DataModel#state} attribute. The lifecycle can be managed by
 * triggering corresponding events which result in state changes of the managed object, if the underlying
 * state transition is allowed or lead to a corresponding {@link LifeCycleException}, if the transition is not
 * allowed.
 * <p>
 * Created by hahnml on 07.04.2017.
 */
public class DataModelLifeCycle {

    Logger logger = LoggerFactory.getLogger("org.trade.core.model.lifecycle.DataModelLifeCycle");

    private FSM<DataModel> fsm = null;

    public DataModelLifeCycle(DataModel dataModel) {
        this(dataModel, true);
    }

    public DataModelLifeCycle(DataModel dataModel, boolean initialize) {
        buildFSM();

        if (initialize) {
            init(dataModel);
        }
    }

    private void buildFSM() {
        FSM.FSMBuilder<DataModel> fsmBuilder = FSM.FSMBuilder.newBuilder(DataModel.class);

        fsmBuilder.
                buildState(ModelStates.INITIAL.name(), true)
                .addTransition(ModelEvents.initial.name(), ModelStates.INITIAL.name())
                .addTransition(ModelEvents.ready.name(), ModelStates.READY.name(), new DataModelLogAction(ModelStates.INITIAL.name()))
                .addTransition(ModelEvents.delete.name(), ModelStates.DELETED.name(), new DataModelLogAction(ModelStates.INITIAL.name()))
                .done()
                .buildState(ModelStates.READY.name())
                .addTransition(ModelEvents.initial.name(), ModelStates.INITIAL.name(), new DataModelLogAction(ModelStates.READY.name()))
                .addTransition(ModelEvents.archive.name(), ModelStates.ARCHIVED.name(), new DataModelLogAction(ModelStates.READY.name()))
                .addTransition(ModelEvents.delete.name(), ModelStates.DELETED.name(), new DataModelLogAction(ModelStates.READY.name()))
                .done()
                .buildState(ModelStates.ARCHIVED.name())
                .addTransition(ModelEvents.unarchive.name(), ModelStates.READY.name(), new DataModelLogAction(ModelStates.ARCHIVED.name()))
                .addTransition(ModelEvents.delete.name(), ModelStates.DELETED.name(), new DataModelLogAction(ModelStates.ARCHIVED.name()))
                .done()
                .buildState(ModelStates.DELETED.name())
                .setEndState(true)
                .done();

        this.fsm = fsmBuilder.build();
    }

    private void init(DataModel dataModel) {
        if (dataModel.getState() == null) {
            try {
                this.fsm.onEvent(dataModel, ModelEvents.initial.name());

                Logger resourceLogger = LoggerFactory.getLogger(dataModel.getClass().getCanonicalName());

                // Log the creation of the resource and trigger a corresponding event
                resourceLogger.info("State of data model ({}) changed from '{}' to '{}' on event '{}'.", dataModel.getIdentifier(),
                        null, dataModel.getState(), ModelEvents.initial.name());

                AuditingServiceFactory.createAuditingService().fireEvent(new ModelStateChangeEvent(dataModel.getIdentifier(),
                        DataModel.class, dataModel, null, dataModel.getState(), ModelEvents.initial.name()));
            } catch (TooBusyException e) {
                e.printStackTrace();
            }
        }
    }

    public ModelStates triggerEvent(DataModel model, ModelEvents event) throws TooBusyException {
        State state = this.fsm.onEvent(model, event.name());

        return ModelStates.valueOf(state.getName());
    }
}

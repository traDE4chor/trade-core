/*
 * Copyright 2016 Michael Hahn
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

package org.trade.core.model.lifecycle.actions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.statefulj.fsm.RetryException;
import org.trade.core.auditing.AuditingServiceFactory;
import org.trade.core.auditing.events.ModelStateChangeEvent;
import org.trade.core.model.data.DataObject;

/**
 * Created by hahnml on 28.10.2016.
 */
public class DataObjectLogAction extends AModelLogAction<DataObject> {

    public DataObjectLogAction(String oldState) {
        super(oldState);
    }

    @Override
    public void execute(DataObject stateful, String event, Object... args) throws RetryException {
        Logger logger = LoggerFactory.getLogger(stateful.getClass().getCanonicalName());

        logger.info("State of data object ({}) changed from '{}' to '{}' on event '{}'.", stateful.getIdentifier(),
                this.oldState, stateful.getState(), event);

        AuditingServiceFactory.createAuditingService().fireEvent(new ModelStateChangeEvent(stateful.getIdentifier(),
                DataObject.class, stateful, this.oldState, stateful.getState(), event));
    }

}

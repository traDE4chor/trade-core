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

package org.trade.core.model.data;

/**
 * Created by hahnml on 07.04.2017.
 */
public interface ILifeCycleModelObject {

    /**
     * Archive the model object according to its life cycle.
     *
     * @throws Exception any exception thrown during execution of the method
     */
    void archive() throws Exception;

    /**
     * Unarchive the model object according to its life cycle.
     *
     * @throws Exception any exception thrown during execution of the method
     */
    void unarchive() throws Exception;

    /**
     * Delete the model object according to its life cycle.
     *
     * @throws Exception any exception thrown during execution of the method
     */
    void delete() throws Exception;

    /**
     * @return If the model object is in state 'INITIAL'
     */
    boolean isInitial();

    /**
     * @return If the model object is in state 'READY'
     */
    boolean isReady();

    /**
     * @return If the model object is in state 'ARCHIVED'
     */
    boolean isArchived();

    /**
     * @return If the model object is in state 'DELETED'
     */
    boolean isDeleted();
}

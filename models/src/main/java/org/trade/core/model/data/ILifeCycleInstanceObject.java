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
public interface ILifeCycleInstanceObject {
    /**
     * Create the instance object according to its life cycle.
     *
     * @throws Exception any exception thrown during execution of the method
     */
    public void create() throws Exception;

    /**
     * Initializes the instance object according to its life cycle.
     *
     * @throws Exception any exception thrown during execution of the method
     */
    public void initialize() throws Exception;

    /**
     * Archive the instance object according to its life cycle.
     *
     * @throws Exception any exception thrown during execution of the method
     */
    public void archive() throws Exception;

    /**
     * Unarchive the instance object according to its life cycle.
     *
     * @throws Exception any exception thrown during execution of the method
     */
    public void unarchive() throws Exception;

    /**
     * Delete the instance object according to its life cycle.
     *
     * @throws Exception any exception thrown during execution of the method
     */
    public void delete() throws Exception;

    /**
     * @return If the instance object is in state 'CREATED'
     */
    public boolean isCreated();

    /**
     * @return If the instance object is in state 'INITIALIZED'
     */
    public boolean isInitialized();

    /**
     * @return If the instance object is in state 'ARCHIVED'
     */
    public boolean isArchived();

    /**
     * @return If the instance object is in state 'DELETED'
     */
    public boolean isDeleted();
}

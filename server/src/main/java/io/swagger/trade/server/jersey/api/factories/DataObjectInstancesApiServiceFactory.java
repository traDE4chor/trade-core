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

package io.swagger.trade.server.jersey.api.factories;

import io.swagger.trade.server.jersey.api.DataObjectInstancesApiService;
import io.swagger.trade.server.jersey.api.impl.DataObjectInstancesApiServiceImpl;

public class DataObjectInstancesApiServiceFactory {
    private final static DataObjectInstancesApiService service = new DataObjectInstancesApiServiceImpl();

    public static DataObjectInstancesApiService getDataObjectInstancesApi() {
        return service;
    }
}

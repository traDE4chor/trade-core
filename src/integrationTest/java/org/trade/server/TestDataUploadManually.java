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

package org.trade.server;

import io.swagger.trade.client.jersey.ApiException;
import io.swagger.trade.client.jersey.api.DataValueApi;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by hahnml on 02.02.2017.
 */
public class TestDataUploadManually {

    public static void main(String[] args) {
        DataValueApi dvApiInstance = new DataValueApi();
        dvApiInstance.getApiClient().setBasePath("http://localhost:8080/api");

        try {
            String idOfDataValue1 = "";
            String idOfDataValue2 = "";

            byte[] data1 = getData("data.dat");

            // Push data to first data value
            dvApiInstance.pushDataValue(idOfDataValue1, new Long(data1.length), data1);

            byte[] data2 = getData("video.mp4");

            // Push data to second data value
            dvApiInstance.pushDataValue(idOfDataValue2, new Long(data1.length), data2);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ApiException e) {
            System.err.println("Exception when calling DataValueApi#addDataValue");
            e.printStackTrace();
        }
    }

    private static byte[] getData(String fileName) throws IOException {
        InputStream in = TestDataUploadManually.class.getResourceAsStream("/" + fileName);
        byte[] data = null;

        try {
            data = IOUtils.toByteArray(in);
        } finally {
            in.close();
        }

        return data;
    }
}

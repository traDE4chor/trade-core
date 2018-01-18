/*
 * Copyright 2018 Michael Hahn
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

package org.trade.core.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * Created by hahnml on 18.01.2018.
 */
public class DataReferenceUtils {

    private static final String HTTP_LINK = "http://";

    private Logger logger = LoggerFactory.getLogger("org.trade.core.utils.DataReferenceUtils");

    /**
     * This methods tries to resolve the provided link to an actual data value.
     *
     * @param link the link to the data to be resolve
     * @return the data as byte[]
     */
    public static byte[] resolveLink(String link) throws Exception {
        byte[] result;

        if (link.startsWith(HTTP_LINK)) {
            Client client = ClientBuilder.newClient();

            // We accept any response and resolve the data from the returned response. The clients are responsible for
            // providing links that reflect the actual data they want to pass to the middleware since we cannot
            // identify if the data provided through the link is appropriate or not.
            Response res = client.target(link).request().get();

            // Read the response entity
            InputStream is = res.readEntity(InputStream.class);

            ByteArrayOutputStream os = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];

            // Copy the bytes from the input stream to the output stream
            for (int len; (len = is.read(buffer)) != -1; )
                os.write(buffer, 0, len);

            os.flush();

            // Set the resulting byte[]
            result = os.toByteArray();

            res.close();
            client.close();
        } else {
            throw new UnknownLinkFormatException("The format of the link '" + link + "' is not supported. Please " +
                    "specify a link according to the following schema: http://[hostname]([:optional " +
                    "port])(/[resource])+");
        }

        return result;
    }
}

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

import io.swagger.trade.client.jersey.model.Link;
import io.swagger.trade.client.jersey.model.LinkArray;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by hahnml on 11.04.2017.
 */
public class TestUtils {

    /**
     * Get data from a file as byte[].
     *
     * @param fileName the file name from which to load data
     * @return the byte[]
     * @throws IOException the io exception
     */
    public static byte[] getData(String fileName) throws IOException {
        InputStream in = TestUtils.class.getResourceAsStream("/" + fileName);
        byte[] data = null;

        try {
            data = IOUtils.toByteArray(in);
        } finally {
            in.close();
        }

        return data;
    }

    public static void printLinkArray(LinkArray links) {
        for (Link link : links) {
            System.out.println("HREF: " + link.getHref());
            System.out.println("REL: " + link.getRel());
            System.out.println("TITLE: " + link.getTitle());
            System.out.println("TYPE: " + link.getType());
            System.out.println("HREF-LANG: " + link.getHreflang());
            System.out.println("LENGTH: " + link.getLength());
        }
    }
}

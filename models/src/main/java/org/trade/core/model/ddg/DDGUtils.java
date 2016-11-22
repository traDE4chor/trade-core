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

package org.trade.core.model.ddg;

import org.trade.core.model.ModelUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.bind.*;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.*;

/**
 * Created by hahnml on 03.11.2016.
 */
public class DDGUtils {

    private static Logger logger = LoggerFactory.getLogger("de.unistuttgart.iaas.trade.model.ddg.DDGUtils");

    public static DataDependenceGraph unmarshalGraph(String ddgGraphFilePath)
            throws JAXBException, SAXException, FileNotFoundException {
        FileInputStream stream = new FileInputStream(new File(ddgGraphFilePath));

        return unmarshalGraph(stream);
    }

    public static DataDependenceGraph unmarshalGraph(byte[] ddgGraph)
            throws JAXBException, SAXException, FileNotFoundException {
        ByteArrayInputStream stream = new ByteArrayInputStream(ddgGraph);

        return unmarshalGraph(stream);
    }

    public static DataDependenceGraph unmarshalGraph(InputStream ddgGraphContent)
            throws JAXBException, SAXException {
        DataDependenceGraph graph = null;

        if (ddgGraphContent != null) {
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

            InputStream xsdSchemaContent = DDGUtils.class.getResourceAsStream(ModelUtils.DDG_SCHEMA_LOCATION);

            Schema schema = (xsdSchemaContent == null)
                    ? null : schemaFactory.newSchema(new StreamSource(xsdSchemaContent));
            JAXBContext jaxbContext = JAXBContext.newInstance(DataDependenceGraph.class.getPackage().getName());

            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            unmarshaller.setSchema(schema);
            graph = (DataDependenceGraph) unmarshaller.unmarshal(ddgGraphContent);
        } else {
            logger.warn("Trying to load a data dependence graph from an empty or not existing source.");
        }

        return graph;
    }

    public static void marshalGraph(String ddgGraphFilePath, DataDependenceGraph jaxbElement)
            throws JAXBException, SAXException, FileNotFoundException {
        FileOutputStream stream = new FileOutputStream(new File(ddgGraphFilePath));

        marshalGraph(stream, jaxbElement);
    }

    public static byte[] marshalGraph(DataDependenceGraph jaxbElement)
            throws JAXBException, SAXException, FileNotFoundException {
        byte[] result = null;

        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        marshalGraph(stream, jaxbElement);
        result = stream.toByteArray();

        return result;
    }

    public static void marshalGraph(OutputStream outStream, DataDependenceGraph jaxbElement)
            throws JAXBException, SAXException {
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

        InputStream xsdSchemaContent = DDGUtils.class.getResourceAsStream(ModelUtils.DDG_SCHEMA_LOCATION);

        Schema schema = (xsdSchemaContent == null)
                ? null : schemaFactory.newSchema(new StreamSource(xsdSchemaContent));
        JAXBContext jaxbContext = JAXBContext.newInstance(jaxbElement.getClass().getPackage().getName());

        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setSchema(schema);
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        marshaller.marshal(jaxbElement, outStream);
    }
}

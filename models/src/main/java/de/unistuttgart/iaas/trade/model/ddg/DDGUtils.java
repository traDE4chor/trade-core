package de.unistuttgart.iaas.trade.model.ddg;

import de.unistuttgart.iaas.trade.model.ModelUtils;
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

    public static DataDependenceGraphType unmarshalGraph(String ddgGraphFilePath)
            throws JAXBException, SAXException, FileNotFoundException {
        FileInputStream fileStream = new FileInputStream(new File(ddgGraphFilePath));

        return unmarshalGraph(fileStream);
    }

    public static DataDependenceGraphType unmarshalGraph(InputStream ddgGraphContent)
            throws JAXBException, SAXException {
        DataDependenceGraphType graph = null;

        if (ddgGraphContent != null) {
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

            InputStream xsdSchemaContent = DDGUtils.class.getResourceAsStream(ModelUtils.DDG_SCHEMA_LOCATION);

            Schema schema = (xsdSchemaContent == null)
                    ? null : schemaFactory.newSchema(new StreamSource(xsdSchemaContent));
            JAXBContext jaxbContext = JAXBContext.newInstance(DataDependenceGraphType.class.getPackage().getName());

            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            unmarshaller.setSchema(schema);
            graph = (DataDependenceGraphType) JAXBIntrospector.getValue(unmarshaller.unmarshal(ddgGraphContent));
        } else {
            logger.warn("Trying to load a data dependence graph from an empty or not existing source.");
        }

        return graph;
    }

    public static void marshalGraph(OutputStream outStream, DataDependenceGraphType jaxbElement)
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

    public static void marshalGraph(String ddgGraphFilePath, DataDependenceGraphType jaxbElement)
            throws JAXBException, SAXException, FileNotFoundException {
        FileOutputStream stream = new FileOutputStream(new File(ddgGraphFilePath));

        marshalGraph(stream, jaxbElement);
    }
}

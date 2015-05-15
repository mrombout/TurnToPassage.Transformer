package nl.mikero.turntopassage.core.transformer;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

/**
 * Transform a regular Twine XML file to an extended Twine XML format.
 */
public class ExtendTwineXmlTransformer {

    public void transform(InputStream input, OutputStream output) throws ParserConfigurationException, TransformerException, IOException, SAXException {
        Objects.requireNonNull(input);
        Objects.requireNonNull(output);

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
        Document document = builder.parse(input);

        TransformerFactory transformerFactory = TransformerFactory.newInstance();

        StreamSource stylesheetSource = new StreamSource(getClass().getResourceAsStream("/extend.xsl"));
        Transformer transformer = transformerFactory.newTransformer(stylesheetSource);

        DOMSource source = new DOMSource(document);
        StreamResult result = new StreamResult(output);
        transformer.transform(source, result);
    }

}

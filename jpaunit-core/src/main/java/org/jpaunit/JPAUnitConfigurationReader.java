package org.jpaunit;

import org.jpaunit.exception.JPAUnitFileReadException;
import org.jpaunit.exception.JPAUnitFileSyntaxException;
import org.jpaunit.exception.JPAUnitNodeProcessingException;
import org.jpaunit.node.INodeProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz Krzyzak
 * Date: 12.12.10
 * Time: 15:57
 */
public class JPAUnitConfigurationReader {


    private static final Logger log = LoggerFactory.getLogger(JPAUnitConfigurationReader.class);

    public JPAUnitConfiguration read(InputStream stream) throws ParserConfigurationException, IOException, SAXException, JPAUnitFileReadException {
        return this.read(stream, new JPAUnitConfiguration());
    }


    public JPAUnitConfiguration read(InputStream stream, JPAUnitConfiguration result) throws IOException, JPAUnitFileReadException {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document document = db.parse(stream);
            document.getDocumentElement().normalize();

            Node firstChild = document.getFirstChild();
            if ("jpaunit".equals(firstChild.getNodeName())) {
                NodeList childNodes = firstChild.getChildNodes();
                for (int i = 0; i < childNodes.getLength(); i++) {
                    Node jpaUnitElement = childNodes.item(i);
                    // take nodeprocessor responsible for processing this type of node
                    INodeProcessor nodeProcessor = result.getNodeProcessor(jpaUnitElement.getNodeName());
                    if (nodeProcessor!=null){
                        // process node

                        try {
                            nodeProcessor.process(jpaUnitElement, result);
                        } catch (JPAUnitNodeProcessingException e) {
                            throw new JPAUnitFileSyntaxException("error at node: "+i, e);
                        }
                    }  else {
                        // if no such processor exists output warning
                        // TODO: consider throwing an exception
                        String s = jpaUnitElement.getNodeName() + " element (" + i + ") does not have associated " + INodeProcessor.class.getCanonicalName();
                        if (log.isWarnEnabled()) {
                            log.warn(s);
                        }
                        throw new JPAUnitFileSyntaxException(s);
                    }
                }
            }
        } catch (ParserConfigurationException e) {
            throw new JPAUnitFileReadException(e);
        } catch (SAXException e) {
            throw new JPAUnitFileReadException(e);
        }
        return result;
    }


}

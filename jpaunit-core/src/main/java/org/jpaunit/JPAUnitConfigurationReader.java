package org.jpaunit;

import org.jpaunit.exception.JPAUnitFileReadException;
import org.jpaunit.exception.JPAUnitFileSyntaxException;
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
                    Node statementNode = childNodes.item(i);
                    if ("statement".equals(statementNode.getNodeName())) {
                        NodeList statementChildren = statementNode.getChildNodes();
                        if (statementChildren.getLength() > 1) {
                            throw new JPAUnitFileSyntaxException("statement element (" + i + ") is allowed only to have 1 child: CDATA");
                        }
                        String statement = null;

                        Node statementCDATA = statementChildren.item(0);
                        if (statementCDATA != null && statementCDATA.getNodeType() == Node.CDATA_SECTION_NODE) {
                            statement = statementCDATA.getNodeValue();
                        }
                        if (statement == null) {
                            NamedNodeMap attributes = statementNode.getAttributes();
                            Node codeNode = attributes.getNamedItem("code");
                            if (codeNode != null)
                                statement = codeNode.getNodeValue();

                        }
                        if (statement != null)
                            result.addStatement(statement);
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

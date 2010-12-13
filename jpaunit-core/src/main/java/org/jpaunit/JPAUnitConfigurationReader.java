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
                    Node jpaUnitElement = childNodes.item(i);
                    if ("statement".equals(jpaUnitElement.getNodeName())) {
                        NodeList statementChildren = jpaUnitElement.getChildNodes();
                        if (statementChildren.getLength() > 1) {
                            throw new JPAUnitFileSyntaxException("statement element (" + i + ") is allowed only to have 1 child: CDATA");
                        }
                        String statement = null;

                        Node statementCDATA = statementChildren.item(0);
                        if (statementCDATA != null && statementCDATA.getNodeType() == Node.CDATA_SECTION_NODE) {
                            statement = statementCDATA.getNodeValue();
                        }
                        if (statement == null) {
                            NamedNodeMap attributes = jpaUnitElement.getAttributes();
                            Node codeNode = attributes.getNamedItem("code");
                            if (codeNode != null)
                                statement = codeNode.getNodeValue();

                        }
                        if (statement != null)
                            result.addStatement(statement);
                    } else if ("import".equals(jpaUnitElement.getNodeName())){
                        NamedNodeMap importAttributes = jpaUnitElement.getAttributes();
                        Node classNode = importAttributes.getNamedItem("class");
                        Node aliasNode = importAttributes.getNamedItem("alias");

                        if (classNode==null) {
                            throw new JPAUnitFileSyntaxException("import element (" + i + ") is must have \"class\" attribute. It must be fully qualified class name");
                        }
                        String className = classNode.getNodeValue();
                        int dotIndex = className.lastIndexOf(".");
                        String alias = className.substring(dotIndex>-1?dotIndex+1:0);
                        if (aliasNode != null){
                            alias = aliasNode.getNodeValue();
                        }
                        result.addImport(className, alias);
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

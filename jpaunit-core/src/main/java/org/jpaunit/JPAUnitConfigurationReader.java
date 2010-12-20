package org.jpaunit;

import org.jpaunit.exception.JPAUnitConfigurationException;
import org.jpaunit.exception.JPAUnitFileReadException;
import org.jpaunit.exception.JPAUnitFileSyntaxException;
import org.jpaunit.exception.JPAUnitNodeProcessingException;
import org.jpaunit.node.INodeProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz Krzyzak
 * Date: 12.12.10
 * Time: 15:57
 */
public class JPAUnitConfigurationReader {

    private static final Logger log = LoggerFactory.getLogger(JPAUnitConfigurationReader.class);

    public static final String Properties_NodeProcessor_Prefix = "jpaunit.nodeprocessor.";
    public static final String JPAUnitPropertiesFileName = "jpaunit.properties";

    private Map<String, INodeProcessor> nodeProcessors = new HashMap<String, INodeProcessor>();

    public JPAUnitConfigurationReader() {
        this(readProperties("/" + JPAUnitPropertiesFileName));
    }

    public JPAUnitConfigurationReader(Properties properties) {
        Enumeration<?> enumeration = properties.propertyNames();
        while (enumeration.hasMoreElements()) {
            String name = (String) enumeration.nextElement();
            if (name.startsWith(Properties_NodeProcessor_Prefix)) {
                String nodeType = name.substring(Properties_NodeProcessor_Prefix.length());
                try {
                    Class<?> clazz = Class.forName(properties.getProperty(name));
                    registerNodeProcessor(nodeType, (INodeProcessor) Class.forName(properties.getProperty(name)).newInstance());
                } catch (Exception e) {
                    throw new JPAUnitConfigurationException(e);
                }
            }
        }

    }


    public static Properties readProperties(String fileName) {
        InputStream resourceAsStream = JPAUnitConfiguration.class.getResourceAsStream(fileName);
        Properties properties = new Properties();
        try {
            properties.load(resourceAsStream);
        } catch (IOException e) {
            throw new JPAUnitConfigurationException(e);
        } finally {
            if (resourceAsStream != null)
                try {
                    resourceAsStream.close();
                } catch (IOException e) {
                    log.error("noe default properties file", e);
                }
        }
        return properties;
    }

    public void registerNodeProcessor(String nodeName, INodeProcessor nodeProcessor) {
        nodeProcessors.put(nodeName, nodeProcessor);
    }

    public INodeProcessor getNodeProcessor(String nodeName) {
        return nodeProcessors.get(nodeName);
    }

    public JPAUnitConfiguration read(InputStream stream) throws JPAUnitFileReadException {
        return this.read(stream, new JPAUnitConfiguration());
    }


    public JPAUnitConfiguration read(InputStream stream, JPAUnitConfiguration result) throws JPAUnitFileReadException {
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
                    if (jpaUnitElement.getNodeType() != Node.ELEMENT_NODE)
                        continue;

                    // take nodeprocessor responsible for processing this type of node
                    INodeProcessor nodeProcessor = getNodeProcessor(jpaUnitElement.getNodeName());
                    if (nodeProcessor != null) {
                        // process node

                        try {
                            nodeProcessor.process(jpaUnitElement, result, this);
                        } catch (JPAUnitNodeProcessingException e) {
                            throw new JPAUnitFileSyntaxException("error at node: " + i, e);
                        }
                    } else {
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
        } catch (IOException e) {
            throw new JPAUnitFileReadException(e);
        }
        return result;
    }


}

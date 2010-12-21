package org.ormunit;

import org.ormunit.exception.ORMUnitConfigurationException;
import org.ormunit.exception.ORMUnitFileReadException;
import org.ormunit.exception.ORMUnitFileSyntaxException;
import org.ormunit.exception.ORMUnitNodeProcessingException;
import org.ormunit.node.INodeProcessor;
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
public class ORMUnitConfigurationReader {

    private static final Logger log = LoggerFactory.getLogger(ORMUnitConfigurationReader.class);

    public static final String Properties_NodeProcessor_Prefix = "ormunit.nodeprocessor.";
    public static final String JPAUnitPropertiesFileName = "ormunit.properties";

    private Map<String, INodeProcessor> nodeProcessors = new HashMap<String, INodeProcessor>();

    public ORMUnitConfigurationReader() {
        this(readProperties("/" + JPAUnitPropertiesFileName));
    }

    public ORMUnitConfigurationReader(Properties properties) {
        Enumeration<?> enumeration = properties.propertyNames();
        while (enumeration.hasMoreElements()) {
            String name = (String) enumeration.nextElement();
            if (name.startsWith(Properties_NodeProcessor_Prefix)) {
                String nodeType = name.substring(Properties_NodeProcessor_Prefix.length());
                try {
                    Class<?> clazz = Class.forName(properties.getProperty(name));
                    registerNodeProcessor(nodeType, (INodeProcessor) Class.forName(properties.getProperty(name)).newInstance());
                } catch (Exception e) {
                    throw new ORMUnitConfigurationException(e);
                }
            }
        }

    }


    public static Properties readProperties(String fileName) {
        InputStream resourceAsStream = ORMUnitConfiguration.class.getResourceAsStream(fileName);
        Properties properties = new Properties();
        try {
            properties.load(resourceAsStream);
        } catch (IOException e) {
            throw new ORMUnitConfigurationException(e);
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

    public ORMUnitConfiguration read(InputStream stream) throws ORMUnitFileReadException {
        return this.read(stream, new ORMUnitConfiguration());
    }


    public ORMUnitConfiguration read(InputStream stream, ORMUnitConfiguration result) throws ORMUnitFileReadException {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document document = db.parse(stream);
            document.getDocumentElement().normalize();

            Node firstChild = document.getFirstChild();
            if ("ormunit".equals(firstChild.getNodeName())) {
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
                        } catch (ORMUnitNodeProcessingException e) {
                            throw new ORMUnitFileSyntaxException("error at node: " + i, e);
                        }
                    } else {
                        // if no such processor exists output warning
                        // TODO: consider throwing an exception
                        String s = jpaUnitElement.getNodeName() + " element (" + i + ") does not have associated " + INodeProcessor.class.getCanonicalName();
                        if (log.isWarnEnabled()) {
                            log.warn(s);
                        }
                        throw new ORMUnitFileSyntaxException(s);
                    }
                }
            }
        } catch (ParserConfigurationException e) {
            throw new ORMUnitFileReadException(e);
        } catch (SAXException e) {
            throw new ORMUnitFileReadException(e);
        } catch (IOException e) {
            throw new ORMUnitFileReadException(e);
        }
        return result;
    }


}

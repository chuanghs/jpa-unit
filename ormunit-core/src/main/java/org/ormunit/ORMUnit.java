package org.ormunit;

import org.ormunit.exception.ORMUnitConfigurationException;
import org.ormunit.exception.ORMUnitFileReadException;
import org.ormunit.exception.ORMUnitFileSyntaxException;
import org.ormunit.exception.ORMUnitNodeProcessingException;
import org.ormunit.node.ANodeProcessor;
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
import java.lang.reflect.Constructor;
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
public class ORMUnit {

    public static final String Properties_NodeProcessor_Prefix = "ormunit.nodeprocessor.";
    public static final String PropertiesFileName = "ormunit.properties";
    public static final String DefaultPropertiesFileName = "ormunit.default.properties";

    public static final String Properties_Datasources = "ormunit.datasources";
    public static final String Properties_DatasourcesDefault = "ormunit.datasources.default";


    private static final Logger log = LoggerFactory.getLogger(ORMUnit.class);
    private String currentDir;
    private Class<?> workClass;

    private String defaultDataSourceName;
    private Map<String, Properties> dsProperties = new HashMap<String, Properties>();
    private Properties properties;

    public ORMUnit(Class<?> workClass) {
        this(workClass, ORMUnitHelper.readOrmUnitProperties(workClass));

    }

    public ORMUnit(Class<?> aClass, Properties properties) {
        this.workClass = aClass;
        this.properties = properties;
        currentDir = "/" + workClass.getPackage().getName().replace(".", "/");

        this.defaultDataSourceName = properties.getProperty(Properties_DatasourcesDefault);

        String datasources = properties.getProperty(Properties_Datasources);
        if (datasources != null) {
            String[] split = datasources.split(",");
            for (String s : split) {
                this.dsProperties.put(s.trim(), extractDataSourceProperties(s.trim(), properties));
            }
        }

    }

    private Properties extractDataSourceProperties(String trim, Properties properties) {
        Properties result = new Properties();

        Enumeration<?> names = properties.propertyNames();
        while (names.hasMoreElements()) {
            String name = (String) names.nextElement();
            if (name.startsWith(trim + ".")) {
                String extractedPropertyName = name.substring(trim.length() + 1);
                String propertyValue = properties.getProperty(name);
                result.setProperty(extractedPropertyName.trim(), propertyValue.trim());
            }
        }

        return result;
    }

    public String getDefaultDataSourceName() {
        return this.defaultDataSourceName;
    }

    public Properties getDefaultDataSourceProperties() {
        if (this.defaultDataSourceName == null) {
            throw new ORMUnitConfigurationException("no default datasource");
        }
        return this.getDefaultDataSourceProperties(new Properties());
    }

    public Properties getDefaultDataSourceProperties(Properties defaults) {
        Properties properties1 = this.dsProperties.get(this.defaultDataSourceName);
        Properties result = new Properties(defaults);
        if (properties1 != null) {
            for (String propertyName : properties1.stringPropertyNames()) {
                result.setProperty(propertyName, properties1.getProperty(propertyName));
            }
        }
        return result;

    }

    public ORMUnitTestSet read(InputStream stream, ORMProvider provider) throws ORMUnitFileReadException {
        return this.read(stream, new ORMUnitTestSet(provider));
    }


    public ORMUnitTestSet read(InputStream stream, ORMUnitTestSet result) throws ORMUnitFileReadException {
        registerNodeProcessors(result);
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
                    ANodeProcessor nodeProcessor = result.getNodeProcessor(jpaUnitElement.getNodeName());
                    if (nodeProcessor != null) {
                        // process node

                        try {
                            nodeProcessor.process(jpaUnitElement, result);
                        } catch (ORMUnitNodeProcessingException e) {
                            throw new ORMUnitFileSyntaxException("error at node: " + i, e);
                        }
                    } else {
                        String s = jpaUnitElement.getNodeName() + " element (" + i + ": " + jpaUnitElement.getNodeName() + ") does not have associated I" + ANodeProcessor.class.getCanonicalName() + " implementations";
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

    private void registerNodeProcessors(ORMUnitTestSet testset) {
        Enumeration<?> enumeration = properties.propertyNames();
        while (enumeration.hasMoreElements()) {
            String name = (String) enumeration.nextElement();
            if (name.startsWith(Properties_NodeProcessor_Prefix)) {
                String nodeType = name.substring(Properties_NodeProcessor_Prefix.length());
                try {
                    Constructor<?> constructor = Class.forName(properties.getProperty(name)).getConstructor(ORMUnit.class);
                    testset.registerNodeProcessor(nodeType, (ANodeProcessor) constructor.newInstance(this));
                } catch (Exception e) {
                    throw new ORMUnitConfigurationException(e);
                }
            }
        }
    }


    public ORMUnitTestSet read(String filePath, ORMUnitTestSet result) throws ORMUnitFileReadException {
        filePath = filePath.replace("\\", "/").trim();


        String workDir = this.currentDir;
        try {
            String fileName = null;
            int lastSlash = filePath.lastIndexOf("/");

            if (lastSlash > 0) {
                fileName = filePath.substring(lastSlash + 1);
                this.currentDir = this.currentDir + "/" + filePath.substring(0, lastSlash);
            } else if (lastSlash == 1) {
                fileName = filePath.substring(lastSlash + 1);
                this.currentDir = filePath.substring(0, lastSlash);
            } else {
                fileName = filePath;
            }

            InputStream stream = null;

            try {
                stream = getResourceAsStream(currentDir + "/" + fileName);

                read(stream, result);
            } catch (Exception e) {
                throw new ORMUnitFileReadException("file does not exist: " + fileName + "(workdir: " + this.currentDir + ")", e);
            } finally {
                if (stream != null)
                    try {
                        stream.close();
                    } catch (IOException e) {
                        log.warn("", e);
                    }
            }
        } finally {
            this.currentDir = workDir;
        }
        return result;
    }

    public InputStream getResourceAsStream(String s) {
        return this.workClass.getResourceAsStream(s);
    }

    public String getCurrentDir() {
        return currentDir;
    }


    public Class<?> getWorkClass() {
        return workClass;
    }


}

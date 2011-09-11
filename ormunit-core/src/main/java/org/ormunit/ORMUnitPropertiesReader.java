package org.ormunit;

import org.ormunit.exception.ConfigurationException;
import org.ormunit.exception.FileReadException;
import org.ormunit.exception.FileSyntaxException;
import org.ormunit.exception.NodeProcessingException;
import org.ormunit.node.NodeProcessor;
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
import java.lang.reflect.InvocationTargetException;
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
public class ORMUnitPropertiesReader {

    public static final String Properties_NodeProcessor_Prefix = "ormunit.nodeprocessor.";
    public static final String PropertiesFileName = "ormunit.properties";
    public static final String DefaultPropertiesFileName = "ormunit.default.properties";

    public static final String Properties_Datasources = "ormunit.datasources";
    public static final String Properties_DatasourcesDefault = "ormunit.datasources.default";


    private static final Logger log = LoggerFactory.getLogger(ORMUnitPropertiesReader.class);
    private String currentDir;
    private Class<?> workClass;

    private String defaultDataSourceName;

    private Map<String, Properties> dsProperties = new HashMap<String, Properties>();
    private Properties properties;

    public ORMUnitPropertiesReader(Class<?> workClass) {
        this(workClass, ORMUnitHelper.readOrmUnitProperties(workClass));
    }

    public ORMUnitPropertiesReader(String currentDir, Class<?> workClass) {
        this(currentDir, workClass, ORMUnitHelper.readOrmUnitProperties(workClass));
    }


    public ORMUnitPropertiesReader(Class<?> aClass, Properties properties) {
        this("/" + aClass.getPackage().getName().replace(".", "/"), aClass, properties);
    }

    public ORMUnitPropertiesReader(String currentDir, Class<?> aClass, Properties properties) {
        this.workClass = aClass;
        this.properties = properties;
        this.currentDir = currentDir;

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
            throw new ConfigurationException("no default datasource", null);
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

    public TestSet read(String filePath, TestSet result) throws FileReadException {
        String workDir = currentDir;
        try {
            InputStream stream = null;

            String[] parentChild = normalizePath(filePath);
            this.currentDir = parentChild[0];
            try {
                read(stream = getResourceAsStream(parentChild[0] + parentChild[1]), result);
            } catch (Exception e) {
                throw new FileReadException(String.format("file does not exist: %s (workdir: %s)", parentChild[1], parentChild[0]), e);
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


    public TestSet read(InputStream stream, TestSet result) throws FileReadException {
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
                    NodeProcessor nodeProcessor = result.getNodeProcessor(jpaUnitElement.getNodeName());
                    if (nodeProcessor != null) {
                        // process node

                        try {
                            nodeProcessor.process(jpaUnitElement, result);
                        } catch (NodeProcessingException e) {
                            throw new FileReadException("error at node: " + i, e);
                        }
                    } else {
                        String s = jpaUnitElement.getNodeName() + " element (" + i + ": " + jpaUnitElement.getNodeName() + ") does not have associated I" + NodeProcessor.class.getCanonicalName() + " implementations";
                        if (log.isWarnEnabled()) {
                            log.warn(s);
                        }
                        throw new FileSyntaxException(s);
                    }
                }
            }
        } catch (ParserConfigurationException e) {
            throw new FileReadException(e);
        } catch (SAXException e) {
            throw new FileReadException(e);
        } catch (IOException e) {
            throw new FileReadException(e);
        }
        return result;
    }


    private void registerNodeProcessors(TestSet testset) {
        createAndRegisterNodeProcessors(testset, findNodeProcessorProperties());
    }

    private void createAndRegisterNodeProcessors(TestSet testset, Map<String, String> nodeProcessorClassNames) {
        for (Map.Entry<String, String> entry : nodeProcessorClassNames.entrySet()) {
            createAndRegisterNodeProcessor(testset, entry.getKey(), entry.getValue());
        }
    }

    private void createAndRegisterNodeProcessor(TestSet testset, String className, String nodeTag) {
        try {
            Constructor<?> constructor = Class.forName(className).getConstructor(ORMUnitPropertiesReader.class);
            testset.registerNodeProcessor(nodeTag, (NodeProcessor) constructor.newInstance(this));
        } catch (Exception e) {
            if (e instanceof ClassNotFoundException || e instanceof NoSuchMethodException)
                throw new ConfigurationException(String.format("There is no class %1$s or there is no there is no required constructor %1$s.<init>(%2$s)", className, ORMUnitPropertiesReader.class.getCanonicalName()), e);
            else if (e instanceof InvocationTargetException || e instanceof InstantiationException || e instanceof IllegalAccessException)
                throw new ConfigurationException(String.format("Couldnt instantiate node processor %1$s", className), e);
        }
    }

    private Map<String, String> findNodeProcessorProperties() {
        Map<String, String> notProcessorClassNames = new HashMap<String, String>();

        Enumeration<?> enumeration = properties.propertyNames();
        while (enumeration.hasMoreElements()) {
            String name = (String) enumeration.nextElement();
            if (name.startsWith(Properties_NodeProcessor_Prefix)) {
                notProcessorClassNames.put(properties.getProperty(name), name.substring(Properties_NodeProcessor_Prefix.length()));
            }
        }
        return notProcessorClassNames;
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


    public String[] normalizePath(String child) {
        return normalizePath(this.currentDir, child);
    }

    public static String[] normalizePath(String parent, String child) {

        child = child.replace("\\", "/").trim();
        parent = parent.replace("\\", "/").trim();


        String filePath = null;

        if (child.startsWith("/")) {
            filePath = child;
        } else {
            filePath = parent + "/" + child;
        }
        filePath = filePath.replace("//", "/");

        int cdIndex = -1;
        while ((cdIndex = filePath.indexOf("/../")) != -1) {
            String begining = filePath.substring(0, cdIndex);
            String end = filePath.substring(cdIndex + 4);

            filePath = begining.substring(0, begining.lastIndexOf("/") + 1) + end;
        }

        filePath = filePath.replace("/./", "/");

        int i = filePath.lastIndexOf("/");

        return new String[]{filePath.substring(0, i + 1), filePath.substring(i + 1)};
    }

}

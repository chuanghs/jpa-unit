package org.ormunit.node;

import org.ormunit.ORMUnitConfiguration;
import org.ormunit.ORMUnitConfigurationReader;
import org.ormunit.exception.ORMUnitConfigurationException;
import org.ormunit.exception.ORMUnitNodeProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Created by IntelliJ IDEA.
 * User: krzyzak
 * Date: 23.12.10
 * Time: 16:37
 */
public class ImportNodeProcessor implements INodeProcessor {

    private static final Logger log = LoggerFactory.getLogger(ImportNodeProcessor.class);
    public static final String ClassNamePattern = "[$a-zA-Z_]+[$a-zA-Z_0-9]*(\\.[$a-zA-Z_]+[$a-zA-Z_0-9]*)*";

    private final WeakHashMap<ORMUnitConfiguration, WeakReference<Map<String, String>>> confImports = new WeakHashMap<ORMUnitConfiguration, WeakReference<Map<String, String>>>();

    public void addImport(ORMUnitConfiguration configuration, String className, String alias) {

        if (confImports.get(configuration) == null) {
            confImports.put(configuration, new WeakReference<Map<String, String>>(new HashMap<String, String>()));
        }
        Map<String, String> imports = confImports.get(configuration).get();


        if (imports.containsKey(alias)) {
            if (!className.equals(imports.get(alias)))
                throw new ORMUnitConfigurationException("alias: " + alias + " is defined more than once (" + imports.get(className) + ", " + className + ")");

            log.warn("alias: " + alias + " is defined twice for the same class: " + className);

        }

        if (!className.matches(ClassNamePattern))
            throw new ORMUnitConfigurationException("className: " + className + " is invalid class name");

        imports.put(alias, className);

    }

    public void process(Node jpaUnitElement, ORMUnitConfiguration result, ORMUnitConfigurationReader reader) throws ORMUnitNodeProcessingException {
        NamedNodeMap importAttributes = jpaUnitElement.getAttributes();
        Node classNode = importAttributes.getNamedItem("class");
        Node aliasNode = importAttributes.getNamedItem("alias");

        if (classNode == null) {
            throw new ORMUnitNodeProcessingException("import element is must have \"class\" attribute. It must be fully qualified class name");
        }
        String className = classNode.getNodeValue();
        int dotIndex = className.lastIndexOf(".");
        String alias = className.substring(dotIndex > -1 ? dotIndex + 1 : 0);
        if (aliasNode != null) {
            alias = aliasNode.getNodeValue();
        }
        addImport(result, className, alias);
        reader.registerNodeProcessor(alias, new EntityNodeProcessor(className, reader));
    }
}

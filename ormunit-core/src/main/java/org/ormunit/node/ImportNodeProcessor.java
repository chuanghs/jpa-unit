package org.ormunit.node;

import org.ormunit.ORMUnitPropertiesReader;
import org.ormunit.TestSet;
import org.ormunit.exception.ConfigurationException;
import org.ormunit.exception.NodeProcessingException;
import org.ormunit.node.entity.EntityNodeProcessor;
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
public class ImportNodeProcessor extends NodeProcessor {

    private static final Logger log = LoggerFactory.getLogger(ImportNodeProcessor.class);
    public static final String ClassNamePattern = "[$a-zA-Z_]+[$a-zA-Z_0-9]*(\\.[$a-zA-Z_]+[$a-zA-Z_0-9]*)*";

    private final WeakHashMap<TestSet, WeakReference<Map<String, String>>> confImports = new WeakHashMap<TestSet, WeakReference<Map<String, String>>>();


    public ImportNodeProcessor(ORMUnitPropertiesReader ormUnit) {
        super(ormUnit);
    }

    public void addImport(TestSet testSet, String className, String alias) {
        if (!className.matches(ClassNamePattern))
            throw new ConfigurationException("className: " + className + " is invalid class name");

        Map<String, String> imports = confImports.get(testSet) != null ? confImports.get(testSet).get() : null;
        if (imports == null) {
            confImports.put(testSet, new WeakReference<Map<String, String>>(imports = new HashMap<String, String>()));
        }

        if (imports.containsKey(alias) && !className.equals(imports.get(alias)))
            throw new ConfigurationException("alias: " + alias + " is defined more than once (" + imports.get(className) + ", " + className + ")");

        imports.put(alias, className);
    }

    public void process(Node jpaUnitElement, TestSet result) throws NodeProcessingException {
        NamedNodeMap importAttributes = jpaUnitElement.getAttributes();
        Node classNode = importAttributes.getNamedItem("class");
        Node aliasNode = importAttributes.getNamedItem("alias");

        if (classNode == null) {
            throw new NodeProcessingException("import element is must have \"class\" attribute. It must be fully qualified class name");
        }
        String className = classNode.getNodeValue();
        int dotIndex = className.lastIndexOf(".");
        String alias = className.substring(dotIndex > -1 ? dotIndex + 1 : 0);
        if (aliasNode != null) {
            alias = aliasNode.getNodeValue();
        }
        addImport(result, className, alias);
        try {
            result.registerNodeProcessor(alias, new EntityNodeProcessor(Class.forName(className)));
        } catch (ClassNotFoundException e) {
            throw new NodeProcessingException(String.format("Import node is declared for class: %s. but no such class could be found.", className), e);
        }
    }
}

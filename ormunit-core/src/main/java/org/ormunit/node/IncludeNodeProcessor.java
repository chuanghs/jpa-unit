package org.ormunit.node;

import org.ormunit.ORMUnitPropertiesReader;
import org.ormunit.TestSet;
import org.ormunit.command.TestSetCommand;
import org.ormunit.exception.ORMUnitFileReadException;
import org.ormunit.exception.ORMUnitNodeProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz Krzyzak
 * Date: 22.12.10
 * Time: 00:01
 */
public class IncludeNodeProcessor extends NodeProcessor {

    private static final Logger log = LoggerFactory.getLogger(IncludeNodeProcessor.class);

    public IncludeNodeProcessor(ORMUnitPropertiesReader ormUnit) {
        super(ormUnit);
    }


    public void process(Node jpaUnitElement, TestSet result) throws ORMUnitNodeProcessingException {
        Node srcNode = jpaUnitElement.getAttributes().getNamedItem("src");
        if (srcNode != null) {
            try {
                include(srcNode.getNodeValue().trim(), result);
            } catch (ORMUnitFileReadException e) {
                throw new ORMUnitNodeProcessingException(e);
            }
        }
    }

    public void include(String s, TestSet currentTestSet) throws ORMUnitFileReadException {
        String[] strings = getOrmUnit().normalizePath(s);
        String s1 = strings[0] + strings[1];

        TestSet includedTestSet = createTestSetIfNotReadBefore(s1, currentTestSet);
        if (includedTestSet != null) {

            includedTestSet = getOrmUnit().read(s, includedTestSet);

            currentTestSet.addCommand(new TestSetCommand(includedTestSet));
        } else {
            log.info("File: "+s+" was included before. Omitting.");
        }
    }

    static private WeakHashMap<TestSet, Map<String, TestSet>> readTestSets = new WeakHashMap<TestSet, Map<String, TestSet>>();

    public TestSet createTestSetIfNotReadBefore(String includeFileName, TestSet parent) {
        Map<String, TestSet> stringORMUnitTestSetMap = readTestSets.get(parent.getRootTestSet());
        if (stringORMUnitTestSetMap == null) {
            readTestSets.put(parent.getRootTestSet(), stringORMUnitTestSetMap = new HashMap<String, TestSet>());
        }
        TestSet result = stringORMUnitTestSetMap.get(includeFileName);
        if (result == null) {
            stringORMUnitTestSetMap.put(includeFileName, result = new TestSet(parent));
        } else {
            result = null;
        }
        return result;
    }
}

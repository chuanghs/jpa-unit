package org.ormunit;

import org.ormunit.command.ORMUnitCommand;
import org.ormunit.node.ANodeProcessor;
import org.ormunit.node.EntityNodeProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz Krzyzak
 * Date: 12.12.10
 * Time: 20:56
 */
public class ORMUnitTestSet {

    private static final Logger log = LoggerFactory.getLogger(ORMUnitTestSet.class);

    private final List<ORMUnitCommand> commands = new LinkedList<ORMUnitCommand>();

    private final ORMProvider provider;

    public ORMUnitTestSet(ORMProvider provider) {
        this.provider = provider;

        // adding entityNodeProcessor for every entity class defined in persistence unit
        Class[] managedTypes = provider.getManagedTypes();
        if (managedTypes != null)
            for (Class<?> c : managedTypes) {
                registerNodeProcessor(c.getSimpleName(), new EntityNodeProcessor(c.getCanonicalName()));
            }
    }

    public void addCommand(ORMUnitCommand command) {
        if (command != null)
            commands.add(command);
    }


    public void execute() {
        for (ORMUnitCommand command : commands) {
            command.visit(this);
        }
    }

    public ORMProvider getProvider() {
        return provider;
    }


    private Map<String, ANodeProcessor> nodeProcessorMap = new HashMap<String, ANodeProcessor>();

    public ANodeProcessor getNodeProcessor(String nodeName) {
        return this.nodeProcessorMap.get(nodeName);
    }

    public void registerNodeProcessor(String nodeType, ANodeProcessor aNodeProcessor) {
        nodeProcessorMap.put(nodeType, aNodeProcessor);
    }
}

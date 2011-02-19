package org.ormunit;

import org.ormunit.command.ORMUnitCommand;
import org.ormunit.node.NodeProcessor;
import org.ormunit.node.EntityNodeProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

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
    private ORMUnitTestSet parent;

    public ORMUnitTestSet(ORMUnitTestSet parent) {
        this(parent.getProvider());
        this.parent = parent;
    }

    public ORMUnitTestSet(ORMProvider provider) {
        this.provider = provider;

        // adding entityNodeProcessor for every entity class defined in persistence unit
        Set<Class<?>> managedTypes = provider.getManagedTypes();
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


    private Map<String, NodeProcessor> nodeProcessorMap = new HashMap<String, NodeProcessor>();

    public NodeProcessor getNodeProcessor(String nodeName) {
        return this.nodeProcessorMap.get(nodeName);
    }

    public void registerNodeProcessor(String nodeType, NodeProcessor aNodeProcessor) {
        nodeProcessorMap.put(nodeType, aNodeProcessor);
    }

    public ORMUnitTestSet getRootTestSet() {
        if (this.parent==null)
            return this;
        else
            return this.parent.getRootTestSet();
    }

    public ORMUnitTestSet getParentTestSet() {
        return this.parent;
    }
}

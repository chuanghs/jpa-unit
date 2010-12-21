package org.ormunit;

import org.ormunit.command.ORMUnitCommand;
import org.ormunit.exception.ORMUnitConfigurationException;
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
public class ORMUnitConfiguration {

    private static final Logger log = LoggerFactory.getLogger(ORMUnitConfiguration.class);

    public static final String ClassNamePattern = "[$a-zA-Z_]+[$a-zA-Z_0-9]*(\\.[$a-zA-Z_]+[$a-zA-Z_0-9]*)*";

    private final Map<String, String> imports = new HashMap<String, String>();

    private final List<ORMUnitCommand> commands = new LinkedList<ORMUnitCommand>();

    private final ORMProvider provider;

    public ORMUnitConfiguration(ORMProvider provider) {
        this.provider = provider;
    }

    public void addImport(String className, String alias) {
        if (imports.containsKey(alias)) {
            if (!className.equals(imports.get(alias)))
                throw new ORMUnitConfigurationException("alias: " + alias + " is defined more than once (" + imports.get(className) + ", " + className + ")");
            else {
                if (log.isWarnEnabled())
                    log.warn("alias: " + alias + " is defined twice for the same class: " + className);
            }
        }

        if (!className.matches(ClassNamePattern))
            throw new ORMUnitConfigurationException("className: " + className + " is invalid class name");

        imports.put(alias, className);

    }


    public void addCommand(ORMUnitCommand command) {
        commands.add(command);
    }


    public void execute() {
        for (ORMUnitCommand command : commands) {
            command.visit(provider);
        }
    }

    public ORMProvider getProvider() {
        return provider;
    }
}

package org.jpaunit;

import org.jpaunit.command.EntityCommand;
import org.jpaunit.command.JPAUnitCommand;
import org.jpaunit.command.JPAUnitCommandVisitor;
import org.jpaunit.command.StatementCommand;
import org.jpaunit.exception.JPAUnitConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz Krzyzak
 * Date: 12.12.10
 * Time: 20:56
 */
public class JPAUnitConfiguration {

    private static final Logger log = LoggerFactory.getLogger(JPAUnitConfiguration.class);

    public static final String ClassNamePattern = "[$a-zA-Z_]+[$a-zA-Z_0-9]*(\\.[$a-zA-Z_]+[$a-zA-Z_0-9]*)*";

    private Map<String, String> imports = new HashMap<String, String>();

    private List<JPAUnitCommand> commands = new LinkedList<JPAUnitCommand>();



    public void addImport(String className, String alias) {
        if (imports.containsKey(alias)) {
            if (!className.equals(imports.get(alias)))
                throw new JPAUnitConfigurationException("alias: " + alias + " is defined more than once (" + imports.get(className) + ", " + className + ")");
            else {
                if (log.isWarnEnabled())
                    log.warn("alias: " + alias + " is defined twice for the same class: " + className);
            }
        }

        if (!className.matches(ClassNamePattern))
            throw new JPAUnitConfigurationException("className: " + className + " is invalid class name");

        imports.put(alias, className);

    }


    public void addCommand(JPAUnitCommand command) {
        commands.add(command);
    }

    public void visit(JPAUnitCommandVisitor visitor) {
        for (JPAUnitCommand command : commands){
            command.visit(visitor);
        }
    }
}

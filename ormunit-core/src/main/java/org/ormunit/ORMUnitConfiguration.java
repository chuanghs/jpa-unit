package org.ormunit;

import org.ormunit.command.ORMUnitCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz Krzyzak
 * Date: 12.12.10
 * Time: 20:56
 */
public class ORMUnitConfiguration {

    private static final Logger log = LoggerFactory.getLogger(ORMUnitConfiguration.class);

    private final List<ORMUnitCommand> commands = new LinkedList<ORMUnitCommand>();

    private final ORMProvider provider;

    public ORMUnitConfiguration(ORMProvider provider) {
        this.provider = provider;
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

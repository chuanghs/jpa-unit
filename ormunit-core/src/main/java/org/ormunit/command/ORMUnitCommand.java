package org.ormunit.command;

import org.ormunit.TestSet;

/**
 * Created by IntelliJ IDEA.
 * User: krzyzak
 * Date: 28.12.10
 * Time: 16:05
 */
public interface ORMUnitCommand {

    void visit(TestSet testSet);

}

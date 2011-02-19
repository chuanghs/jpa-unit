package org.ormunit.command;

import org.ormunit.ORMUnit;
import org.ormunit.ORMUnitTestSet;

/**
 * Created by IntelliJ IDEA.
 * User: krzyzak
 * Date: 17.02.11
 * Time: 10:45
 */
public class TestSetCommand implements ORMUnitCommand{

    private ORMUnitTestSet testSet;

    public TestSetCommand(ORMUnitTestSet testSet){
        this.testSet = testSet;
    }

    public void visit(ORMUnitTestSet testSet) {
        this.testSet.execute();
    }
}

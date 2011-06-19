package org.ormunit.command;

import org.ormunit.TestSet;

/**
 * Created by IntelliJ IDEA.
 * User: krzyzak
 * Date: 17.02.11
 * Time: 10:45
 */
public class TestSetCommand implements ORMUnitCommand{

    private TestSet testSet;

    public TestSetCommand(TestSet testSet){
        this.testSet = testSet;
    }

    public void visit(TestSet testSet) {
        this.testSet.execute();
    }
}

package org.ormunit.junit;

import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;

/**
 * Created by IntelliJ IDEA.
 * User: jan
 * Date: 17.09.11
 * Time: 22:55
 * To change this template use File | Settings | File Templates.
 */
public abstract class BaseORMUnitRunner extends BlockJUnit4ClassRunner {

    /**
     * Creates a BlockJUnit4ClassRunner to run {@code klass}
     *
     * @throws org.junit.runners.model.InitializationError
     *          if the test class is malformed.
     */
    public BaseORMUnitRunner(Class<?> klass) throws InitializationError {
        super(klass);
    }


}

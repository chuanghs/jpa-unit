package org.ormunit.exception;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz Krzyzak
 * Date: 31.12.10
 * Time: 20:56
 */
public class ORMUnitInstantiationException extends ORMUnitAccessorException {
    public ORMUnitInstantiationException(String message) {
        super(message);
    }

    public ORMUnitInstantiationException(Throwable e) {
        super(e);
    }
}

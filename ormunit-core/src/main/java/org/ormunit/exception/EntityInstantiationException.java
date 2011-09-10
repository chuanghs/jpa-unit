package org.ormunit.exception;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz Krzyzak
 * Date: 31.12.10
 * Time: 20:56
 */
public class EntityInstantiationException extends AccessorException {

    public EntityInstantiationException(String message) {
        super(message);
    }

    public EntityInstantiationException(Exception e) {
        super(e);
    }
}

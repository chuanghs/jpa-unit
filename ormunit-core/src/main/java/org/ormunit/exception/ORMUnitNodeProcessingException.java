package org.ormunit.exception;

/**
 * Created by IntelliJ IDEA.
 * User: krzyzak
 * Date: 23.12.10
 * Time: 17:11
 */
public class ORMUnitNodeProcessingException extends RuntimeException {

    public ORMUnitNodeProcessingException(String message) {
        super(message);
    }


    public ORMUnitNodeProcessingException(Throwable e) {
        super(e);
    }
}

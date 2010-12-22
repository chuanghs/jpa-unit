package org.ormunit.exception;

/**
 * Created by IntelliJ IDEA.
 * User: krzyzak
 * Date: 23.12.10
 * Time: 17:11
 * To change this template use File | Settings | File Templates.
 */
public class ORMUnitNodeProcessingException extends Exception {

    public ORMUnitNodeProcessingException(String message) {
        super(message);
    }


    public ORMUnitNodeProcessingException(Throwable e) {
        super(e);
    }
}

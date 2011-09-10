package org.ormunit.exception;

/**
 * Created by IntelliJ IDEA.
 * User: krzyzak
 * Date: 23.12.10
 * Time: 17:11
 */
public class NodeProcessingException extends RuntimeException {

    public NodeProcessingException(String message) {
        super(message);
    }

    public NodeProcessingException(Throwable e) {
        super(e);
    }


}

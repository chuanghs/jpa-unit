package org.ormunit.exception;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz Krzyzak
 * Date: 12.12.10
 * Time: 20:47
 */
public class FileReadException extends Exception {

    public FileReadException(String message, Throwable cause) {
        super(message, cause);
    }

    public FileReadException(String message) {
        super(message);
    }

    public FileReadException(Throwable e) {
    }
}

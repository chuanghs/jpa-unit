package org.ormunit.exception;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz Krzyzak
 * Date: 12.12.10
 * Time: 21:11
 */
public class FileSyntaxException extends FileReadException {

    public FileSyntaxException(String message) {
        super(message);
    }

    public FileSyntaxException(String message, Throwable cause) {
        super(message, cause);
    }

}

package org.ormunit.exception;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz Krzyzak
 * Date: 13.12.10
 * Time: 21:05
 */
public class ConfigurationException extends RuntimeException {

    public ConfigurationException(String s, Exception cause) {
        super(s, cause);
    }

    public ConfigurationException(Exception e) {
        super(e);

    }

    public ConfigurationException(String message) {
        super(message);
    }
}

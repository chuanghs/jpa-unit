package org.ormunit.dialect;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz Krzyżak
 * Date: 14.06.11
 * Time: 23:10
 *
 */
public class DefaultDialect implements Dialect {
    public String getCreateSchemaStatement(String schemaName) {
        return "create schema " + schemaName.toUpperCase() + " authorization sa";
    }
}

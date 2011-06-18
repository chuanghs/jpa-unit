package org.ormunit.dialect;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz Krzyżak
 * Date: 14.06.11
 * Time: 23:09
 *
 */
public class HSQLDialect implements Dialect {
    public String getCreateSchemaStatement(String schemaName) {
        return "create schema " + schemaName.toUpperCase() + " authorization DBA";
    }
}

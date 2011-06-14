package org.ormunit;

/**
 * Created by IntelliJ IDEA.
 * User: jan kowalski
 * Date: 14.06.11
 * Time: 23:09
 * To change this template use File | Settings | File Templates.
 */
public class HSQLDialect implements Dialect {
    public String getCreateSchemaStatement(String schemaName) {
        return "create schema " + schemaName.toUpperCase() + " authorization DBA";
    }
}

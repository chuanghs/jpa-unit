package org.ormunit;

/**
 * Created by IntelliJ IDEA.
 * User: jan kowalski
 * Date: 14.06.11
 * Time: 23:11
 * To change this template use File | Settings | File Templates.
 */
public interface Dialect {
    String getCreateSchemaStatement(String schemaName);
}

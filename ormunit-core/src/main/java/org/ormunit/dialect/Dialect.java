package org.ormunit.dialect;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz Krzyżak
 * Date: 14.06.11
 * Time: 23:11
 */
public interface Dialect {

    String getCreateSchemaStatement(String schemaName);

    String getDefaultPrincipal();

}

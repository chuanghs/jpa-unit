package org.ormunit.jpa.unit;

import org.ormunit.jpa.entityinspector.EntityInspector;
import org.ormunit.jpa.providerproperties.ProviderProperties;

import java.util.Properties;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz Krzyzal (tomasz.krzyzak@gmail.com )
 * Date: 19.06.11
 * Time: 18:51
 */
public interface PersistenceUnit {

    String getPersistenceProvider();

    Set<Class<?>> getManagedTypes();

    Properties getProperties(Properties defaults);

    String getUnitName();

    ProviderProperties createProviderProperties(Properties defaultDataSourceProperties);

    EntityInspector createClassInspector();
}

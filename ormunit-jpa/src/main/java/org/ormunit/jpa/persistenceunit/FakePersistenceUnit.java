package org.ormunit.jpa.persistenceunit;

import org.ormunit.jpa.entityinspector.AnnotationsEntityInspector;
import org.ormunit.jpa.entityinspector.EntityInspector;
import org.ormunit.jpa.providerproperties.EclipseLinkProperties;
import org.ormunit.jpa.providerproperties.HibernatJPAProperties;
import org.ormunit.jpa.providerproperties.OpenJPAProperties;
import org.ormunit.jpa.providerproperties.ProviderProperties;

import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz Krzyzal (tomasz.krzyzak@gmail.com )
 * Date: 19.06.11
 * Time: 18:55
 */
public class FakePersistenceUnit implements PersistenceUnit {


    public String getPersistenceProvider() {
        String persistenceProvider = null;
        List<String> foundProviders = XmlPersistenceUnit.findAllProviders();
        if (foundProviders.size() > 0) {
            persistenceProvider = foundProviders.get(0);
        }
        return persistenceProvider;
    }

    public Set<Class<?>> getManagedTypes() {
        return new HashSet<Class<?>>();
    }

    public Properties getProperties(Properties defaults) {
        return new Properties(defaults);
    }

    public ProviderProperties createProviderProperties(Properties defaultDataSourceProperties) {
        String provider = getPersistenceProvider();
        if (EclipseLinkProperties.ProviderClassName.equals(provider)) {
            return new EclipseLinkProperties(this, defaultDataSourceProperties);
        } else if (OpenJPAProperties.providerClassName.equals(provider)) {
            return new OpenJPAProperties(this, defaultDataSourceProperties);
        } else if (HibernatJPAProperties.ProviderClassName.equals(provider)) {
            return new HibernatJPAProperties(this, defaultDataSourceProperties);
        } else
            throw new RuntimeException("unknown Persistence Provider");
    }

    public EntityInspector createClassInspector() {
        return new AnnotationsEntityInspector();
    }
}

package org.ormunit.unit;

import org.ormunit.inspector.AnnotationsEntityInspector;
import org.ormunit.inspector.EntityInspector;
import org.ormunit.provider.EclipseLinkProperties;
import org.ormunit.provider.HibernatJPAProperties;
import org.ormunit.provider.OpenJPAProperties;
import org.ormunit.provider.ProviderProperties;

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

    public String getUnitName() {
        return null;
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

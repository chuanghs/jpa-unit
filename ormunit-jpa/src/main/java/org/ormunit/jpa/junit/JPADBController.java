package org.ormunit.jpa.junit;

import org.ormunit.BaseDBController;
import org.ormunit.JPAORMProvider;
import org.ormunit.ORMUnitHelper;

import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: jan
 * Date: 18.09.11
 * Time: 20:07
 * To change this template use File | Settings | File Templates.
 */
public class JPADBController extends BaseDBController {


    private Properties properties;
    private String unitName;
    private JPAORMProvider provider;

    public JPADBController(Class<?> callerClass, String unitName, String ormUnitFileName) {
        super(callerClass, ormUnitFileName);

        this.unitName = unitName;
    }

    @Override
    protected final boolean isWithDB() {
        return !"false".equals(getProperties().getProperty("test_with_db." + unitName));
    }

    @Override
    public JPAORMProvider getProvider() {
        if (provider == null)
            provider = new JPAORMProvider(getORMUnit(), unitName);
        return provider;
    }

    @Override
    public void tearDown() {
        if (isWithDB()) {
            getProvider().tearDown();
        }
    }

    @Override
    public void setUp() {
        if (isWithDB()) {

            JPAORMProvider jpaormProvider = getProvider();
            jpaormProvider.setUp();

            executeTestSet();

            jpaormProvider.getEntityManager().flush();
            jpaormProvider.getEntityManager().clear();
        }
    }

    public Properties getProperties() {
        if (properties == null) {
            properties = ORMUnitHelper.readOrmUnitProperties(getCallerClass());
        }
        return properties;
    }
}

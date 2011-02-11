package org.ormunit.junit;

import junit.framework.TestCase;
import org.junit.After;
import org.junit.Before;
import org.ormunit.JPAORMProvider;
import org.ormunit.ORMUnit;
import org.ormunit.ORMUnitHelper;
import org.ormunit.ORMUnitTestSet;
import org.ormunit.exception.ORMUnitConfigurationException;
import org.ormunit.exception.ORMUnitFileReadException;
import org.ormunit.node.EntityNodeProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.Table;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.util.Properties;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: krzyzak
 * Date: 30.12.10
 * Time: 11:25
 */
public abstract class JPAUnitTestCase extends TestCase {

    private static final Logger log = LoggerFactory.getLogger(JPAUnitTestCase.class);
    private String unitName;
    private String ormUnitFileName;
    private ORMUnit ormUnit;

    private Properties properties;
    private JPAORMProvider provider;
    private ORMUnitTestSet testSet;
    private String persistenceProvider;


    public JPAUnitTestCase(String unitName) {
        this(unitName, null);
    }

    public JPAUnitTestCase(String unitName, String ormUnitFileName) {

        this.properties = ORMUnitHelper.readOrmUnitProperties(getClass());
        this.ormUnit = new ORMUnit(getClass());
        this.provider = createProvider(unitName);
        this.unitName = unitName;
        this.ormUnitFileName = ormUnitFileName;

        if (isWithDB()) {
            testSet = new ORMUnitTestSet(provider);

            // adding entityNodeProcessor for every entity class defined in persistence unit
            for (Class<?> c : getManagedTypes()) {
                testSet.registerNodeProcessor(c.getSimpleName(), new EntityNodeProcessor(c.getCanonicalName()));
            }

            InputStream inputStream = null;
            if (this.ormUnitFileName != null) {
                inputStream = getClass().getResourceAsStream(this.ormUnitFileName);
            } else {
                inputStream = getClass().getResourceAsStream("./" + getClass().getSimpleName() + ".xml");
            }
            if (inputStream != null) {
                try {
                    ormUnit.read(inputStream, testSet);
                } catch (ORMUnitFileReadException e) {
                    throw new ORMUnitConfigurationException(e);
                }
            }
            if (inputStream != null)
                try {
                    inputStream.close();
                } catch (IOException e) {
                    log.error(e.getMessage());
                }
        }
    }


    private String extractSchemaName(Class<?> c) {
        Table annotation = c.getAnnotation(Table.class);
        if (annotation != null && !"".equals(annotation.schema()))
            return annotation.schema();
        return null;
    }

    protected final boolean isWithDB() {
        return !"false".equals(properties.getProperty("test_with_db." + unitName));
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        if (isWithDB()) {
            Connection con = null;
            try {

                con = provider.getConnection();
                if (con != null) {
                    for (Class<?> c : getManagedTypes()) {
                        try {

                            String x = extractSchemaName(c);

                            if (x != null) {
                                log.info("creating schema: " + x);
                                con.prepareStatement("create schema " + x.toUpperCase()).executeUpdate();
                            }

                        } catch (Throwable e) {
                            log.error(e.getMessage());
                        }
                    }
                    con.close();
                }
            } catch (Exception e) {
                log.error(e.getMessage());
            }
            provider.setUp();
            testSet.execute();
        }
    }

    @After
    public void tearDown() throws Exception {
        if (isWithDB()) {
            provider.tearDown();
        }
        super.tearDown();
    }


    private JPAORMProvider createProvider(String unitName) {
        return new JPAORMProvider(ormUnit, unitName);
    }


    public EntityManager getEm() {
        return provider.getEntityManager();
    }

    private Set<Class> getManagedTypes() {
        return JPAHelper.getManagedTypes(getClass(), this.unitName);
    }


}

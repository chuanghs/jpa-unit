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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

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


    public JPAUnitTestCase(String unitName) {
        this(unitName, null);
    }

    public JPAUnitTestCase(String unitName, String ormUnitFileName) {

        this.properties = ORMUnitHelper.readOrmUnitProperties(getClass());
        this.ormUnit = new ORMUnit(getClass());
        this.unitName = unitName;
        this.provider = createProvider();
        this.ormUnitFileName = ormUnitFileName;

        if (isWithDB()) {
            testSet = new ORMUnitTestSet(provider);

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

    protected final boolean isWithDB() {
        return !"false".equals(properties.getProperty("test_with_db." + unitName));
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        if (isWithDB()) {

            provider.setUp();

            testSet.execute();

            provider.getEntityManager().flush();
            provider.getEntityManager().clear();
        }
    }

    @After
    public void tearDown() throws Exception {
        if (isWithDB()) {
            provider.tearDown();
        }
        super.tearDown();
    }


    private JPAORMProvider createProvider() {
        return new JPAORMProvider(ormUnit, unitName);
    }


    public EntityManager getEm() {
        return provider.getEntityManager();
    }


}

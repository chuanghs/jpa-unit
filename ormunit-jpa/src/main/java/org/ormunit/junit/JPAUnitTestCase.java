package org.ormunit.junit;

import junit.framework.TestCase;
import org.junit.After;
import org.junit.Before;
import org.ormunit.JPAORMProvider;
import org.ormunit.ORMUnitPropertiesReader;
import org.ormunit.ORMUnitHelper;
import org.ormunit.TestSet;
import org.ormunit.exception.ConfigurationException;
import org.ormunit.exception.FileReadException;
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
    private ORMUnitPropertiesReader ormUnit;

    private Properties properties;
    private JPAORMProvider provider;
    private TestSet testSet;


    public JPAUnitTestCase(String unitName) {
        this(unitName, null);
    }

    public JPAUnitTestCase(String unitName, String ormUnitFileName) {

        this.properties = ORMUnitHelper.readOrmUnitProperties(getClass());
        this.ormUnit = new ORMUnitPropertiesReader(getClass());
        this.unitName = unitName;
        this.provider = createProvider();
        this.ormUnitFileName = ormUnitFileName;

        if (isWithDB()) {
            testSet = new TestSet(provider);

            InputStream inputStream = null;
            if (this.ormUnitFileName != null) {
                inputStream = getClass().getResourceAsStream(this.ormUnitFileName);
            } else {
                inputStream = getClass().getResourceAsStream("./" + getClass().getSimpleName() + ".xml");
            }
            if (inputStream != null) {
                try {
                    ormUnit.read(inputStream, testSet);
                } catch (FileReadException e) {
                    throw new ConfigurationException(e);
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

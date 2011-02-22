package org.ormunit.junit;

import junit.framework.TestCase;
import org.hibernate.Session;
import org.junit.After;
import org.junit.Before;
import org.ormunit.HibernateORMProvider;
import org.ormunit.ORMUnit;
import org.ormunit.ORMUnitHelper;
import org.ormunit.ORMUnitTestSet;
import org.ormunit.exception.ORMUnitConfigurationException;
import org.ormunit.exception.ORMUnitFileReadException;
import org.ormunit.node.EntityNodeProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Table;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: jan kowalski
 * Date: 10.02.11
 * Time: 21:03
 */
public class HibernateUnitTestCase extends TestCase {

    private static final Logger log = LoggerFactory.getLogger(HibernateUnitTestCase.class);

    private String ormUnitFileName;
    private ORMUnit ormUnit;

    private Properties properties;
    private HibernateORMProvider provider;
    private ORMUnitTestSet testSet;
    private Session session;
    private String hibernateMappingFileName;


    public HibernateUnitTestCase(String hibernateMappingFileName, String ormUnitFileName) {


        this.properties = ORMUnitHelper.readOrmUnitProperties(getClass());
        this.ormUnit = new ORMUnit(getClass());
        this.hibernateMappingFileName = hibernateMappingFileName;
        this.ormUnitFileName = ormUnitFileName;
        this.provider = createProvider(hibernateMappingFileName);

        this.testSet = new ORMUnitTestSet(provider);

        if (isWithDB()) {
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

    private Class<Object>[] getManagedTypes() {
        Map allClassMetadata = getSession().getSessionFactory().getAllClassMetadata();
        Class<Object>[] result = new Class[allClassMetadata.size()];
        int i=0;
        Iterator iterator = allClassMetadata.entrySet().iterator();
        while (iterator.hasNext()){
            Map.Entry e = (Map.Entry) iterator.next();
            try {
                result[i++] = (Class<Object>) Class.forName((String) e.getKey());
            } catch (Exception e1) {
                throw new ORMUnitConfigurationException(e1);
            }
        }
        return result;
    }

    protected final boolean isWithDB() {
        return !"false".equals(properties.getProperty("test_with_db." + hibernateMappingFileName));
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        if (isWithDB()) {

            provider.setUp();

            testSet.execute();

            provider.getHibernateSession().flush();
            provider.getHibernateSession().clear();
        }
    }

    public Session getSession() {
        return provider.getHibernateSession();
    }

    @After
    public void tearDown() throws Exception {
        if (isWithDB()) {
            provider.tearDown();
        }
        super.tearDown();
    }

    private HibernateORMProvider createProvider(String hibernateMappingFileName) {
        return new HibernateORMProvider(ormUnit, hibernateMappingFileName);
    }

}

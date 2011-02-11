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
import java.sql.PreparedStatement;
import java.util.HashSet;
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

        ormUnit = new ORMUnit(getClass());
        this.provider = new JPAORMProvider(ormUnit, unitName);
        this.unitName = unitName;
        this.ormUnitFileName = ormUnitFileName;


        properties = ORMUnitHelper.readOrmUnitProperties(getClass());

        if (isWithDB()) {
            testSet = new ORMUnitTestSet(provider);

            // adding entityNodeProcessor for every entity class defined in persistence unit
            for (Class<?> c : JPAHelper.getManagedTypes(getClass(), this.unitName)) {
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

    public EntityManager getEm() {
        return provider.getEntityManager();
    }


    @Before
    public void setUp() throws Exception {
        super.setUp();
        if (isWithDB()) {
            long start = System.nanoTime();

            Connection con = null;
            try {

                con = provider.getConnection();
                if (con != null) {
                    Set<String> createdSchemas = new HashSet<String>();
                    for (Class<?> c : JPAHelper.getManagedTypes(getClass(), this.unitName)) {
                        try {

                            String x = extractSchemaName(c);
                            if (createdSchemas.contains(x))
                                continue;
                            createdSchemas.add(x);

                            if (x != null) {
                                log.info("creating schema: " + x);
                                PreparedStatement preparedStatement = con.prepareStatement("create schema " + x.toUpperCase());

                                int i = preparedStatement.executeUpdate();

                                con.prepareStatement("create table " + x + ".testtable (id int primary key)").executeUpdate();
                                con.prepareStatement("select * from  " + x + ".testtable").executeQuery();
                                log.info(i + "");
                            }

                        } catch (Throwable e) {
                            log.error(e.getMessage());
                        }
                    }

                }
            } catch (Exception e) {
                log.error(e.getMessage());
            }

            provider.setUp();

            testSet.execute();
            log.error("setting up testcase: "+(System.nanoTime()-start)/1000000.0+"ms");

            con.close();
        }
    }

    @After
    public void tearDown() throws Exception {
        if (isWithDB()) {
            provider.tearDown();
        }
        super.tearDown();
    }


}

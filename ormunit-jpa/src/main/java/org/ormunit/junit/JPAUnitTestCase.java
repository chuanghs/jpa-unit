package org.ormunit.junit;

import junit.framework.TestCase;
import org.junit.After;
import org.junit.Before;
import org.ormunit.JPAORMProvider;
import org.ormunit.ORMUnit;
import org.ormunit.ORMUnitHelper;
import org.ormunit.node.EntityNodeProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: krzyzak
 * Date: 30.12.10
 * Time: 11:25
 */
public abstract class JPAUnitTestCase extends TestCase {

    private static final Logger log = LoggerFactory.getLogger(JPAUnitTestCase.class);

    private static Map<String, EntityManagerFactory> entityManagerFactories = new HashMap<String, EntityManagerFactory>();
    private String unitName;
    private Properties properties;
    private String ormUnitFileName;
    private EntityManager em;
    private ORMUnit ormUnit;

    public JPAUnitTestCase(String unitName) {
        this(unitName, null);
    }

    public JPAUnitTestCase(String unitName, String ormUnitFileName) {
        this.unitName = unitName;
        this.ormUnitFileName = ormUnitFileName;

        ormUnit = new ORMUnit(getClass(), properties = ORMUnitHelper.readOrmUnitProperties(getClass()));
        String fullUnitName = ormUnit.getDefaultDataSourceName() + unitName;

        if (isWithDB() && entityManagerFactories.get(fullUnitName) == null) {
            entityManagerFactories.put(
                    fullUnitName,
                    javax.persistence.Persistence.createEntityManagerFactory(unitName, ormUnit.getDefaultDataSourceProperties()));
        }
    }

    protected final boolean isWithDB() {
        return !"false".equals(properties.get("test_with_db." + unitName));
    }

    public EntityManager getEm() {
        return em;
    }


    @Before
    public void setUp() throws Exception {
        super.setUp();
        if (isWithDB()) {
            String fullUnitName = ormUnit.getDefaultDataSourceName() + unitName;

            em = entityManagerFactories.get(fullUnitName).createEntityManager(ormUnit.getDefaultDataSourceProperties());
            em.getTransaction().begin();

            InputStream inputStream = null;
            if (this.ormUnitFileName != null) {
                inputStream = getClass().getResourceAsStream(this.ormUnitFileName);
            } else {
                inputStream = getClass().getResourceAsStream("./" + getClass().getSimpleName() + ".xml");
            }


            if (inputStream != null) {

                for (Class<?> c : JPAHelper.getManagedTypes(getClass(), this.unitName)) {
                    ormUnit.registerNodeProcessor(c.getSimpleName(), new EntityNodeProcessor(c.getCanonicalName(), ormUnit));
                }


                ormUnit.read(inputStream, new JPAORMProvider(getEm()))
                        .execute();
            }

            if (inputStream != null)
                inputStream.close();
        }
    }

    @After
    public void tearDown() throws Exception {
        if (isWithDB()) {
            em.clear();
            em.close();
            em.getTransaction().rollback();
        }
        super.tearDown();
    }


}

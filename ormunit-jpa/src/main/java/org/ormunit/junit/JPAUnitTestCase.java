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

    public JPAUnitTestCase(String unitName) {
        this(unitName, null);
    }

    public JPAUnitTestCase(String unitName, String ormUnitFileName) {
        this.unitName = unitName;
        this.ormUnitFileName = ormUnitFileName;
        properties = ORMUnitHelper.readOrmUnitProperties(getClass());

        if (isWithDB() && entityManagerFactories.get(unitName) == null) {

            EntityManagerFactory entityManagerFactory = javax.persistence.Persistence.createEntityManagerFactory(unitName);

            entityManagerFactories.put(unitName, entityManagerFactory);
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
            em = entityManagerFactories.get(this.unitName).createEntityManager();
            em.getTransaction().begin();

            InputStream inputStream = null;
            if (this.ormUnitFileName != null) {
                inputStream = getClass().getResourceAsStream(this.ormUnitFileName);
            } else {
                inputStream = getClass().getResourceAsStream("./" + getClass().getSimpleName() + ".xml");
            }


            if (inputStream != null) {
                ORMUnit reader = new ORMUnit(getClass(), this.properties);

                for (Class<?> c : JPAHelper.getManagedTypes(getClass(), this.unitName)) {
                    reader.registerNodeProcessor(c.getSimpleName(), new EntityNodeProcessor(c.getCanonicalName(), reader));
                }


                reader.read(inputStream, new JPAORMProvider(getEm()))
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

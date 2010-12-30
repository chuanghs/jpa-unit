package org.ormunit.junit;

import junit.framework.TestCase;
import org.ormunit.ORMUnitConfigurationReader;
import org.ormunit.command.JPAORMProvider;
import org.ormunit.command.JPAUnitCommandVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: krzyzak
 * Date: 30.12.10
 * Time: 11:25
 * To change this template use File | Settings | File Templates.
 */
public abstract class JPAUnitTestCase extends TestCase {

    private static final Logger log = LoggerFactory.getLogger(JPAUnitTestCase.class);

    private static Map<String, EntityManagerFactory> entityManagerFactories = new HashMap<String, EntityManagerFactory>();
    private String unitName;
    private Properties properties;
    private String ormUnitFileName;
    private EntityManager em;

    public JPAUnitTestCase(String unitName, String ormUnitFileName) {
        this.unitName = unitName;
        this.ormUnitFileName = ormUnitFileName;
        properties = new Properties();

        try {
            properties.load(getClass().getResourceAsStream("/ormunit.properties"));
        } catch (IOException e) {
            log.error("", e);
        }

        if (isWithDB() && entityManagerFactories.get(unitName) == null)
            entityManagerFactories.put(unitName, Persistence.createEntityManagerFactory(unitName));
    }

    protected final boolean isWithDB() {
        return !"false".equals(properties.get("test_with_db." + unitName));
    }

    public EntityManager getEm() {
		return em;
	}


    public void setUp() throws Exception {
        super.setUp();
        if (isWithDB()) {
            em = entityManagerFactories.get(this.unitName).createEntityManager();
            em.getTransaction().begin();

            new ORMUnitConfigurationReader(new JPAORMProvider())
                    .read(getClass().getResourceAsStream(this.ormUnitFileName))
                    .visit(new JPAUnitCommandVisitor(getEm()));
        }
    }

}

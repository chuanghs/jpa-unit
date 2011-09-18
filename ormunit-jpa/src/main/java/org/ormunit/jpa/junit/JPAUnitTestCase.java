package org.ormunit.jpa.junit;

import junit.framework.TestCase;
import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;

/**
 * Created by IntelliJ IDEA.
 * User: krzyzak
 * Date: 30.12.10
 * Time: 11:25
 */
public abstract class JPAUnitTestCase extends TestCase {

    private static final Logger log = LoggerFactory.getLogger(JPAUnitTestCase.class);

    private JPADBController dbController;


    public JPAUnitTestCase(String unitName) {
        this(unitName, null);
    }

    public JPAUnitTestCase(String unitName, String ormUnitFileName) {
        dbController = new JPADBController(getClass(), unitName, ormUnitFileName);
    }

    @Before
    public void setUp() throws Exception {
        dbController.setUp();
    }

    @After
    public void tearDown() throws Exception {
        dbController.tearDown();
    }


    public EntityManager getEm() {
        return dbController.getProvider().getEntityManager();
    }


}

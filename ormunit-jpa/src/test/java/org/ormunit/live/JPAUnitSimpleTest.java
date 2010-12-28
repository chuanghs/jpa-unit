package org.ormunit.live;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.ormunit.junit.Helper;
import org.ormunit.junit.JPAUnitTestCase;

import javax.xml.bind.JAXBException;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz Krzyzak
 * Date: 28.12.10
 * Time: 21:37
 */
@RunWith(MockitoJUnitRunner.class)
public class JPAUnitSimpleTest extends JPAUnitTestCase {

    public JPAUnitSimpleTest() {
        super("ormunit-jpa");
    }

    @Test
    public void testTransaction() throws JAXBException {
        assertTrue(getEm().getTransaction().isActive());

        assertEquals(2, Helper.getManagedTypes(getClass(), "ormunit-jpa").size());
    }


}

package org.ormunit.example;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.ormunit.jpa.junit.JPAUnitTestCase;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz Krzyzak
 * Date: 28.12.10
 * Time: 21:37
 */
@RunWith(MockitoJUnitRunner.class)
public class JPAUnitEntitiesWithComposedIdsTest extends JPAUnitTestCase {

    public JPAUnitEntitiesWithComposedIdsTest() {
        super("ormunit-jpa-composed-ids");
    }

    @Test
    public void testReadAllEntities() {
        EmployeeId ids = new EmployeeId("PL", 1);


        assertNotNull(getEm().find(Employee.class, ids));

    }


    @Test
    public void testReferences() {
        PhoneNumber phoneNumber = getEm().find(PhoneNumber.class, 1);

        assertNotNull(phoneNumber);

        assertEquals(1, phoneNumber.getContact().geteId().intValue());
        assertEquals("PL", phoneNumber.getContact().getCountry());

    }

}

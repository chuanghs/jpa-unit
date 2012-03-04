package org.ormunit.live.dinoo333.t1;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ormunit.jpa.annotations.Em;
import org.ormunit.jpa.annotations.JPAUnitTestCase;
import org.ormunit.jpa.junit.JPAUnitRunner;

import javax.persistence.EntityManager;

/**
 * Created by IntelliJ IDEA.
 * User: jan
 * Date: 04.03.12
 * Time: 22:25
 * To change this template use File | Settings | File Templates.
 */
@RunWith(JPAUnitRunner.class)
@JPAUnitTestCase(unitName = "dinoo333.t1", ormUnitFileName = "ormunit.xml")
public class SetReferenceTest {
    
    @Em
    EntityManager em;
    
    @Test
    public void testAllEntitiesArePersisted(){
        Assert.assertNotNull(em.find(Foo.class, "foo.1"));
        Assert.assertNotNull(em.find(Foo.class, "foo.2"));

        Assert.assertNotNull(em.find(Bar.class, 1l));
        Assert.assertNotNull(em.find(Bar.class, 2l));

        Assert.assertNotNull(em.find(FooBarAssociation.class, 1l));
        Assert.assertNotNull(em.find(FooBarAssociation.class, 2l));
    }
    
}

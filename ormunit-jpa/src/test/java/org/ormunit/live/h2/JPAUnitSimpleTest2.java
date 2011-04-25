package org.ormunit.live.h2;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.ormunit.entity.Auction;
import org.ormunit.entity.AuctionParamValue;
import org.ormunit.entity.FieldAccessEntity;
import org.ormunit.entity.PropertyAccessEntity;
import org.ormunit.junit.JPAHelper;
import org.ormunit.junit.JPAUnitTestCase;

import javax.xml.bind.JAXBException;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz Krzyzak
 * Date: 28.12.10
 * Time: 21:37
 */
@RunWith(MockitoJUnitRunner.class)
public class JPAUnitSimpleTest2 extends JPAUnitTestCase {

    public JPAUnitSimpleTest2() {
        super("ormunit-so-unused");
    }

    @Test
    public void testTransaction() throws JAXBException {
        Auction entity = new Auction();
        getEm().persist(entity);
        List resultList = getEm().createQuery("select o from " + Auction.class.getSimpleName() + " o ").getResultList();
        Assert.assertEquals(1, resultList.size());
    }



}

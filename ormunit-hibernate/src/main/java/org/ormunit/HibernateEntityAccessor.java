package org.ormunit;

import org.ormunit.entity.AEntityAccessor;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz Krzyzak
 * Date: 23.12.10
 * Time: 10:24
 */
public class HibernateEntityAccessor extends AEntityAccessor {

    public Class getEntityClass() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Object newInstance(String propertyName) {
        return null;
    }

    public Class getType(String propertyName) {
        return null;
    }

    public void set(Object entity, String propertyName, Object value) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public Object get(Object entity, String propertyName) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Class getCollectionParameterType(String propertyName) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}

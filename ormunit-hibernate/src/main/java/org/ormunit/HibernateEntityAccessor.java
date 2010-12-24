package org.ormunit;

import org.ormunit.entity.AEntityAccessor;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz Krzyzak
 * Date: 23.12.10
 * Time: 10:24
 */
public class HibernateEntityAccessor extends AEntityAccessor {

    public Object newInstance(String propertyName) {
        return null;
    }

    public Class getType(String propertyName) {
        return null;
    }

    public void set(Object entity, String propertyName, Object value) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}

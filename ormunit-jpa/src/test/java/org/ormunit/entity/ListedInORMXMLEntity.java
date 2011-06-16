package org.ormunit.entity;

import javax.persistence.Id;

/**
 * Created by IntelliJ IDEA.
 * User: jan kowalski
 * Date: 16.06.11
 * Time: 15:53
 * To change this template use File | Settings | File Templates.
 */
public class ListedInORMXMLEntity {

    @Id
    private Integer id;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}

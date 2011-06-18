package org.ormunit.entity;

import javax.persistence.Id;

/**
 * Created by IntelliJ IDEA.
 * User: Tomasz Krzyżak
 * Date: 16.06.11
 * Time: 15:53
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

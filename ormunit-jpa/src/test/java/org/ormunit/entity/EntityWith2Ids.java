package org.ormunit.entity;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;

/**
 * Created by IntelliJ IDEA.
 * User: jan kowalski
 * Date: 21.04.11
 * Time: 21:43
 * To change this template use File | Settings | File Templates.
 */
@Entity
@IdClass(EntityWith2Ids.class)
public class EntityWith2Ids {

    @Id
    private Integer id1;

    @Id
    private Integer id2;

    public Integer getId1() {
        return id1;
    }

    public void setId1(Integer id1) {
        this.id1 = id1;
    }

    public Integer getId2() {
        return id2;
    }

    public void setId2(Integer id2) {
        this.id2 = id2;
    }
}

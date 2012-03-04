package org.ormunit.live.dinoo333.t1;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class FooBarAssociation {

    @Id
    private long fooBarAssociationId;

    @ManyToOne
    private Foo foo;

    @ManyToOne
    private Bar bar;

}
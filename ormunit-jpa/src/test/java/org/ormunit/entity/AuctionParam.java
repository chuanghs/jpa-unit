package org.ormunit.entity;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Access(AccessType.FIELD)
public class AuctionParam {

    @Id
    private Integer id;

    private String description;

}
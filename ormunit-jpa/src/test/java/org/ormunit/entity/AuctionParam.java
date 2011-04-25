package org.ormunit.entity;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class AuctionParam {

    @Id
    private Integer id;

    private String description;

}
package org.ormunit.entity;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(schema = "testschema")
public class AuctionParamValue {

    @Id
    @ManyToOne
    private Auction auction;

    @Id
    @ManyToOne
    private AuctionParam auctionParam;

    private String value;

}
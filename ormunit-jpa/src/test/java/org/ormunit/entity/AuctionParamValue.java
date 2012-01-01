package org.ormunit.entity;

import javax.persistence.*;

@Entity
@Table(schema = "testschema")
@Access(AccessType.FIELD)
public class AuctionParamValue {

    @Id
    @ManyToOne
    private Auction auction;

    @Id
    @ManyToOne
    private AuctionParam auctionParam;

    private String value;

}
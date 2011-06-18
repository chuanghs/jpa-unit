package org.ormunit.entity;

import javax.persistence.*;
import java.util.Map;

@Entity
public class Auction {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @OneToMany(mappedBy = "auction")
    @MapKey(name = "auctionParam")
    private Map<AuctionParam, AuctionParamValue> values;

}
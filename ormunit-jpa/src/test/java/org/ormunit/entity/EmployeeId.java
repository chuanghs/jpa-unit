package org.ormunit.entity;

import java.io.Serializable;

public class EmployeeId implements Serializable {

    private Integer eId;
    private String country;


    public EmployeeId() {

    }

    public EmployeeId(String country, Integer eId) {
        this.country = country;
        this.eId = eId;
    }
}
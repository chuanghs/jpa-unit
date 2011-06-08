package org.ormunit.entity;

public class EmployeeId {

    private Integer eId;
    private String country;


    public EmployeeId() {

    }

    public EmployeeId(String country, Integer eId) {
        this.country = country;
        this.eId = eId;
    }
}
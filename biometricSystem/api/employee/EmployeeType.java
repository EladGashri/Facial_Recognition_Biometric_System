package com.biometricsystem.api.employee;


public enum EmployeeType {
    UNDEFINED ("Undefined"),
    STANDARD ("Standard"),
    ADMIN ("Admin");

    private final String name;

    EmployeeType(String name){
        this.name=name;
    }

    public String getName(){
        return name;
    }
}
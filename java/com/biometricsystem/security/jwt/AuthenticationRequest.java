package com.biometricsystem.security.jwt;


public class AuthenticationRequest {
    private long id;
    private long employeeNumber;


    public AuthenticationRequest(long id, long employeeNumber) {
        this.id = id;
        this.employeeNumber = employeeNumber;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setEmployeeNumber(long employeeNumber) {
        this.employeeNumber = employeeNumber;
    }

    public String getId() {
        return String.valueOf(id);
    }

    public String getEmployeeNumber() {
        return String.valueOf(employeeNumber);
    }

}

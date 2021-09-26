package com.biometricsystem.security;
import org.springframework.security.core.GrantedAuthority;


public enum EmployeeType implements GrantedAuthority {

    UNDEFINED ("Undefined",0),
    STANDARD ("Standard",1),
    ADMIN ("Admin",2),
    CTO("CTO",3);

    private final String name;
    private final int databaseValue;

    EmployeeType(String name,int databaseValue){
        this.name=name;
        this.databaseValue=databaseValue;
    }

    @Override
    public String getAuthority() {
        return getName();
    }

    public static EmployeeType getEmployeeTypeByDatabaseValue(int databaseValue) {
        for (EmployeeType type : values()) {
            if (type.getDatabaseValue()==databaseValue) {
                return type;
            }
        }
        return EmployeeType.UNDEFINED;
    }

    public static EmployeeType getEmployeeTypeByName(String name) {
        for (EmployeeType type : values()) {
            if (type.getName().equals(name)){
                return type;
            }
        }
        return EmployeeType.UNDEFINED;
    }

    public String getName(){
        return name;
    }

    public int getDatabaseValue() {
        return databaseValue;
    }

}
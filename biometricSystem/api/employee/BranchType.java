package com.biometricsystem.api.employee;


public enum BranchType {

    //delete this part and add the part below after the database has been updated
    UNDEFINED ("Undefined", null),
    TA("Tel Aviv", "A"),
    SF("San Francisco", "B"),
    TO("Tokyo", "C"),
    BN("Berlin", "D");

    /*UNDEFINED ("Undefined", null),
    TA("Tel Aviv", "TA"),
    SF("San Francisco", "SF"),
    TO("Tokyo", "TO"),
    BN("Berlin", "BN");*/

    private final String name;
    private final String databaseValue;

    BranchType(String name, String databaseValue){
        this.name=name;
        this.databaseValue=databaseValue;
    }

    public static BranchType getBranchTypeByDatabaseValue(String databaseValue) {
        for (BranchType branch : values()) {
            if (branch.getDatabaseValue()!=null && branch.getDatabaseValue().equals(databaseValue)) {
                return branch;
            }
        }
        return BranchType.UNDEFINED;
    }

    public String getName(){
        return name;
    }

    public String getDatabaseValue(){
        return databaseValue;
    }

}

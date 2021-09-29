package com.biometricsystem.branch;
import java.net.InetAddress;
import java.net.UnknownHostException;


public enum BranchLocation {

    UNDEFINED ("Undefined", null,null),
    TA("Tel Aviv", "TA","localhost"),
    SF("San Francisco", "SF",null),
    TO("Tokyo", "TO",null),
    BN("Berlin", "BN",null);

    private final String location;
    private final String databaseValue;
    private final String address;

    BranchLocation(String location, String databaseValue, String address){
        this.location = location;
        this.databaseValue=databaseValue;
        this.address=address;
    }

    public static BranchLocation getBranchLocationByDatabaseValue(String databaseValue) {
        if (databaseValue==null){
            return BranchLocation.UNDEFINED;
        }
        for (BranchLocation branch : values()) {
            if (branch.getDatabaseValue()!=null && branch.getDatabaseValue().equals(databaseValue)) {
                return branch;
            }
        }
        return BranchLocation.UNDEFINED;
    }

    public static BranchLocation getBranchLocationByLocation(String location) {
        if (location==null){
            return BranchLocation.UNDEFINED;
        }
        for (BranchLocation branch : values()) {
            if (branch.getLocation()!=null && branch.getLocation().equals(location)) {
                return branch;
            }
        }
        return BranchLocation.UNDEFINED;
    }

    public InetAddress getAddress() throws UnknownHostException {
        return InetAddress.getByName(address);
    }

    public String getLocation(){
        return location;
    }

    public String getDatabaseValue(){
        return databaseValue;
    }

}
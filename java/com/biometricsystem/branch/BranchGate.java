package com.biometricsystem.branch;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;


public class BranchGate {
    private final BranchLocation branch;
    private AtomicInteger numberOfPeopleEntering=new AtomicInteger(0);
    private AtomicBoolean open=new AtomicBoolean(false);
    private static BranchGate[] gates;
    private static HashMap<InetAddress, BranchGate> addressToGate=new HashMap<>();

    static {
        gates=new BranchGate[BranchLocation.values().length];
        for (int i=0;i<BranchLocation.values().length;i++){
            gates[i]=new BranchGate(BranchLocation.values()[i]);
            try {
                addressToGate.put(gates[i].getBranch().getAddress(),gates[i]);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }
    }

    private BranchGate(BranchLocation branch) {
        this.branch=branch;
    }

    public synchronized void openGate() {
        if (numberOfPeopleEntering.addAndGet(1)-1==0){
            open.set(true);
            System.out.println("Gate in " + branch.getLocation() + " opened");
        }
    }

    public synchronized void closeGate(){
        if (numberOfPeopleEntering.addAndGet(-1)==0) {
            open.set(false);
            System.out.println("Gate in "+branch.getLocation()+" closed");
        }
    }

    public BranchLocation getBranch() {
        return branch;
    }

    public AtomicBoolean getOpen() {
        return open;
    }

    public static BranchGate[] getAllGates(){
        return gates;
    }

    public static HashMap<InetAddress, BranchGate> getAddressToGate(){
        return addressToGate;
    }

}
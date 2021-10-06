package com.biometricsystem.branch;


public class BranchGateSetter implements Runnable {
    private final BranchGate gate;
    private static final int DURATION_FOR_GATE_TO_BE_OPEN=10000; //10 seconds in milliseconds

    public BranchGateSetter(BranchGate gate){
        this.gate=gate;
    }

    @Override
    public void run(){
        gate.openGate();
        try {
            Thread.sleep(DURATION_FOR_GATE_TO_BE_OPEN);
        }catch (InterruptedException e){
            e.printStackTrace();
        }
        gate.closeGate();
    }

}
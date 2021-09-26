package com.biometricsystem.livefeed.server;
import com.biometricsystem.api.repository.AttendanceRepository;
import com.biometricsystem.branch.BranchGate;
import com.biometricsystem.branch.BranchGateSetter;
import com.biometricsystem.database.Time;
import com.biometricsystem.entity.image.CapturedFrame;
import java.net.InetAddress;
import java.util.HashMap;
import org.springframework.beans.factory.annotation.Autowired;


public class EntryRegister implements Runnable{

    @Autowired
    private AttendanceRepository attendanceRegisterService;
    private MultiThreadedQueue<CapturedFrame> framesQueue;
    private HashMap<InetAddress, BranchGate> addressToGate;

    public EntryRegister(MultiThreadedQueue<CapturedFrame> framesQueue) {
        this.framesQueue = framesQueue;
        addressToGate=BranchGate.getAddressToGate();
    }

    @Override
    public void run() {
        BranchGate currentGate;
        CapturedFrame currentFrame;
        while(true) {
            currentFrame=framesQueue.dequeue();
            try {
                attendanceRegisterService.registerEntryNow(currentFrame.getEmployee());
            } catch (Time.TimeException timeException) {
                timeException.printStackTrace();
            }
            System.out.println("Captured frame in EntryRegister");
            currentGate=addressToGate.get(currentFrame.getAddress());
            new Thread(new BranchGateSetter(currentGate)).start();
        }
    }

}
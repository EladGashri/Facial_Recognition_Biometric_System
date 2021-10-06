package com.biometricsystem.livefeed.server;
import com.biometricsystem.api.BeanUtil;
import com.biometricsystem.api.repository.AttendanceRepository;
import com.biometricsystem.api.service.AttendancesService;
import com.biometricsystem.branch.BranchGate;
import com.biometricsystem.branch.BranchGateSetter;
import com.biometricsystem.database.Time;
import com.biometricsystem.entity.image.CapturedFrame;
import java.net.InetAddress;
import java.util.HashMap;

import com.biometricsystem.entity.image.FaceRecognitionResult;
import com.biometricsystem.model.FaceClassifier;
import org.springframework.beans.factory.annotation.Autowired;


public class EntryRegister implements Runnable{

    private final AttendancesService attendancesService = BeanUtil.getBean(AttendancesService.class);
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
        while (true) {
            currentFrame = framesQueue.dequeue();
            try {
                if (currentFrame.getResult() == FaceRecognitionResult.FACE_RECOGNIZED) {
                    if (attendancesService.checkAndRegisterEntry(currentFrame.getEmployee())) {
                        currentGate = addressToGate.get(currentFrame.getAddress());
                        new Thread(new BranchGateSetter(currentGate)).start();
                    }
                }
            } catch (Time.TimeException timeException) {
                timeException.printStackTrace();
            }
        }
    }

}
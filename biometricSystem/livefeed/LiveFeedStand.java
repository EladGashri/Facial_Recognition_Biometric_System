package com.biometricsystem.livefeed;
import com.biometricsystem.image.CapturedFrame;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.concurrent.atomic.AtomicBoolean;


public class LiveFeedStand extends LiveFeed {

    private final LocalTime startTime=LocalTime.parse("08:00:00");
    private final LocalTime endTime=LocalTime.parse("17:00:00");
    private AtomicBoolean entryStatus;
    private VideoCapture capture;
    private Mat frame;
    private AtomicBoolean streaming;
    private AtomicBoolean pause;
    private LiveFeedStandGUI gui;


    public LiveFeedStand(){
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        entryStatus=new AtomicBoolean(false);
        gui=new LiveFeedStandGUI(this,entryStatus);
        nu.pattern.OpenCV.loadLocally();
        capture=new VideoCapture(0);
        frame=new Mat();
        streaming= new AtomicBoolean(true);
        pause=new AtomicBoolean(false);
    }

    public void startLiveFeed(){
        gui.start();
        System.out.println("Live feed is starting\n");
        boolean captureResult;
        while (streaming.get() && capture.isOpened() && getDate().equals(LocalDate.now()) && LocalTime.now().isBefore(startTime) && LocalTime.now().isBefore(endTime)){
            while(pause.get()){
                System.out.println("The live feed has been paused");
                try{
                    this.wait();
                } catch (InterruptedException e) {
                    System.err.println("Live feed pause interrupted");
                }
            }
            captureResult=capture.read(frame);
            if (!captureResult || frame.empty()){
                break;
            }
            //send CapturedFrame via UDP
            CapturedFrame newFrame=new CapturedFrame(frame);
        }
        if (!capture.isOpened() || !capture.read(frame)) {
            System.err.println("No access to camera");
        }else{
            System.out.println("Daily live feed has ended");
        }
    }

    public void stopLiveFeed(){
        streaming.set(false);
    }

}



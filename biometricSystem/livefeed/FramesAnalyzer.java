package com.biometricsystem.livefeed;
import com.biometricsystem.image.CapturedFrame;
import com.biometricsystem.image.FaceRecognitionResult;
import org.opencv.core.Mat;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;


public class FramesAnalyzer extends Thread{
    private final LinkedList<CapturedFrame> framesQueue;
    private AtomicBoolean beAlive;

    public FramesAnalyzer(LinkedList<CapturedFrame> queue){
        setName("FramesAnalyzer");
        framesQueue=queue;
        beAlive=new AtomicBoolean(true);
    }

    @Override
    public void run(){
        while (beAlive.get()){
            //get CapturedFrame from UDP
            CapturedFrame currentFrame=new CapturedFrame(new Mat());
            currentFrame.setFaceImage();
            if (currentFrame.wasAFaceDetected()) {
                currentFrame.identify();
            }
            currentFrame.setResult();
            //delete later
            if (currentFrame.getResult() == FaceRecognitionResult.FACE_RECOGNIZED) {
                System.out.println("FACE RECOGNIZED IN FRAMES ANALYZER");
            }else if (currentFrame.getResult() == FaceRecognitionResult.FACE_DETECTED_AND_NOT_RECOGNIZED) {
                System.out.println("FACE DETECTED AND NOT RECOGNIZED IN FRAMES ANALYZER");
            }else if (currentFrame.getResult() == FaceRecognitionResult.FACE_NOT_DETECTED) {
                System.out.println("FACE NOT DETECTED IN FRAMES ANALYZER");
            }
            synchronized (framesQueue) {
                framesQueue.addLast(currentFrame);
                framesQueue.notify();
            }
        }
    }

    public void kill(){
        beAlive.set(false);
    }
}
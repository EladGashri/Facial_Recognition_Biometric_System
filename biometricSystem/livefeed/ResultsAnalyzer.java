package com.biometricsystem.livefeed;
import com.biometricsystem.image.CapturedFrame;
import com.biometricsystem.image.FaceRecognitionResult;
import java.util.LinkedList;


public class ResultsAnalyzer{

    private final LiveFeed liveFeed=new LiveFeed();
    private FramesAnalyzer framesAnalyzer;
    private LinkedList<CapturedFrame> framesQueue;

    public ResultsAnalyzer() {
        framesQueue=new LinkedList<>();
    }

    public void analyze(){
        liveFeed.updateAll();
        framesAnalyzer=new FramesAnalyzer(framesQueue);
        framesAnalyzer.start();
        CapturedFrame currentFrame;
        synchronized (framesQueue) {
            while (framesQueue.isEmpty()) {
                System.out.println("RESULTS ANALYZER INQUEUE IS EMPTY");
                try {
                    framesQueue.wait();
                } catch (InterruptedException e) {
                    System.err.println("analyzedFramesQueue dequeue interrupted");
                }
            }
            currentFrame = framesQueue.removeFirst();
        }
        if (currentFrame.getResult() == FaceRecognitionResult.FACE_RECOGNIZED) {
            System.out.println("FACE RECOGNIZED IN RESULTS ANALYZER\n");
            liveFeed.checkAndRegisterEntry(currentFrame);
        } else if (currentFrame.getResult() == FaceRecognitionResult.FACE_DETECTED_AND_NOT_RECOGNIZED) {
            System.out.println("FACE DETECTED AND NOT RECOGNIZED IN RESULTS ANALYZER\n");
        }else if (currentFrame.getResult() == FaceRecognitionResult.FACE_NOT_DETECTED) {
            System.out.println("FACE NOT DETECTED IN RESULTS ANALYZER\n");
        }
        //send CapturedFrame back via UDP
    }


}
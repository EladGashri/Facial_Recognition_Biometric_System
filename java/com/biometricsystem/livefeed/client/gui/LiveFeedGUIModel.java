package com.biometricsystem.livefeed.client.gui;
import com.biometricsystem.entity.image.CapturedFrame;
import com.biometricsystem.entity.image.FaceRecognitionResult;
import com.biometricsystem.livefeed.LiveFeedManager;
import org.opencv.imgcodecs.Imgcodecs;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class LiveFeedGUIModel {

    private LiveFeedGUIController controller;
    private final DateTimeFormatter formatter= DateTimeFormatter.ofPattern("dd-MM-yyyy HH-mm-ss");
    private static final int NUMBER_OF_UNRECOGNIZED_FACES_UNTIL_IDENTIFICATION_BY_ID=3;
    private int numberOfUnrecognizedFaces;

    public void setController(LiveFeedGUIController controller){
        this.controller=controller;
        numberOfUnrecognizedFaces=0;
    }

    public void setCurrentFrame(CapturedFrame frame){
        if (frame.getResult() == FaceRecognitionResult.FACE_RECOGNIZED) {
            System.out.println("CapturedFrame of recognized face in GUI");
            faceRecognized(frame);
        }else if (frame.getResult()==FaceRecognitionResult.FACE_DETECTED_AND_NOT_RECOGNIZED){
            if(++numberOfUnrecognizedFaces==NUMBER_OF_UNRECOGNIZED_FACES_UNTIL_IDENTIFICATION_BY_ID) {
                System.out.println("CapturedFrame of unrecognized face is displayed in GUI");
                numberOfUnrecognizedFaces = 0;
                identifyById(frame);
            }else{
                System.out.println("CapturedFrame of unrecognized face is being skipped in GUI");
            }
        }
    }

    public void faceRecognized(CapturedFrame currentFrame) {
        String frameTitle = "Face recognized as " + currentFrame.getEmployee().getName() + " with " + currentFrame.getRecognitionProbabilityString() + "% probability";
        System.out.println(frameTitle);
        controller.setRecognizedFace(currentFrame.getFaceImageAsByteArray(),frameTitle);
        saveCurrentFrameToFacesRecognizedDirectory(currentFrame);
        controller.openGate();
    }

    public void identifyById(CapturedFrame currentFrame){
        controller.setIdentificationComponents(currentFrame.getFaceImageAsByteArray(),currentFrame.getRecognitionProbabilityString());
        int[] IdentifiedIdAndEmployeeNumber=controller.getIdAndEmployeeNumber();
        if (IdentifiedIdAndEmployeeNumber!=null){
            long id=IdentifiedIdAndEmployeeNumber[0];
            long employeeNumber=IdentifiedIdAndEmployeeNumber[1];
            new IdentifiedById(currentFrame,id,employeeNumber,controller).start();
        }
    }

    private void saveCurrentFrameToFacesRecognizedDirectory(CapturedFrame currentFrame){
        Imgcodecs.imwrite(LiveFeedManager.FACES_RECOGNIZED_DIRECTORY+"\\"+currentFrame.getEmployee().getName()+" "+LocalDateTime.now().format(formatter)+".jpg",currentFrame.getFaceImage());
    }

}
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
            faceRecognized(frame);
        }else if (frame.getResult() == FaceRecognitionResult.FACE_DETECTED_AND_NOT_RECOGNIZED){
            if(++numberOfUnrecognizedFaces==NUMBER_OF_UNRECOGNIZED_FACES_UNTIL_IDENTIFICATION_BY_ID) {
                numberOfUnrecognizedFaces = 0;
                identifyById(frame);
            }
        }
    }

    public void faceRecognized(CapturedFrame currentFrame) {
        String frameTitle = "Face recognized as " + currentFrame.getEmployeeName() + " with " + currentFrame.getRecognitionProbabilityString() + "% probability";
        System.out.println(frameTitle);
        controller.setRecognizedFace(currentFrame.getFaceImageAsByteArray(),frameTitle);
        saveCurrentFrameToFacesRecognizedDirectory(currentFrame);
        controller.openGate();
    }

    public void identifyById(CapturedFrame currentFrame){
        String frameTitle="Face did not pass threshold with "+currentFrame.getRecognitionProbabilityString()+"% probability. Enter ID and employee number.";
        System.out.println(frameTitle);
        controller.setIdentificationComponents(currentFrame.getFaceImageAsByteArray(), frameTitle);
        int[] IdentifiedIdAndEmployeeNumber=controller.getIdAndEmployeeNumber();
        if (IdentifiedIdAndEmployeeNumber!=null){
            long id=IdentifiedIdAndEmployeeNumber[0];
            long employeeNumber=IdentifiedIdAndEmployeeNumber[1];
            new IdentifiedById(id,employeeNumber,controller).start();
        }
    }

    private void saveCurrentFrameToFacesRecognizedDirectory(CapturedFrame currentFrame){
        Imgcodecs.imwrite(LiveFeedManager.FACES_RECOGNIZED_DIRECTORY+"\\"+currentFrame.getEmployeeName()+" "+LocalDateTime.now().format(formatter)+".jpg",currentFrame.getFaceImage());
    }

}

package com.biometricsystem.entity.image;
import java.io.Serializable;


public class CapturedFrameWrapper implements Serializable{

    private byte[] FaceImageAsCompressedByteArray;
    private FaceRecognitionResult result;
    private double recognitionProbability;
    private String employeeName;

    public CapturedFrameWrapper(CapturedFrame frame) {
        FaceImageAsCompressedByteArray = frame.getFaceImageAsCompressedByteArray();
        result = frame.getResult();
        recognitionProbability=frame.getRecognitionProbability();
        if(frame.getEmployee()!=null) {
            employeeName = frame.getEmployee().getName();
        }
    }

    public double getRecognitionProbability() {
        return recognitionProbability;
    }

    public byte[] getFaceImageAsCompressedByteArray() {
        return FaceImageAsCompressedByteArray;
    }

    public FaceRecognitionResult getResult() {
        return result;
    }

    public String getEmployeeName(){
        return employeeName;
    }

}
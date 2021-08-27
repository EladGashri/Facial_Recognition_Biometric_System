package com.biometricsystem.entity.image;
import java.io.Serializable;


public class CapturedFrameWrapper implements Serializable{

    private byte[] FaceImageAsCompressedByteArray;
    private FaceRecognitionResult result;
    private double recognitionProbability;
    private Integer modelClass;

    public CapturedFrameWrapper(CapturedFrame frame) {
        FaceImageAsCompressedByteArray = frame.getFaceImageAsCompressedByteArray();
        result = frame.getResult();
        recognitionProbability=frame.getRecognitionProbability();
        if(frame.getEmployee()!=null) {
            modelClass = frame.getEmployee().getModelClass();
        }else{
            modelClass=null;
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

    public Integer getModelClass() {
        return modelClass;
    }

}
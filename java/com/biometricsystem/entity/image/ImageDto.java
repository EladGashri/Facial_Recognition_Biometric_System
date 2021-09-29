package com.biometricsystem.entity.image;
import com.biometricsystem.entity.employee.Employee;
import org.opencv.core.Rect;


public class ImageDto {

    private Employee employee;
    private FaceRecognitionResult result;
    private String path;
    private boolean faceDetected;
    private Rect faceIndexes;
    private double recognitionProbability;


    public ImageDto(Employee employee, FaceRecognitionResult result, String path, boolean faceDetected, Rect faceIndexes, double recognitionProbability) {
        this.employee = employee;
        this.result = result;
        this.path = path;
        this.faceDetected = faceDetected;
        this.faceIndexes = faceIndexes;
        this.recognitionProbability = recognitionProbability;
    }

    public static ImageDto asImageDTO(ImageForIdentification image){
        return new ImageDto(image.getEmployee(), image.getResult(), image.getPath(), image.wasAFaceDetected(), image.getFaceIndexes(), image.getRecognitionProbability());
    }

    public Employee getEmployee() {
        return employee;
    }

    public FaceRecognitionResult getResult() {
        return result;
    }

    public String getPath() {
        return path;
    }

    public boolean isFaceDetected() {
        return faceDetected;
    }

    public Rect getFaceIndexes() {
        return faceIndexes;
    }

    public double getRecognitionProbability() {
        return recognitionProbability;
    }

}
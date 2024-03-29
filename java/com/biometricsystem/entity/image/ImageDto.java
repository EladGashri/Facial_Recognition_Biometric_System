package com.biometricsystem.entity.image;
import com.biometricsystem.entity.employee.Employee;
import org.opencv.core.Rect;


public class ImageDto {

    private double recognitionProbability;
    private FaceRecognitionResult result;
    private String path;
    private boolean faceDetected;
    private Rect faceIndexes;
    private Employee employee;


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

    public boolean getFaceDetected() {
        return faceDetected;
    }

    public FaceRecognitionResult getResult() {
        return result;
    }

    public double getRecognitionProbability() {
        return recognitionProbability;
    }

    public Employee getEmployee() {
        return employee;
    }

    public Rect getFaceIndexes() {
        return faceIndexes;
    }

}

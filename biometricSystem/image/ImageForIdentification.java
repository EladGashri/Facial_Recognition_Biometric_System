package com.biometricsystem.image;
import ai.djl.modality.Classifications;
import ai.djl.modality.Classifications.Classification;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import com.biometricsystem.classification.FaceClassifier;
import com.biometricsystem.database.Database;
import com.biometricsystem.livefeed.LiveFeed;
import com.mongodb.MongoException;
import org.bson.Document;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.objdetect.CascadeClassifier;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.lang.Math;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.time.LocalTime;


public abstract class ImageForIdentification {

    final Path FACE_DETECTOR_PATH= Paths.get("src\\main\\resources\\detectors\\haarcascade_frontalface_alt.xml");
    private final static CascadeClassifier faceDetector=new CascadeClassifier();
    private final static FaceClassifier faceClassifier=new FaceClassifier(new Database().getNumberOfEmployees());
    private final static CascadeClassifier eyesDetector = new CascadeClassifier();
    private Mat values, faceImage;
    private FaceRecognitionResult result;
    private String path, name;
    private boolean faceDetected;
    private int modelClass;
    private Rect faceIndexes;
    private double recognitionProbability;
    private final DecimalFormat decimalFormat = new DecimalFormat("#.000");
    private final LocalTime timeCaptured;


    public ImageForIdentification(Mat mat){
        values=mat;
        name=null;
        faceDetected=false;
        modelClass =-1;
        recognitionProbability=0;
        decimalFormat.setRoundingMode(RoundingMode.CEILING);
        result=FaceRecognitionResult.FACE_NOT_YET_ANALYZED;
        timeCaptured=LocalTime.now();
    }

    public LocalTime getTimeCaptured(){
        return timeCaptured;
    }

    public void setModelClass(int modelClass){
        this.modelClass = modelClass;
    }

    public void setName(String[] idToName){
        if (modelClass ==-1){
            System.err.println("id must be detected in order to set name");
        }else {
            name = idToName[modelClass];
        }
    }

    public String getRecognitionProbabilityString(){
        return decimalFormat.format(recognitionProbability * 100);
    }

    public Mat getValues(){
        return values;
    }

    public String getPath(){
        return path;
    }

    public Rect getFaceIndexes() {
        return faceIndexes;
    }

    public String getName() {
        return name;
    }

    public void setFaceImage(){
        try {
            faceDetector.load(FACE_DETECTOR_PATH.toString());
            MatOfRect facesDetected = new MatOfRect();
            faceDetector.detectMultiScale(values, facesDetected);
            Rect[] facesArray = facesDetected.toArray();
            if (facesArray.length > 0) {
                faceIndexes = facesArray[0];
                faceImage = new Mat(values, new Rect(faceIndexes.x, faceIndexes.y, faceIndexes.width, faceIndexes.height));
                faceDetected = true;
            } else {
                faceDetected = false;
            }
        }catch (CvException e){
            System.err.println("could not load face detector");
        }
    }

    public void identify() {
        if (!faceDetected) {
            System.err.println("face must be detected in order to perform identification");
            return;
        }
        Image predictionFace=getFaceImageAsImage();
        if (predictionFace!=null) {
            Classifications predictions = faceClassifier.predictPerson(predictionFace);
            if (predictions != null) {
                int i=0;
                double probabilitiesExpSum=0;
                while (true){
                    try {
                        probabilitiesExpSum += Math.exp(predictions.item(i++).getProbability());
                    }catch (IndexOutOfBoundsException e){
                        break;
                    }
                }
                Classification best=predictions.best();
                modelClass = Integer.parseInt(best.getClassName());
                recognitionProbability = getSoftmaxProbability(best.getProbability(),probabilitiesExpSum);
            }
        }
    }

    public void setResult(){
        if (faceDetected){
            if (recognitionProbability>= LiveFeed.FACE_RECOGNITION_THRESHOLD){
                result=FaceRecognitionResult.FACE_RECOGNIZED;
            }else{
                result=FaceRecognitionResult.FACE_DETECTED_AND_NOT_RECOGNIZED;
            }
        }else{
            result=FaceRecognitionResult.FACE_NOT_DETECTED;
        }
    }

    public FaceRecognitionResult getResult(){
        return result;
    }

    private double getSoftmaxProbability(double probability,double probabilitiesExpSum){
        return Math.exp(probability)/probabilitiesExpSum;
    }

    public boolean wasAFaceDetected(){return faceDetected;}

    public Mat getFaceImage(){
        return faceImage;
    }

    public byte[] getFaceImageAsByteArray() {
        MatOfByte matOfByte = new MatOfByte();
        Imgcodecs.imencode(".jpg", faceImage,matOfByte);
        return matOfByte.toArray();
    }

    public Image getFaceImageAsImage() {
        InputStream in = new ByteArrayInputStream(getFaceImageAsByteArray());
        try {
            BufferedImage bufImage = ImageIO.read(in);
            return ImageFactory.getInstance().fromImage(bufImage);
        } catch (IOException e) {
            System.err.println("could not convert Mat format to Image format");
            return null;
        }
    }

    public int getModelClass() {
        return modelClass;
    }

    public double getRecognitionProbability() {
        return recognitionProbability;
    }

    public void saveImageToDatabase(Database database){
        try {
            int numberOfEmployeeImagesInt = (int) database.getImagesCollection().countDocuments(new Document("employee id", modelClass)) + 1;
            String numberOfEmployeeImagesStr;
            if (String.valueOf(numberOfEmployeeImagesInt).length() < 4) {
                numberOfEmployeeImagesStr = "0".repeat(4 - String.valueOf(numberOfEmployeeImagesInt).length()) + numberOfEmployeeImagesInt;
            } else {
                numberOfEmployeeImagesStr = String.valueOf(numberOfEmployeeImagesInt);
            }
            Document pathQuery = (Document) database.getEmployeesCollection().find(new Document("_id", modelClass)).first();
            String employeeImagesPath = (String) pathQuery.get("images directory path");
            path = employeeImagesPath + "//" + name + "_" + numberOfEmployeeImagesStr + ".jpg";
            Imgcodecs.imwrite(path, values);
            int[] arrayFaceIndexes = new int[]{faceIndexes.x, faceIndexes.width, faceIndexes.y, faceIndexes.height};
            database.insertImageDocument(path, modelClass, arrayFaceIndexes, true, recognitionProbability);
            System.out.println(name + "\'s image saved to the database ");
        }catch (MongoException e){
            System.err.println("could not save image to database");
        }
    }

}
package com.biometricsystem.entity.image;
import ai.djl.modality.Classifications;
import ai.djl.modality.Classifications.Classification;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import com.biometricsystem.api.BeanUtil;
import com.biometricsystem.api.service.EmployeeService;
import com.biometricsystem.entity.employee.Employee;
import com.biometricsystem.model.FaceClassifier;
import com.biometricsystem.livefeed.LiveFeedManager;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.objdetect.CascadeClassifier;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.RoundingMode;
import java.lang.Math;
import java.text.DecimalFormat;
import java.time.LocalTime;


public abstract class ImageForIdentification {

    protected final static CascadeClassifier faceDetector;
    protected final static String FACE_DETECTOR_PATH= "src\\main\\resources\\detectors\\haarcascade_frontalface_alt.xml";
    protected Mat values, faceImage;
    protected Employee employee;
    protected FaceRecognitionResult result;
    protected String path;
    protected boolean faceDetected;
    protected Rect faceIndexes;
    protected double recognitionProbability;
    protected final LocalTime timeCaptured;

    static{
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        faceDetector=new CascadeClassifier();
    }

    public ImageForIdentification(Mat mat){
        values=mat;
        employee=null;
        faceDetected=false;
        recognitionProbability=0;
        result=FaceRecognitionResult.FACE_NOT_YET_ANALYZED;
        timeCaptured=LocalTime.now();
    }

    public Rect getFaceIndexes(){
        return faceIndexes;
    }

    public void initializeFaceImage(){
        try {
            faceDetector.load(FACE_DETECTOR_PATH);
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
        FaceClassifier faceClassifier = BeanUtil.getBean(FaceClassifier.class);
        if (!faceDetected) {
            System.out.println("no face detected in image");
            recognitionProbability=0;
            result=FaceRecognitionResult.FACE_NOT_DETECTED;
            employee=null;
        }else{
            Image predictionFace = getFaceImageAsImage();
            if (predictionFace != null) {
                Classifications predictions = faceClassifier.predictPerson(predictionFace);
                if (predictions != null) {
                    setRecognitionProbability(predictions);
                }
            }
        }
    }

    protected void setRecognitionProbability(Classifications predictions){
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
        recognitionProbability=Math.exp(best.getProbability())/probabilitiesExpSum;
        int modelClass = Integer.parseInt(best.getClassName());
        setResult(modelClass);
    }

    protected void setResult(int modelClass){
        if (faceDetected){
            if (recognitionProbability>=LiveFeedManager.FACE_RECOGNITION_THRESHOLD){
                result=FaceRecognitionResult.FACE_RECOGNIZED;
                setEmployee(modelClass);
            }else{
                result=FaceRecognitionResult.FACE_DETECTED_AND_NOT_RECOGNIZED;
            }
        }else{
            result=FaceRecognitionResult.FACE_NOT_DETECTED;
        }
    }

    protected void setEmployee(int modelClass){
        EmployeeService employeeService = BeanUtil.getBean(EmployeeService.class);
        employee=employeeService.getEmployee("model class",modelClass);
    }

    public Employee getEmployee(){
        return employee;
    }

    public String getRecognitionProbabilityString(){
        DecimalFormat decimalFormat = new DecimalFormat("#.000");
        decimalFormat.setRoundingMode(RoundingMode.CEILING);
        return decimalFormat.format(recognitionProbability * 100);
    }

    public String getPath(){
        return path;
    }

    public void setEmployee(Employee employee){
        this.employee=employee;
    }

    public void setEmployeeImages(ImageFromDatabase[] images){
        employee.setImages(images);
    }

    public FaceRecognitionResult getResult(){
        return result;
    }

    public boolean wasAFaceDetected(){return faceDetected;}

    public Mat getFaceImage(){
        return faceImage;
    }

    public byte[] getFaceImageAsByteArray() {
        MatOfByte matOfByte = new MatOfByte();
        Imgcodecs.imencode(".jpg",faceImage,matOfByte);
        return matOfByte.toArray();
    }

    public Image getFaceImageAsImage() {
        InputStream stream = new ByteArrayInputStream(getFaceImageAsByteArray());
        try {
            BufferedImage bufImage = ImageIO.read(stream);
            return ImageFactory.getInstance().fromImage(bufImage);
        } catch (IOException e) {
            System.err.println("could not convert Mat format to Image format");
            return null;
        }
    }

    public Mat getValues(){
        return values;
    }

    public double getRecognitionProbability() {
        return recognitionProbability;
    }

    public void setPath(String path){
        this.path=path;
    }

}
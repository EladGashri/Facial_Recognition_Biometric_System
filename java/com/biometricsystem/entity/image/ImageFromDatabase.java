package com.biometricsystem.entity.image;
import com.biometricsystem.database.Time;
import org.bson.Document;
import org.opencv.core.Rect;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.LocalDate;
import java.util.ArrayList;


@Entity
public class ImageFromDatabase {

    @Id
    private String id;
    private Rect faceIndexes;
    private FaceRecognitionResult result;
    private LocalDate uploadedDate;
    private Time uploadedTime;

    public ImageFromDatabase(Document document){
        id=document.getString("_id");
        if(document.getBoolean("recognized by model")){
            result= FaceRecognitionResult.FACE_RECOGNIZED;
        }else{
            result=FaceRecognitionResult.FACE_DETECTED_AND_NOT_RECOGNIZED;
        }
        ArrayList<Double> indexes=(ArrayList<Double>) document.get("face indexes");
        if (indexes!=null) {
            faceIndexes = new Rect(indexes.get(0).intValue(), indexes.get(2).intValue(), indexes.get(1).intValue(), indexes.get(3).intValue());
        }
        Document uploadedDocument=(Document) document.get("uploaded");
        Document dateDocument=(Document)uploadedDocument.get("date");
        Document timeDocument=(Document)uploadedDocument.get("time");
        uploadedDate=LocalDate.of(dateDocument.getInteger("year"),dateDocument.getInteger("month"),dateDocument.getInteger("day"));
        uploadedTime=new Time(timeDocument.getInteger("hour"),timeDocument.getInteger("minute"),timeDocument.getInteger("second"));
    }

    public String getId() {
        return id;
    }

    public LocalDate getUploadedDate() {
        return uploadedDate;
    }

    public Time getUploadedTime() {
        return uploadedTime;
    }

    public Rect getFaceIndexes() {
        return faceIndexes;
    }

    public FaceRecognitionResult getResult() {
        return result;
    }

}
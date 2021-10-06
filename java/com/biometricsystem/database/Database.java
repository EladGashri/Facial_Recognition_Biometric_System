package com.biometricsystem.database;
import com.biometricsystem.entity.employee.Employee;
import com.biometricsystem.entity.image.FaceRecognitionResult;
import com.biometricsystem.entity.image.ImageForIdentification;
import com.mongodb.MongoException;
import com.mongodb.client.*;
import org.bson.Document;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.stereotype.Component;


@Component
public class Database {

    private MongoCollection<Document> attendanceCollection;
    private MongoCollection<Document> employeesCollection;
    private MongoCollection<Document> imagesCollection;

    public Database(){
        try {
            Logger.getLogger("org.mongodb.driver").setLevel(Level.OFF);
            MongoClient client = MongoClients.create("mongodb://localhost:27017/");
            MongoDatabase database = client.getDatabase("biometric_system");
            attendanceCollection = database.getCollection("attendances");
            employeesCollection = database.getCollection("employees");
            imagesCollection = database.getCollection("images");
        }catch (MongoException e){
            e.printStackTrace();
            System.err.println("Could not connect to the database");
        }
    }

    public MongoCollection<Document> getAttendanceCollection(){
        return attendanceCollection;
    }

    public MongoCollection<Document> getEmployeesCollection(){
        return employeesCollection;
    }

    public MongoCollection<Document> getImagesCollection(){
        return imagesCollection;
    }

    public boolean insertImageDocument(ImageForIdentification image) {
        String imageId = image.getPath().replace("/","_");
        Document dateDocument = new Document("year", LocalDate.now().getYear()).append("month", LocalDate.now().getMonthValue()).
            append("day", LocalDate.now().getDayOfMonth());
        Document timeDocument = new Document("hour", LocalDateTime.now().getHour()).append("minute", LocalDateTime.now().getMinute()).
            append("second", LocalDateTime.now().getSecond());
        Document uploadedDocument = new Document("date", dateDocument).append("time", timeDocument);
        /*Document faceIndexesDocument=new Document("0",image.getFaceIndexes().x).append("1",image.getFaceIndexes().width).
            append("2",image.getFaceIndexes().y).append("3",image.getFaceIndexes().height);*/
        Document faceIndexesDocument=null;
        Document imageDocument = new Document("_id",imageId).append("employee id",image.getEmployee().getId()).
            append("face indexes", faceIndexesDocument).append("recognized by model",image.getResult()==FaceRecognitionResult.FACE_RECOGNIZED).append("uploaded", uploadedDocument);
        try {
            imagesCollection.insertOne(imageDocument);
            return true;
        }catch (MongoException e){
            e.printStackTrace();
            System.err.println("could not insert document to database");
            return false;
        }
    }

    public boolean insertAttendanceDocument(Employee employee,LocalDate date,Time entryTime,Time exitTime) throws Time.TimeException {
        Document entryDocument=null;
        Document exitDocument=null;
        Document totalDocument=null;
        if (entryTime!=null && exitTime!=null) {
            entryDocument=entryTime.castToDocument();
            TotalTime total = entryTime.getTotalTimeDifference(exitTime);
            totalDocument=total.castToDocument();
            exitDocument=exitTime.castToDocument();
        }else if (entryTime==null && exitTime!=null) {
            exitDocument=exitTime.castToDocument();
        }else if (exitTime==null && entryTime!=null) {
            entryDocument=entryTime.castToDocument();
        }
        Document dateDocument=new Document("year",date.getYear()).append("month",date.getMonthValue()).append("day",date.getDayOfMonth());
        Document attendanceDocument = new Document("_id", employee.getId()+"-"+date.getYear()+"-"+date.getMonthValue()+"-"+date.getDayOfMonth()).append("employee id", employee.getId())
            .append("date", dateDocument).append("entry", entryDocument).append("exit", exitDocument).append("total", totalDocument);
        try {
            attendanceCollection.insertOne(attendanceDocument);
            return true;
        }catch (MongoException e){
            e.printStackTrace();
            System.err.println("could not insert document to database");
            return false;
        }
    }

    public boolean insertEmployeeDocument(Employee employee){
        Document employeeDocument = new Document("_id", employee.getId()).append("employee number", employee.getEmployeeNumber()).
            append("name", employee.getName()).append("images directory path", employee.getImagesDirectoryPath()).
            append("branch", employee.getBranch().getDatabaseValue()).append("employee type", employee.getEmployeeType().getDatabaseValue()).
            append("number of images",employee.getNumberOfImages()).append("model class",employee.getModelClass()).append("included in model",employee.isIncludedInModel());
        try {
            employeesCollection.insertOne(employeeDocument);
            return true;
        }catch (MongoException e){
            e.printStackTrace();
            System.err.println("could not insert document to database");
            return false;
        }
    }

    /*public int[] calculateTotal(int[] entryTime, int[] exitTime) {
        int toHours=(exitTime[1] < entryTime[1])? 1 : 0;
        int hours=exitTime[0] - entryTime[0] - toHours;

        int toMinutes=(exitTime[2] < entryTime[2])? 1 : 0;
        int minutes=exitTime[1] - entryTime[1] - toMinutes;
        if (minutes<0){
            minutes+=60;
        }

        int seconds=exitTime[2] - entryTime[2];
        if (seconds<0){
            seconds+=60;
        }

        int[] total= {hours, minutes, seconds};
        return total;
    }*/

}
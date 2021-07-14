package com.biometricsystem.database;
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

    private MongoClient client;
    private MongoDatabase database;
    private MongoCollection attendanceCollection;
    private MongoCollection employeesCollection;
    private MongoCollection imagesCollection;

    public Database() {
        try {
            Logger mongoLogger = Logger.getLogger("org.mongodb.driver");
            mongoLogger.setLevel(Level.SEVERE);
            client = MongoClients.create("mongodb://localhost:27017/");
            database = client.getDatabase("biometric_system");
            attendanceCollection = database.getCollection("attendance");
            employeesCollection = database.getCollection("employees");
            imagesCollection = database.getCollection("images");
        }catch (MongoException e){
            System.err.println("could not connect to database");
        }
    }

    public MongoCollection getAttendanceCollection(){
        return attendanceCollection;
    }

    public MongoCollection getEmployeesCollection(){
        return employeesCollection;
    }

    public MongoCollection getImagesCollection(){
        return imagesCollection;
    }


    public void insertImageDocument(String path, int employeeId, int[] faceIndexes, boolean recognized, double accuracy) {
        String[] pathSplit = path.split("\\\\");
        Document dateDocument = new Document("year", LocalDate.now().getYear()).append("months", LocalDate.now().getMonthValue())
                .append("day", LocalDate.now().getDayOfMonth());
        Document timeDocument = new Document("hour", LocalDateTime.now().getHour()).append("minute", LocalDateTime.now().getMinute())
                .append("second", LocalDateTime.now().getSecond());
        Document uploadedDocument = new Document("date", dateDocument).append("time", timeDocument);
        Document faceIndexesDocument=new Document("0",faceIndexes[0]).append("1",faceIndexes[1]).append("2",faceIndexes[2]).append("3",faceIndexes[3]);
        Document imageDocument = new Document("_id", pathSplit[pathSplit.length - 1]).append("employee id", employeeId)
                .append("face indexes", faceIndexesDocument).append("recognized", recognized).append("accuracy", accuracy).append("uploaded", uploadedDocument);
        try {
            imagesCollection.insertOne(imageDocument);
        }catch (MongoException e){
            System.err.println("could not insert document to database");
        }
    }

    public void insertAttendanceDocument(int employeeId, LocalDate date, Time entryTime, Time exitTime) throws Time.TimeException {
        Document totalDocument;
        Document exitDocument;
        if (exitTime!=null) {
            Time total = entryTime.getTimeDifference(exitTime);
            totalDocument=total.getDocument(true);
            exitDocument=exitTime.getDocument(false);
        }else {
            totalDocument = null;
            exitDocument=null;
        }
        Document entryDocument=entryTime.getDocument(false);
        Document dateDocument=new Document("year",date.getYear()).append("month",date.getMonthValue()).append("day",date.getDayOfMonth());
        Document attendanceDocument = new Document("_id", employeeId+"-"+date.getYear()+"-"+date.getMonthValue()+"-"+date.getDayOfMonth()).append("employee id", employeeId)
                .append("date", dateDocument).append("entry", entryDocument).append("exit", exitDocument).append("total", totalDocument);
        try {
            attendanceCollection.insertOne(attendanceDocument);
        }catch (MongoException e){
            System.err.println("could not insert document to database");
        }
    }

    public void insertEmployeeDocument(int employeeId, int employeeNumber, String name, String imagesDirectoryPath, char branch, boolean admin){
        Document employeeDocument = new Document("_id", employeeId).append("employee number", employeeNumber)
                .append("name", name).append("images directory path", imagesDirectoryPath).append("branch", branch).append("admin", admin);
        try {
            employeesCollection.insertOne(employeeDocument);
        }catch (MongoException e){
            System.err.println("could not insert document to database");
        }
    }

    public int getNumberOfEmployees(){
        try {
            return (int) employeesCollection.countDocuments();
        }catch (MongoException e){
            System.err.println("could not find the number of employees. returned 0");
            return 0;
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
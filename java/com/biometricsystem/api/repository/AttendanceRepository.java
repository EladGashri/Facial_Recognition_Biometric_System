package com.biometricsystem.api.repository;
import com.biometricsystem.database.Database;
import com.biometricsystem.database.Time;
import com.biometricsystem.database.TotalTime;
import com.biometricsystem.entity.attendance.Attendance;
import com.biometricsystem.entity.employee.Employee;
import com.mongodb.client.MongoCursor;
import org.bson.Document;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.NoSuchElementException;
import static com.mongodb.client.model.Projections.include;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;


@Repository
public class AttendanceRepository {

    @Autowired
    private Database database;
    public final static boolean OVERRIDE_ATTENDANCE=false;


    public Attendance getAttendanceFromDatabase(long employeeId, LocalDate date){
        try{
            Iterator<Document> iterator=database.getAttendanceCollection().find(new Document("date", getDateDocument(date)).
                    append("employee id",employeeId)).iterator();
            return new Attendance(iterator.next());
        }catch (NoSuchElementException e){
            return null;
        }
    }

    public boolean needToRegisterEntryToday(Employee employee){
        return OVERRIDE_ATTENDANCE||getEmployeeEntry(employee,LocalDate.now())!=null;
    }

    public boolean registerEntryNow(Employee employee) throws Time.TimeException {
        LocalDate date=LocalDate.now();
        Time time=new Time(LocalDateTime.now().getHour(),LocalDateTime.now().getMinute(),LocalDateTime.now().getSecond());
        return registerEntryToDatabase(employee,date,time);
    }


    public boolean registerExitNow(Employee employee) throws Time.TimeException {
        LocalDate date=LocalDate.now();
        Time time=new Time(LocalDateTime.now().getHour(),LocalDateTime.now().getMinute(),LocalDateTime.now().getSecond());
        return registerExitToDatabase(employee,date,time);
    }


    public boolean checkAndRegisterEntry(Employee employee) throws Time.TimeException {
        if (needToRegisterEntryToday(employee)) {
            return registerExitNow(employee);
        } else {
            System.out.println(employee.getName() + " already registered entry today");
            return false;
        }
    }


    public Document getEmployeeEntry(Employee employee, LocalDate date){
        try {
            Document dateDocument = getDateDocument(date);
            Document employeesEntryFind = new Document("employee id",employee.getId()).append("date", dateDocument).append("entry", new Document("$ne", null));
            MongoCursor attendanceQuery = database.getAttendanceCollection().find(employeesEntryFind).projection(include("entry")).iterator();
            Document document=(Document) attendanceQuery.next();
            return (Document) document.get("entry");
        }catch (NoSuchElementException e){
            return null;
        }
    }

    public Document getEmployeeExit(Employee employee, LocalDate date){
        try {
            Document dateDocument = getDateDocument(date);
            Document employeesExitFind = new Document("employee id",employee.getId()).append("date", dateDocument).append("exit", new Document("$ne", null));
            MongoCursor attendanceQuery = database.getAttendanceCollection().find(employeesExitFind).projection(include("exit")).iterator();
            Document document=(Document) attendanceQuery.next();
            return (Document) document.get("exit");
        }catch (NoSuchElementException e){
            return null;
        }
    }

    public boolean registerEntryToDatabase(Employee employee,LocalDate date,Time entryTime) throws Time.TimeException {
        Document dateDocument = getDateDocument(date);
        Document entryDocument= new Document("entry", entryTime.castToDocument());
        Document employeesEntryFind=new Document("employee id",employee.getId()).append("date",dateDocument);
        Document attendanceQuery=database.getAttendanceCollection().find(employeesEntryFind).first();
        if(attendanceQuery!=null) {
            if (OVERRIDE_ATTENDANCE) {
                Document exitDocument = (Document) attendanceQuery.get("exit");
                if (exitDocument!=null) {
                    Time exitTime = new Time(exitDocument.getInteger("hour"),exitDocument.getInteger("minute"),exitDocument.getInteger("second"));
                    if (!entryTime.checkIfEntryBeforeExit(exitTime)) {
                        throw new Time.TimeException("entry time must be before exit time");
                    }
                    TotalTime totalTime = entryTime.getTotalTimeDifference(exitTime);
                    Document updateOperation = new Document("$set", entryDocument.append("total",totalTime.castToDocument()));
                    database.getAttendanceCollection().updateOne(attendanceQuery, updateOperation);
                    System.out.println("database entry and total updated for " + employee.getName());
                }else{
                    Document updateOperation = new Document("$set", entryDocument);
                    database.getAttendanceCollection().updateOne(attendanceQuery, updateOperation);
                    System.out.println("database entry updated for " + employee.getName());
                }
                return true;
            } else {
                System.out.println(employee.getName() + " already registered entry today. Must set OVERRIDE_ATTENDANCE to true in order to update entry");
                return false;
            }
        }else{
            database.insertAttendanceDocument(employee, date, entryTime, null);
            System.out.println("database entry registered for " + employee.getName());
            return true;
        }
    }

    public boolean registerExitToDatabase(Employee employee,LocalDate date,Time exitTime) throws Time.TimeException{
        Document dateDocument = getDateDocument(date);
        Document employeesEntryFind=new Document("employee id",employee.getId()).append("date",dateDocument).append("entry", new Document("$ne",null));
        Document entryQuery= database.getAttendanceCollection().find(employeesEntryFind).first();
        if(entryQuery==null) {
            throw new Time.TimeException(employee.getName() + " didn't register entry today and therefore cannot register exit");
        }else{
            Document findAlreadyRegisteredExit=new Document("employee id",employee.getId()).append("date",dateDocument).append("exit",new Document("$ne",null));
            Document exitQuery=database.getAttendanceCollection().find(findAlreadyRegisteredExit).first();
            if (!OVERRIDE_ATTENDANCE && exitQuery!=null) {
                System.out.println(employee.getName() + " already registered exit today. Must set OVERRIDE_ATTENDANCE to true in order to update exit");
                return false;
            }else{
                Document entryDocument=(Document) entryQuery.get("entry");
                Time entryTime=new Time(entryDocument.getInteger("hour"),entryDocument.getInteger("minute"),entryDocument.getInteger("second"));
                if (!entryTime.checkIfEntryBeforeExit(exitTime)){
                    throw new Time.TimeException("entry time must be before exit time");
                }
                Time totalTime=entryTime.getTotalTimeDifference(exitTime);
                Document updateOperation= new Document("$set",new Document("exit",exitTime.castToDocument()).append("total",totalTime.castToDocument()));
                database.getAttendanceCollection().updateOne(entryQuery,updateOperation);
                if (exitQuery!=null){
                    System.out.println("database exit and total updated for " + employee.getName());
                }else{
                    System.out.println("database exit and total registered for " + employee.getName());
                }
                return true;
            }
        }
    }

    public static Document getDateDocument(LocalDate date){
        return new Document("year", date.getYear()).append("month", date.getMonthValue()).append("day", date.getDayOfMonth());
    }

}
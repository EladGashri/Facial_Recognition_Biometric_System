package com.biometricsystem.entity.attendance;
import com.biometricsystem.database.Time;
import com.biometricsystem.database.TotalTime;
import org.bson.Document;

import java.time.DateTimeException;
import java.time.LocalDate;
import javax.persistence.Entity;
import javax.persistence.Id;


@Entity
public class Attendance {

    @Id
    private String id;
    private long employeeId;
    private LocalDate date;
    private Time entry;
    private Time exit;
    private TotalTime total;

    public Attendance(Document document) throws AttendanceException{
        id=document.getString("_id");
        try {
            employeeId = document.getInteger("employee id");
        }catch(java.lang.ClassCastException e){
            employeeId = document.getLong("employee id");
        }
        Document dateDocument=(Document) document.get("date");
        date=LocalDate.of(dateDocument.getInteger("year"),dateDocument.getInteger("month"),dateDocument.getInteger("day"));
        Document entryDocument=(Document) document.get("entry");
        if (entryDocument!=null ) {
            entry = new Time(entryDocument.getInteger("hour"), entryDocument.getInteger("minute"), entryDocument.getInteger("second"));
        }else {
            throw new AttendanceException("not entry in document");
        }
        Document exitDocument=(Document) document.get("exit");
        if (exitDocument!=null){
            exit = new Time(exitDocument.getInteger("hour"), exitDocument.getInteger("minute"), exitDocument.getInteger("second"));
        }else{
            throw new AttendanceException("not exit in document");
        }
        Document totalDocument=(Document) document.get("total");
        total=new TotalTime(totalDocument.getInteger("hours"),totalDocument.getInteger("minutes"),totalDocument.getInteger("seconds"));
    }

    public LocalDate getDate() {
        return date;
    }

    public Time getEntry() {
        return entry;
    }

    public Time getExit() {
        return exit;
    }

    public TotalTime getTotal() {
        return total;
    }

    public class AttendanceException extends DateTimeException {

        public AttendanceException(String message){
            super(message);
        }
    }

}
package com.biometricsystem.entity.attendance;
import com.biometricsystem.database.Time;
import com.biometricsystem.database.TotalTime;
import org.bson.Document;
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

    public Attendance(Document document) throws NullPointerException{
        id=document.getString("_id");
        try {
            employeeId = document.getInteger("employee id");
        }catch(java.lang.ClassCastException e){
            employeeId = document.getLong("employee id");
        }
        Document dateDocument=(Document) document.get("date");
        date=LocalDate.of(dateDocument.getInteger("year"),dateDocument.getInteger("month"),dateDocument.getInteger("day"));
        Document entryDocument=(Document) document.get("entry");
        entry=new Time(entryDocument.getInteger("hour"),entryDocument.getInteger("minute"),entryDocument.getInteger("second"));
        Document exitDocument=(Document) document.get("exit");
        exit=new Time(exitDocument.getInteger("hour"),exitDocument.getInteger("minute"),exitDocument.getInteger("second"));
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

}
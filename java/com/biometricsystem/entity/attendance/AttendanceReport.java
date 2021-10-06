package com.biometricsystem.entity.attendance;
import com.biometricsystem.database.TotalTime;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.ArrayList;


public class AttendanceReport {

    private long employeeNumber;
    private String employeeName;
    private LocalDate startDate;
    private LocalDate endDate;
    private int numberOfDaysWithAttendance;
    private ArrayList<Attendance> attendances;
    private TotalTime totalTime;

    public AttendanceReport(long employeeNumber, String employeeName, LocalDate startDate, LocalDate endDate) throws AttendanceReportException {
        if (endDate.isBefore(startDate)){
            throw new AttendanceReportException();
        }
        this.employeeNumber = employeeNumber;
        this.employeeName=employeeName;
        this.startDate = startDate;
        this.endDate = endDate;
        attendances = new ArrayList<>();
        numberOfDaysWithAttendance=0;
        totalTime=new TotalTime(0,0,0);
    }

    public long getEmployeeNumber(){
        return employeeNumber;
    }

    public String getEmployeeName(){
        return employeeName;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public int getNumberOfDaysWithAttendance(){
        return numberOfDaysWithAttendance;
    }

    public ArrayList<Attendance> getAttendances() {
        return attendances;
    }

    public TotalTime getTotalTime() {
        return totalTime;
    }

    public void addAttendance(Attendance attendance){
        attendances.add(attendance);
        numberOfDaysWithAttendance+=1;
        totalTime.add(attendance.getTotal());
    }

    public class AttendanceReportException extends DateTimeException{

        public AttendanceReportException(){
            super("End date cannot be after start date");
        }
    }

}
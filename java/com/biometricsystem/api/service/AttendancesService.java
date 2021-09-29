package com.biometricsystem.api.service;
import com.biometricsystem.api.repository.AttendanceRepository;
import com.biometricsystem.database.Time;
import com.biometricsystem.entity.attendance.Attendance;
import com.biometricsystem.entity.attendance.AttendanceReport;
import com.biometricsystem.entity.attendance.DateRange;
import com.biometricsystem.entity.employee.Employee;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.HashMap;


@Service
public class AttendancesService {

    @Autowired
    private AttendanceRepository attendancesRepository;
    @Autowired
    private EmployeeService employeeService;

    public AttendanceReport getAttendanceReport(long employeeNumber, LocalDate startDate, LocalDate endDate) throws AttendanceReport.AttendanceReportException, NullPointerException {
        long employeeId=employeeService.getId(employeeNumber);
        AttendanceReport attendanceReport = new AttendanceReport(employeeNumber, employeeService.getEmployeeName(employeeId), startDate, endDate);
        Attendance attendance;
        for (LocalDate date : new DateRange(startDate, endDate)) {
            attendance = attendancesRepository.getAttendanceFromDatabase(employeeId, date);
            if (attendance != null) {
                attendanceReport.addAttendance(attendance);
            }
        }
        return attendanceReport;
    }

    public boolean registerEntry(HashMap<String, String> requestBody) throws com.biometricsystem.database.Time.TimeException {
        return attendancesRepository.registerEntryToDatabase(getEmployeeFromRequestBody(requestBody),
                getDateFromRequestBody(requestBody),getTimeFromRequestBody(requestBody));
    }

    public boolean registerExit(HashMap<String, String> requestBody) throws com.biometricsystem.database.Time.TimeException {
        return attendancesRepository.registerExitToDatabase(getEmployeeFromRequestBody(requestBody),
                getDateFromRequestBody(requestBody),getTimeFromRequestBody(requestBody));
    }

    private Employee getEmployeeFromRequestBody(HashMap<String, String> requestBody){
        long employeeNumber=Long.parseLong(requestBody.get("employee-number"));
        long employeeId=employeeService.getId(employeeNumber);
        return employeeService.getEmployee(employeeId,employeeNumber);
    }

    private LocalDate getDateFromRequestBody(HashMap<String, String> requestBody){
        String stringDate=requestBody.get("date");
        return LocalDate.parse(stringDate);
    }

    private Time getTimeFromRequestBody(HashMap<String, String> requestBody){
        String stringTime=requestBody.get("time");
        return Time.parse(stringTime);
    }

}
package com.biometricsystem.api.controller;
import com.biometricsystem.api.service.AttendancesService;
import com.biometricsystem.database.Time;
import com.biometricsystem.entity.attendance.AttendanceReport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.HashMap;


@RequestMapping("/attendances")
@RestController
public class AttendancesController {

    @Autowired
    private AttendancesService attendancesService;

    @GetMapping
    public ResponseEntity<AttendanceReport> getAttendanceReport(@RequestParam("employee-number") Long employeeNumber, @RequestParam("start-date")
    @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate, @RequestParam("end-date") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate){
        try{
            return ResponseEntity.ok(attendancesService.getAttendanceReport(employeeNumber,startDate,endDate));
        }catch (AttendanceReport.AttendanceReportException|NullPointerException e){
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/entry")
    public ResponseEntity<String> addEntry(@RequestBody HashMap<String,String> requestBody){
        try{
            if(attendancesService.registerEntry(requestBody)) {
                return ResponseEntity.status(HttpStatus.CREATED).build();
            }else{
                return ResponseEntity.ok().build();
            }
        }catch (Time.TimeException e){
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/exit")
    public ResponseEntity<String> addExit(@RequestBody HashMap<String,String> requestBody){
        try{
            if(attendancesService.registerExit(requestBody)) {
                return ResponseEntity.status(HttpStatus.CREATED).build();
            }else{
                return ResponseEntity.ok().build();
            }
        }catch (Time.TimeException e){
            return ResponseEntity.badRequest().build();
        }
    }

}

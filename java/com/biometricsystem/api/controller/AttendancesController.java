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


@RestController
@RequestMapping("/attendances")
public class AttendancesController {

    @Autowired
    private AttendancesService attendancesService;

    @GetMapping
    public ResponseEntity<?> getAttendanceReport(@RequestParam("employee-number") Long employeeNumber, @RequestParam("start-date")
    @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate, @RequestParam("end-date") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate){
        try{
            return new ResponseEntity<>(attendancesService.getAttendanceReport(employeeNumber,startDate,endDate), HttpStatus.OK);
        }catch (AttendanceReport.AttendanceReportException e){
            return new ResponseEntity<>("Start date cannot be after end date", HttpStatus.BAD_REQUEST);
        }catch (NullPointerException e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/entry")
    public ResponseEntity<String> addEntry(@RequestBody HashMap<String,String> requestBody){
        try{
            if(attendancesService.registerEntry(requestBody)) {
                return new ResponseEntity<>("Entry added", HttpStatus.CREATED);
            }else{
                return new ResponseEntity<>("Employee already has registered entry for " +requestBody.get("date")+". Must set OVERWRITE_ATTENDANCE to true in order to update entry.", HttpStatus.OK);
            }
        }catch (Time.TimeException e){
            return new ResponseEntity<>("Entry is after registered exit", HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/entry-now")
    public ResponseEntity<String> addEntryNow(@RequestParam("employee-number") Long employeeNumber){
        try{
            if(attendancesService.checkAndRegisterEntry(employeeNumber)) {
                return new ResponseEntity<>("Entry added", HttpStatus.CREATED);
            }else{
                return new ResponseEntity<>("Employee already has registered entry for " +LocalDate.now()+". Must set OVERWRITE_ATTENDANCE to true in order to update entry.", HttpStatus.OK);
            }
        }catch (Time.TimeException e){
            return new ResponseEntity<>("Entry is after registered exit", HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/exit")
    public ResponseEntity<String> addExit(@RequestBody HashMap<String,String> requestBody){
        try{
            if(attendancesService.registerExit(requestBody)) {
                return new ResponseEntity<>("Exit added", HttpStatus.CREATED);
            }else{
                return new ResponseEntity<>("Employee already registered exit for " +requestBody.get("date")+". Must set OVERWRITE_ATTENDANCE to true in order to update exit.", HttpStatus.BAD_REQUEST);
            }
        }catch (Time.TimeException e){
            return new ResponseEntity<>("Exit is before registered entry", HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/exit-now")
    public ResponseEntity<String> addExitNow(@RequestParam("employee-number") Long employeeNumber){
        try{
            if(attendancesService.registerExitNow(employeeNumber)) {
                return new ResponseEntity<>("Exit added", HttpStatus.CREATED);
            }else{
                return new ResponseEntity<>("Employee already has registered exit for " +LocalDate.now()+". Must set OVERWRITE_ATTENDANCE to true in order to update entry.", HttpStatus.OK);
            }
        }catch (Time.TimeException e){
            return new ResponseEntity<>("Exit is before registered entry", HttpStatus.BAD_REQUEST);
        }
    }

}

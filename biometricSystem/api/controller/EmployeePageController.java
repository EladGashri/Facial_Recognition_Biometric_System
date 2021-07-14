package com.biometricsystem.api.controller;
import com.biometricsystem.api.employee.Employee;
import com.biometricsystem.api.service.SingleEmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import javax.websocket.server.PathParam;


@RestController
public class EmployeePageController{

    @Autowired
    private SingleEmployeeService singleEmployeeService;

    @GetMapping("/employeepage")
    public ResponseEntity<Employee> getEmployeePage(@PathParam("employeeNumber") long employeeNumber){
        Employee employee= singleEmployeeService.getEmployeeFromDatabaseByEmployeeNumber(employeeNumber);
        if (employee!=null){
            return ResponseEntity.ok(employee);
        }else{
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/personalpage")
    public ResponseEntity<Employee> getCurrentEmployeePage(HttpServletRequest request){
        long employeeNumber=singleEmployeeService.getEmployeeNumberFromRequest(request);
        return getEmployeePage(employeeNumber);
    }

}
package com.biometricsystem.api.controller;
import com.biometricsystem.api.employee.Employee;
import com.biometricsystem.api.service.SingleEmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletRequest;


@RestController
@RequestMapping("/")
public class MainController {

    @Autowired
    private SingleEmployeeService singleEmployeeService;

    @GetMapping
    public ResponseEntity mainGet(HttpServletRequest request){
        Employee employee=singleEmployeeService.getEmployeeFromRequest(request);
        return ResponseEntity.ok("Hello "+employee.getName());
    }

}
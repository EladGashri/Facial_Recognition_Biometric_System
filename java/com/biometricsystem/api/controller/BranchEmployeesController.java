package com.biometricsystem.api.controller;
import com.biometricsystem.api.service.BranchEmployeesService;
import com.biometricsystem.entity.employee.EmployeeDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;


@RestController
@RequestMapping("/branch-employees")
public class BranchEmployeesController {

    @Autowired
    private BranchEmployeesService branchEmployeesService;

    @GetMapping
    public ResponseEntity<?> getEmployeesList(HttpServletRequest request) {
        List<EmployeeDto> employeeList=branchEmployeesService.getBranchEmployees(request);
        if(employeeList!=null){
            return new ResponseEntity<>(employeeList, HttpStatus.OK);
        }else{
            return new ResponseEntity<>("No employees found in branch", HttpStatus.NOT_FOUND);
        }
    }

}
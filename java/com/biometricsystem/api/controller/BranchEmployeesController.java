package com.biometricsystem.api.controller;
import com.biometricsystem.api.service.BranchEmployeesService;
import com.biometricsystem.entity.employee.EmployeeDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;


@RestController
@RequestMapping("/branch-employees")
public class BranchEmployeesController {

    @Autowired
    private BranchEmployeesService branchEmployeesService;

    @GetMapping
    public ResponseEntity<ArrayList<EmployeeDto>> getEmployeesList(HttpServletRequest request) {
        ArrayList<EmployeeDto> employeeList=branchEmployeesService.getBranchEmployees(request);
        if(employeeList!=null){
            return ResponseEntity.ok(employeeList);
        }else{
            return ResponseEntity.badRequest().build();
        }
    }

}
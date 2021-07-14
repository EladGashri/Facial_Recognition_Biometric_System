package com.biometricsystem.api.controller;
import com.biometricsystem.api.service.BranchService;
import com.biometricsystem.api.service.SingleEmployeeService;
import com.biometricsystem.api.employee.Employee;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;


@RestController
@RequestMapping("/employeeslist")
public class EmployeesListController {

    @Autowired
    private BranchService branchService;
    @Autowired
    private SingleEmployeeService singleEmployeeService;

    @GetMapping
    public ResponseEntity<ArrayList<Employee>> getEmployeesList(HttpServletRequest request) {
        Employee employee=singleEmployeeService.getEmployeeFromRequest(request);
        ArrayList<Employee> employeeList=branchService.getBranchEmployees(employee.getBranch());
        return ResponseEntity.ok(employeeList);
    }

}
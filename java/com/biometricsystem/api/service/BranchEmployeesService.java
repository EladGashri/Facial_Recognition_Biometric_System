package com.biometricsystem.api.service;
import com.biometricsystem.api.repository.BranchEmployeesRepository;
import com.biometricsystem.entity.employee.Employee;
import com.biometricsystem.entity.employee.EmployeeDto;
import org.bson.Document;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Iterator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class BranchEmployeesService {

    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private BranchEmployeesRepository branchEmployeesRepository;


    public ArrayList<EmployeeDto> getBranchEmployees(HttpServletRequest request) {
        Employee employee=employeeService.getEmployeeFromRequest(request);
        if (employee==null){
            return null;
        }
        Iterator<Document> branchEmployeesIterator=branchEmployeesRepository.getBranchEmployees(employee.getBranch());
        int numberOfBranchEmployees = branchEmployeesRepository.getNumberOfBranchEmployees(employee.getBranch());
        if (branchEmployeesIterator!=null && numberOfBranchEmployees>0){
            return employeeService.getEmployeesArrayListFromIterator(branchEmployeesIterator);
        } else {
            return null;
        }
    }

}

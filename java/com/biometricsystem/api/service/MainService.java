package com.biometricsystem.api.service;
import com.biometricsystem.entity.employee.Employee;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class MainService {

    @Autowired
    private EmployeeService employeeService;

    public String getEmployeeName(HttpServletRequest request){
        Employee employee= employeeService.getEmployeeFromRequest(request);
        if (employee!=null){
            return employee.getName();
        }else{
            return null;
        }
    }

}
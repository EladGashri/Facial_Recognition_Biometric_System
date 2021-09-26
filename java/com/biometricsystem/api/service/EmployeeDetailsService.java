package com.biometricsystem.api.service;
import com.biometricsystem.entity.employee.Employee;
import com.biometricsystem.security.EmployeeDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;


@Service
public class EmployeeDetailsService implements UserDetailsService {

    @Autowired
    private EmployeeService employeeService;

    @Override
    public EmployeeDetails loadUserByUsername(String employeeNumber) throws UsernameNotFoundException {
        return loadUserByEmployeeNumber(Long.parseLong(employeeNumber));
    }

    private EmployeeDetails loadUserByEmployeeNumber(long employeeNumber){
        Employee employee=employeeService.getEmployee("employee number",employeeNumber);
        if (employee==null) {
            throw new UsernameNotFoundException("Could not find employee");
        }
        return new EmployeeDetails(employee);
    }

}

package com.biometricsystem.api.service;
import com.biometricsystem.api.employee.Employee;
import com.biometricsystem.api.employee.EmployeeDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;


@Service
public class EmployeeDetailsService implements UserDetailsService {

    @Autowired
    private SingleEmployeeService service;

    @Override
    public EmployeeDetails loadUserByUsername(String employeeNumber) throws UsernameNotFoundException {
        return loadUserByEmployeeNumber(Long.parseLong(employeeNumber));
    }

    public EmployeeDetails loadUserByEmployeeNumber(long employeeNumber){
        Employee employee = service.getEmployeeFromDatabaseByEmployeeNumber(employeeNumber);
        if (employee == null) {
            throw new UsernameNotFoundException("Could not find employee");
        }
        return new EmployeeDetails(employee);
    }

}

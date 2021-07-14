package com.biometricsystem.api.service;
import com.biometricsystem.api.employee.Employee;
import com.biometricsystem.api.security.jwt.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.servlet.http.HttpServletRequest;


@Service
public class SingleEmployeeService extends EmployeeService{

    @Autowired
    private JwtUtil jwtUtil;

    public Employee getEmployeeFromDatabase(long id, long number){
        if (checkIfValid(id, number)){
            return getEmployee(id, number);
        }else{
            return null;
        }
    }

    public Employee getEmployeeFromDatabaseByEmployeeNumber(long number){
        Long id=getId(number);
        if (id!=null){
            return getEmployee(id, number);
        }else{
            return null;
        }
    }

    public long getEmployeeNumberFromRequest(HttpServletRequest request) {
        String authorizationHeader = request.getHeader(JwtUtil.HEADER);
        String jwt = authorizationHeader.substring(7);
        String username = jwtUtil.extractUsername(jwt);
        return Long.parseLong(username);
    }

    public Employee getEmployeeFromRequest(HttpServletRequest request) {
        long employeeNumber=getEmployeeNumberFromRequest(request);
        return getEmployeeFromDatabaseByEmployeeNumber(employeeNumber);
    }

}
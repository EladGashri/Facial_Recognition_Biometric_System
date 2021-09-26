package com.biometricsystem.api.service;
import com.biometricsystem.security.EmployeeDetails;
import com.biometricsystem.security.jwt.AuthenticationRequest;
import com.biometricsystem.security.jwt.AuthenticationResponse;
import com.biometricsystem.security.jwt.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import java.util.HashMap;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;



@Service
public class AuthenticateService {

    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private EmployeeDetailsService employeeDetailsService;
    @Autowired
    private JwtUtil jwtUtil;


    public AuthenticationResponse getAuthenticationResponse(HashMap<String,Long> requestBody) throws BadCredentialsException{
        long id=requestBody.get("id");
        long employeeNumber=requestBody.get("employee-number");
        AuthenticationRequest authenticationRequest=new AuthenticationRequest(id,employeeNumber);
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authenticationRequest.getEmployeeNumber(),authenticationRequest.getId()));
        EmployeeDetails employeeDetails=employeeDetailsService.loadUserByUsername(authenticationRequest.getEmployeeNumber());
        String jwt=jwtUtil.generateToken(employeeDetails);
        return new AuthenticationResponse(jwt);
    }

}
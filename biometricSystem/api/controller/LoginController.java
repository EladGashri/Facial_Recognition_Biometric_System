package com.biometricsystem.api.controller;
import com.biometricsystem.api.employee.EmployeeDetails;
import com.biometricsystem.api.security.jwt.AuthenticationRequest;
import com.biometricsystem.api.security.jwt.AuthenticationResponse;
import com.biometricsystem.api.security.jwt.JwtUtil;
import com.biometricsystem.api.service.EmployeeDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/login")
public class LoginController {

    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private EmployeeDetailsService employeeDetailsService;
    @Autowired
    private JwtUtil jwtUtil;


    @PostMapping
    public ResponseEntity createAuthenticationToken(@RequestParam long id, @RequestParam long employeeNumber){
        /*@RequestBody AuthenticationRequest authenticationRequest){*/
        AuthenticationRequest authenticationRequest=new AuthenticationRequest(id,employeeNumber);
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authenticationRequest.getEmployeeNumber(),authenticationRequest.getId()));
        }catch (BadCredentialsException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Incorrect ID or employee number");
        }
        EmployeeDetails employeeDetails=employeeDetailsService.loadUserByUsername(authenticationRequest.getEmployeeNumber());
        String jwt=jwtUtil.generateToken(employeeDetails);
        return ResponseEntity.ok(new AuthenticationResponse(jwt));
    }

}
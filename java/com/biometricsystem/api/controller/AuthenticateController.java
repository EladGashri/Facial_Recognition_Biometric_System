package com.biometricsystem.api.controller;
import com.biometricsystem.api.service.AuthenticateService;
import com.biometricsystem.security.EmployeeDetails;
import com.biometricsystem.security.jwt.AuthenticationRequest;
import com.biometricsystem.security.jwt.AuthenticationResponse;
import com.biometricsystem.security.jwt.JwtUtil;
import com.biometricsystem.api.service.EmployeeDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;


@RestController
@RequestMapping("/authenticate")
public class AuthenticateController {

    @Autowired
    private AuthenticateService authenticateService;

    @PostMapping
    public ResponseEntity createAuthenticationToken(@RequestBody HashMap<String,Long> requestBody) {
       try {
            AuthenticationResponse authenticationResponse=authenticateService.getAuthenticationResponse(requestBody);
            return ResponseEntity.ok(authenticationResponse);
        }catch (BadCredentialsException e) {
           return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Incorrect ID or employee number");
       }
    }

}
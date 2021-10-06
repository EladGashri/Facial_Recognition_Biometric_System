package com.biometricsystem.api.controller;
import com.biometricsystem.api.service.LoginService;
import com.biometricsystem.security.jwt.AuthenticationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;


@RestController
@RequestMapping("/login")
public class LoginController {

    @Autowired
    private LoginService loginService;

    @PostMapping
    public ResponseEntity<?> createAuthenticationToken(@RequestBody HashMap<String,Long> requestBody) {
       try {
            AuthenticationResponse authenticationResponse= loginService.getAuthenticationResponse(requestBody);
            return new ResponseEntity<>(authenticationResponse, HttpStatus.OK);
        }catch (BadCredentialsException e) {
           return new ResponseEntity<>("Incorrect ID or employee number", HttpStatus.BAD_REQUEST);
       }
    }

}
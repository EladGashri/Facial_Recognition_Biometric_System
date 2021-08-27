package com.biometricsystem.api.controller;
import com.biometricsystem.api.service.MainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletRequest;


@RestController
@RequestMapping("/")
public class MainController {

    @Autowired
    private MainService mainService;
    private final String greeting="Hello ";

    @GetMapping
    public ResponseEntity mainGet(HttpServletRequest request){
        String employeeName=mainService.getEmployeeName(request);
        if (employeeName!=null) {
            return ResponseEntity.ok(greeting+employeeName);
        }else{
            return ResponseEntity.badRequest().build();
        }
    }

}
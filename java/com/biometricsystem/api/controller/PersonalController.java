package com.biometricsystem.api.controller;
import com.biometricsystem.api.service.PersonalService;
import com.biometricsystem.entity.employee.EmployeeDto;
import com.biometricsystem.entity.image.ImageFromDatabase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;


@RequestMapping("/personal")
@RestController
public class PersonalController {

    @Autowired
    private PersonalService personalService;

    @GetMapping
    public ResponseEntity<EmployeeDto> getPersonal(HttpServletRequest request){
        return new ResponseEntity<>(personalService.getPersonal(request), HttpStatus.OK);
    }

    @GetMapping("/images")
    public ResponseEntity<ImageFromDatabase[]> getPersonalImages(HttpServletRequest request){
        return new ResponseEntity<>(personalService.getImages(request), HttpStatus.OK);
    }

    @GetMapping("/images/{image-id}")
    public ResponseEntity<ImageFromDatabase> getPersonalImage(HttpServletRequest request,@PathVariable("image-id") String imageId){
        return new ResponseEntity<>(personalService.getImage(request,imageId), HttpStatus.OK);
    }

}
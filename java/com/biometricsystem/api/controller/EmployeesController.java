package com.biometricsystem.api.controller;
import com.biometricsystem.api.service.EmployeeService;
import com.biometricsystem.api.service.ImagesService;
import com.biometricsystem.entity.employee.EmployeeDto;
import com.biometricsystem.entity.employee.EmployeeToDatabase;
import com.biometricsystem.entity.image.ImageFromDatabase;
import com.mongodb.MongoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import org.springframework.web.server.UnsupportedMediaTypeStatusException;
import org.springframework.beans.factory.annotation.Autowired;


@RestController
@RequestMapping("/employees")
public class EmployeesController {

    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private ImagesService imagesService;

    @GetMapping("/{employee-number}")
    public ResponseEntity<EmployeeDto> getEmployee(@PathVariable("employee-number") Long employeeNumber){
        EmployeeDto employee=employeeService.getEmployeeWithImages("employee number", employeeNumber);
        if(employee!=null) {
            return ResponseEntity.ok(employee);
        }else{
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<EmployeeDto>> searchEmployee(@RequestParam("name") String name,
                                                            @RequestParam(value = "limit", required = false) Integer limit){
        return ResponseEntity.ok(employeeService.getEmployeesListByNameWithRegex(name,limit));
    }

    @PostMapping
    public ResponseEntity addEmployee(@RequestBody EmployeeToDatabase employee){
        if(employeeService.addEmployee(employee)){
            return ResponseEntity.status(HttpStatus.CREATED).build();
        }else{
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{employee-number}")
    public ResponseEntity updateEmployee(@PathVariable("employee-number") Long employeeNumber,@RequestBody HashMap<String, String> requestBody){
        Boolean updated=employeeService.updateEmployee(employeeNumber, requestBody);
        if (updated==null) {
            return ResponseEntity.badRequest().build();
        }else if(updated){
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{employee-number}")
    public ResponseEntity deleteEmployee(@PathVariable("employee-number") long employeeNumber){
        if(employeeService.deleteEmployee(employeeNumber)){
            return ResponseEntity.ok().build();
        }else{
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{employee-number}/images")
    public ResponseEntity<ImageFromDatabase[]> getEmployeeImages(@PathVariable("employee-number") Long employeeNumber){
        return ResponseEntity.ok(imagesService.getEmployeeImagesByEmployeeNumber(employeeNumber));
    }

   @GetMapping("/{employee-number}/images/{image-id}")
    public ResponseEntity<ImageFromDatabase> getImage(@PathVariable("employee-number") Long employeeNumber,
                                    @PathVariable("image-id") String imageId){
       return ResponseEntity.ok(imagesService.getImage(employeeNumber,imageId));
    }

    @PostMapping("/{employee-number}/images")
    public ResponseEntity addImage(@PathVariable("employee-number") Long employeeNumber,
                                   @RequestParam MultipartFile image) {
        try {
            if (imagesService.addImage(employeeNumber,image)) {
                return ResponseEntity.status(HttpStatus.CREATED).build();
            } else {
                return ResponseEntity.ok("No face was detected in the image");
            }
        } catch (UnsupportedMediaTypeStatusException e){
            return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).build();
        } catch (MongoException|IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

   @DeleteMapping("/{employee-number}/images/{image-id}")
    public ResponseEntity deleteImage(@PathVariable("employee-number") Long employeeNumber,
                                    @PathVariable("image-id") String imageId){
       if(imagesService.deleteImageFromDatabase(employeeNumber,imageId)){
           return ResponseEntity.ok().build();
       }else{
           return ResponseEntity.notFound().build();
       }
    }

}
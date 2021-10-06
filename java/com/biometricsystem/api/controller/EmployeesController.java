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
    public ResponseEntity<?> getEmployee(@PathVariable("employee-number") Long employeeNumber){
        EmployeeDto employee=employeeService.getEmployeeWithImages("employee number", employeeNumber);
        if(employee!=null) {
            return new ResponseEntity<>(employee, HttpStatus.OK);
        }else{
            return new ResponseEntity<>("No employee with employee number "+employeeNumber, HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<EmployeeDto>> searchEmployee(@RequestParam("name") String name,
                                                            @RequestParam(value = "limit", required = false) Integer limit){
        return new ResponseEntity<>(employeeService.getEmployeesListByNameWithRegex(name,limit), HttpStatus.OK);
    }

    @GetMapping("/verification")
    public ResponseEntity<?> verifyEmployee(@RequestParam("id") Long id, @RequestParam("employee-number") Long employeeNumber){
        if (employeeService.verifyEmployee(id, employeeNumber)){
            EmployeeDto employee=employeeService.getEmployeeWithImages("employee number", employeeNumber);
            return new ResponseEntity<>(employee, HttpStatus.OK);
        }else{
            return new ResponseEntity<>("No employee with ID "+id+" and employee number "+employeeNumber, HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping
    public ResponseEntity<String> addEmployee(@RequestBody EmployeeToDatabase employee){
        if(employeeService.addEmployee(employee)){
            return new ResponseEntity<>("Employee added", HttpStatus.CREATED);
        }else{
            return new ResponseEntity<>("Could not add employee", HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/{employee-number}")
    public ResponseEntity<String> updateEmployee(@PathVariable("employee-number") Long employeeNumber,@RequestBody HashMap<String, String> requestBody){
        Boolean updated=employeeService.updateEmployee(employeeNumber, requestBody);
        if (updated==null) {
            return new ResponseEntity<>("Could not update employee's details", HttpStatus.BAD_REQUEST);
        }else if(updated){
            return new ResponseEntity<>("Employee's details updated", HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{employee-number}")
    public ResponseEntity<String> deleteEmployee(@PathVariable("employee-number") long employeeNumber){
        if(employeeService.deleteEmployee(employeeNumber)){
            return new ResponseEntity<>("Employee deleted",HttpStatus.OK);
        }else{
            return new ResponseEntity<>("Could not delete employee", HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/{employee-number}/images")
    public ResponseEntity<ImageFromDatabase[]> getEmployeeImages(@PathVariable("employee-number") Long employeeNumber){
        return new ResponseEntity<>(imagesService.getEmployeeImagesByEmployeeNumber(employeeNumber), HttpStatus.OK);
    }

   @GetMapping("/{employee-number}/images/{image-id}")
    public ResponseEntity<?> getImage(@PathVariable("employee-number") Long employeeNumber,
                                    @PathVariable("image-id") String imageId){
        ImageFromDatabase image = imagesService.getImage(employeeNumber,imageId);
        if (image!=null){
            return new ResponseEntity<>(image, HttpStatus.OK);
        }else{
            return new ResponseEntity<>("No image with "+imageId+" ID", HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/{employee-number}/images")
    public ResponseEntity<String> addImage(@PathVariable("employee-number") Long employeeNumber,
                                   @RequestParam MultipartFile image) {
        try {
            if (imagesService.addImage(employeeNumber,image)) {
                return new ResponseEntity<>("Image added", HttpStatus.CREATED);
            } else {
                return new ResponseEntity<>("No face was detected in the image. Image not added.", HttpStatus.OK);
            }
        } catch (UnsupportedMediaTypeStatusException e){
            return new ResponseEntity<>("Must only transfer jpg, jpeg or png images", HttpStatus.UNSUPPORTED_MEDIA_TYPE);
        } catch (IOException e) {
            return new ResponseEntity<>("Could not read image", HttpStatus.BAD_REQUEST);
        }catch (MongoException e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

   @DeleteMapping("/{employee-number}/images/{image-id}")
    public ResponseEntity<String> deleteImage(@PathVariable("employee-number") Long employeeNumber,
                                    @PathVariable("image-id") String imageId){
       if(imagesService.deleteImageFromDatabase(employeeNumber,imageId)){
           return new ResponseEntity<>("Image deleted", HttpStatus.OK);
       }else{
           return new ResponseEntity<>("No image with "+imageId+" ID", HttpStatus.NOT_FOUND);
       }
    }

}
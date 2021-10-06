package com.biometricsystem.api.service;
import com.biometricsystem.entity.employee.EmployeeDto;
import com.biometricsystem.entity.image.ImageFromDatabase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.servlet.http.HttpServletRequest;


@Service
public class PersonalService {

    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private ImagesService imagesService;
    private final String employeesPath="employees/";

    public EmployeeDto getPersonal(HttpServletRequest request){
        Long employeeNumber= employeeService.getEmployeeNumberFromRequest(request);
        return employeeService.getEmployeeWithImages("employee number", employeeNumber);
    }

    public ImageFromDatabase[] getImages(HttpServletRequest request){
        Long employeeNumber= employeeService.getEmployeeNumberFromRequest(request);
        return imagesService.getEmployeeImagesByEmployeeNumber(employeeNumber);
    }

    public ImageFromDatabase getImage(HttpServletRequest request,String imageId){
        long employeeId=employeeService.getEmployeeNumberFromRequest(request);
        return imagesService.getImage(employeeId,imageId);
    }

}
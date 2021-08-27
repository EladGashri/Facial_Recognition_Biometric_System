package com.biometricsystem.api.service;
import com.biometricsystem.api.repository.EmployeesRepository;
import com.biometricsystem.api.repository.ImagesRepository;
import com.biometricsystem.branch.BranchLocation;
import com.biometricsystem.entity.employee.Employee;
import com.biometricsystem.entity.employee.EmployeeDto;
import com.biometricsystem.security.EmployeeType;
import com.biometricsystem.security.jwt.JwtUtil;
import com.mongodb.MongoException;
import org.bson.Document;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.NoSuchElementException;
import javax.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;


@Service
public class EmployeeService {

    @Autowired
    private ImagesService imagesService;
    @Autowired
    private EmployeesRepository employeesRepository;
    @Autowired
    private ImagesRepository imagesRepository;
    @Autowired
    private JwtUtil jwtUtil;

    public Employee getEmployee(long id,long employeeNumber){
        if (employeesRepository.checkIfValid(id,employeeNumber)){
            return employeesRepository.getEmployeeFromDatabase(id,employeeNumber);
        }else{
            return null;
        }
    }

    public Employee getEmployee(String field,Object value){
        try{
            return employeesRepository.getEmployeeFromDatabase(field,value);
        }catch (NoSuchElementException e) {
            return null;
        }
    }

    public EmployeeDto getEmployeeDto(long id, long employeeNumber){
        if (employeesRepository.checkIfValid(id, employeeNumber)){
            return employeesRepository.getEmployeeDtoFromDatabase(id,employeeNumber);
        }else{
            return null;
        }
    }

    public EmployeeDto getEmployeeDto(String field,Object value){
        try{
            return employeesRepository.getEmployeeDtoFromDatabase(field,value);
        }catch (NoSuchElementException e) {
            return null;
        }
    }

    public ArrayList<EmployeeDto> getEmployeesListByNameWithRegex(String name,Integer limit){
        try{
            Iterator<Document> iterator=employeesRepository.getEmployeesListByNameWithRegex(name,limit);
            return getEmployeesArrayListFromIterator(iterator);
        }catch (NoSuchElementException e) {
            return null;
        }
    }

    public EmployeeDto getEmployeeWithImages(String field,Object value){
        EmployeeDto employee=getEmployeeDto(field,value);
        if (employee!=null){
            employee.setImages(imagesRepository.getEmployeeImagesFromDatabaseAsArray(employeesRepository.getId(field,value)));
        }
        return employee;
    }

    public Long getEmployeeNumberFromRequest(HttpServletRequest request) {
        String authorizationHeader = request.getHeader(JwtUtil.HEADER);
        if (authorizationHeader==null){
            return null;
        }
        String jwt = authorizationHeader.substring(7);
        String username = jwtUtil.extractUsername(jwt);
        return Long.parseLong(username);
    }

    public Employee getEmployeeFromRequest(HttpServletRequest request) {
        Long employeeNumber=getEmployeeNumberFromRequest(request);
        return getEmployee("employee number",employeeNumber);
    }

    public String getEmployeeName(long employeeId){
        long employeeNumber=employeesRepository.getEmployeeNumber(employeeId);
        return employeesRepository.getEmployeeName(employeeId,employeeNumber);
    }

    public long getId(long employeeNumber){
        return employeesRepository.getId(employeeNumber);
    }

    public void checkIncludedInModelAfterImageInsertionOrDeletion(Employee employee,int numberOfImagesChanged){
        try{
            employeesRepository.incrementNumberOfImages(employee.getId(),employee.getEmployeeNumber(),numberOfImagesChanged);
            if ((!employee.isIncludedInModel()) && (employee.getNumberOfImages()+numberOfImagesChanged>=Employee.MINIMUM_NUMBER_OF_IMAGES_FOR_MODEL)) {
                employeesRepository.setIncludedInModel(employee.getId(), employee.getId(), true);
            }else if ((employee.isIncludedInModel()) && (employee.getNumberOfImages()+numberOfImagesChanged<Employee.MINIMUM_NUMBER_OF_IMAGES_FOR_MODEL)) {
                employeesRepository.setIncludedInModel(employee.getId(), employee.getId(), false);
            }
        }catch (MongoException e){
            e.printStackTrace();
        }
    }

    public boolean addEmployee(Employee employee) {
         return employeesRepository.insertEmployeeDocument(employee);
    }

    public Boolean updateEmployee(long employeeNumber, HashMap<String, String> requestBody){
        if (requestBody.get("name") == null && requestBody.get("branch") == null &&
                requestBody.get("employee-type") == null && requestBody.get("images-directory-path") == null) {
            return null;
        }
        Document updateDocument=new Document();
        if (requestBody.get("name")!=null){
            updateDocument.append("name", requestBody.get("name"));
        }
        if(requestBody.get("branch")!=null){
            updateDocument.append("branch", BranchLocation.getBranchLocationByLocation(requestBody.get("branch")).getDatabaseValue());
        }
        if(requestBody.get("employee-type")!=null){
            updateDocument.append("employee type", EmployeeType.getEmployeeTypeByName(requestBody.get("employee-type")).getDatabaseValue());
        }
        if(requestBody.get("images-directory-path")!=null){
            updateDocument.append("images directory path", requestBody.get("images-directory-path"));
        }
        return employeesRepository.updateEmployee(employeeNumber,updateDocument).wasAcknowledged();
    }

    public boolean deleteEmployee(long employeeNumber) {
        return employeesRepository.deleteEmployee(employeeNumber);
    }

    public ArrayList<EmployeeDto> getEmployeesArrayListFromIterator(Iterator<Document> iterator) {
        ArrayList<EmployeeDto> employeesArrayList = new ArrayList<>();
        EmployeeDto currentEmployee;
        while (iterator.hasNext()) {
            Document document = iterator.next();
            currentEmployee = EmployeeDto.getEmployeeDtoFromDocument(document);
            currentEmployee.setImages(imagesRepository.getEmployeeImagesFromDatabaseAsArray(currentEmployee.receiveId()));
            employeesArrayList.add(currentEmployee);
        }
        return employeesArrayList;
    }

}
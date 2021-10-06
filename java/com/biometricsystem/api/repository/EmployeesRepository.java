package com.biometricsystem.api.repository;
import com.biometricsystem.branch.BranchLocation;
import com.biometricsystem.database.Database;
import com.biometricsystem.entity.employee.Employee;
import com.biometricsystem.entity.employee.EmployeeDto;
import com.biometricsystem.security.EmployeeType;
import com.mongodb.MongoException;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import java.util.Iterator;
import java.util.NoSuchElementException;
import static com.mongodb.client.model.Projections.include;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;


@Repository
public class EmployeesRepository {

    @Autowired
    private Database database;
    @Autowired
    private ImagesRepository imagesRepository;

    public boolean checkIfValid(long id, long number){
        return getEmployeeName(id,number)!=null;
    }

    public Employee getEmployeeFromDatabase(long id, long employeeNumber){
        MongoCursor employeesQuery = database.getEmployeesCollection().find(getIdEmployeeNumberDocument(id,employeeNumber)).iterator();
        Document document = (Document) employeesQuery.next();
        return Employee.getEmployeeFromDocument(document);
    }

    public Employee getEmployeeFromDatabase(String field,Object value) throws NoSuchElementException {
        Document document = database.getEmployeesCollection().find(new Document(field, value)).iterator().next();
        return Employee.getEmployeeFromDocument(document);
    }

    public EmployeeDto getEmployeeDtoFromDatabase(long id, long employeeNumber){
        MongoCursor employeesQuery = database.getEmployeesCollection().find(getIdEmployeeNumberDocument(id,employeeNumber)).iterator();
        Document document = (Document) employeesQuery.next();
        return EmployeeDto.getEmployeeDtoFromDocument(document);
    }

    public EmployeeDto getEmployeeDtoFromDatabase(String field,Object value) throws NoSuchElementException {
        Document document = database.getEmployeesCollection().find(new Document(field, value)).iterator().next();
        return EmployeeDto.getEmployeeDtoFromDocument(document);
    }

    public int getNumberOfEmployees(){
        try {
            return (int) database.getEmployeesCollection().countDocuments();
        }catch (MongoException e){
            e.printStackTrace();
            System.err.println("could not find the number of employees. returned 0");
            return 0;
        }
    }

    public Iterator<Document> getEmployeesListByNameWithRegex(String name, Integer limit) throws NoSuchElementException {
        if (limit != null) {
            return database.getEmployeesCollection().find(new Document("name", new Document("$regex", name))).limit(limit).iterator();
        } else {
            return database.getEmployeesCollection().find(new Document("name", new Document("$regex", name))).iterator();
        }
    }

    public boolean insertEmployeeDocument(Employee employee){
        return database.insertEmployeeDocument(employee);
    }

    public UpdateResult updateEmployee(long employeeNumber, Document updateDocument){
        return database.getEmployeesCollection().updateOne(new Document("employee number", employeeNumber),new Document("$set",updateDocument));
    }

    public boolean deleteEmployee(long employeeNumber) {
        return database.getEmployeesCollection().deleteOne(new Document("employee number", employeeNumber)).wasAcknowledged();
    }

    public String getEmployeeName(long id, long employeeNumber) {
        try {
            MongoCursor employeesQuery = database.getEmployeesCollection().find(getIdEmployeeNumberDocument(id,employeeNumber))
                    .projection(include("name")).iterator();
            Document document = (Document) employeesQuery.next();
            return document.getString("name");
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    public EmployeeType getEmployeeType(long id, long employeeNumber){
        try {
            MongoCursor employeesQuery = database.getEmployeesCollection().find(getIdEmployeeNumberDocument(id,employeeNumber))
                    .projection(include("employee type")).iterator();
            Document document = (Document) employeesQuery.next();
            return EmployeeType.getEmployeeTypeByDatabaseValue(document.getInteger("employee type"));
        }catch (NoSuchElementException e){
            return EmployeeType.UNDEFINED;
        }
    }

    public BranchLocation getBranch(long id, long employeeNumber){
        try {
            MongoCursor employeesQuery = database.getEmployeesCollection().find(getIdEmployeeNumberDocument(id,employeeNumber))
                    .projection(include("branch")).iterator();
            Document document = (Document) employeesQuery.next();
            return BranchLocation.getBranchLocationByDatabaseValue(document.getString("branch"));
        }catch (NoSuchElementException e){
            return BranchLocation.UNDEFINED;
        }
    }

    public Long getId(long employeeNumber){
        try {
            MongoCursor employeesQuery = database.getEmployeesCollection().find(new Document("employee number", employeeNumber))
                    .projection(include("_id")).iterator();
            Document document = (Document) employeesQuery.next();
            try {
                return Long.valueOf(document.getInteger("_id"));
            }catch(java.lang.ClassCastException e){
                return document.getLong("_id");
            }
        }catch (NoSuchElementException e) {
            return null;
        }
    }

    public Long getId(String field,Object value){
        try {
            MongoCursor employeesQuery = database.getEmployeesCollection().find(new Document(field, value))
                    .projection(include("_id")).iterator();
            Document document = (Document) employeesQuery.next();
            try {
                return Long.valueOf(document.getInteger("_id"));
            }catch(java.lang.ClassCastException e){
                return document.getLong("_id");
            }
        }catch (NoSuchElementException e) {
            return null;
        }
    }

    public Long getEmployeeNumber(long id){
        try {
            MongoCursor employeesQuery = database.getEmployeesCollection().find(new Document("_id", id))
                    .projection(include("employee number")).iterator();
            Document document = (Document) employeesQuery.next();
            try {
                return Long.valueOf(document.getInteger("employee number"));
            }catch(java.lang.ClassCastException e){
                return document.getLong("employee number");
            }
        }catch (NoSuchElementException e) {
            return null;
        }
    }

    public Integer getNumberOfImages(long id, long employeeNumber){
        try {
            MongoCursor employeesQuery = database.getEmployeesCollection().find(getIdEmployeeNumberDocument(id,employeeNumber))
                    .projection(include("number of images")).iterator();
            Document document = (Document) employeesQuery.next();
            return  document.getInteger("number of images");
        }catch (NoSuchElementException e) {
            return 0;
        }
    }

    public Integer getModelClass(long id, long employeeNumber){
        try {
            MongoCursor employeesQuery = database.getEmployeesCollection().find(getIdEmployeeNumberDocument(id,employeeNumber))
                    .projection(include("model class")).iterator();
            Document document = (Document) employeesQuery.next();
            return  document.getInteger("model class");
        }catch (NoSuchElementException e) {
            return 0;
        }
    }

    public Boolean getIncludedInModel(long id, long employeeNumber){
        try {
            MongoCursor employeesQuery = database.getEmployeesCollection().find(getIdEmployeeNumberDocument(id,employeeNumber))
                    .projection(include("included in model")).iterator();
            Document document = (Document) employeesQuery.next();
            return document.getBoolean("included in model");
        }catch (NoSuchElementException e) {
            return false;
        }
    }

    public String getImagesDirectoryPath(long id, long employeeNumber){
        try {
            MongoCursor employeesQuery = database.getEmployeesCollection().find(getIdEmployeeNumberDocument(id,employeeNumber))
                    .projection(include("images directory path")).iterator();
            Document document = (Document) employeesQuery.next();
            return  document.getString("images directory path");
        }catch (NoSuchElementException e) {
            return null;
        }
    }

    public void incrementNumberOfImages(long id, long employeeNumber, int incrementValue){
        database.getEmployeesCollection().updateOne(getIdEmployeeNumberDocument(id,employeeNumber), new Document("$inc",new Document("number of images",incrementValue)));
    }

    public void setIncludedInModel(long id, long employeeNumber,boolean included){
        database.getEmployeesCollection().updateOne(getIdEmployeeNumberDocument(id,employeeNumber),new Document("$set",new Document("included in model",included)));
    }

    private static Document getIdEmployeeNumberDocument(long id, long employeeNumber){
        return new Document("_id", id).append("employee number", employeeNumber);
    }

}
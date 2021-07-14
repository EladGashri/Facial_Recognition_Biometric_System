package com.biometricsystem.api.service;
import com.biometricsystem.api.employee.BranchType;
import com.biometricsystem.api.employee.Employee;
import com.biometricsystem.api.employee.EmployeeType;
import com.biometricsystem.database.Database;
import org.springframework.beans.factory.annotation.Autowired;
import com.mongodb.client.MongoCursor;
import static com.mongodb.client.model.Projections.include;
import org.bson.Document;
import java.util.NoSuchElementException;


public abstract class EmployeeService {

    @Autowired
    protected Database database;

    public boolean checkIfValid(long id, long number){
        return !(getEmployeeName(id,number)==null);
    }

    protected Employee getEmployee(long employeeId, long employeeNumber){
        MongoCursor employeesQuery = database.getEmployeesCollection().find(new Document("_id", employeeId).append("employee number", employeeNumber)).iterator();
        Document document = (Document) employeesQuery.next();
        return getEmployeeFromDocument(document);
    }

    protected Employee getEmployeeFromDocument(Document document){
        return new Employee(
                Long.valueOf(document.getInteger("_id")),
                Long.valueOf(document.getInteger("employee number")),
                document.getString("name"),
                document.getString("branch"),
                document.getBoolean("admin"));
                //add later after the database has been updated
                /*document.getInteger("number of images"),
                document.getDouble("accuracy"),
                document.getInteger("mode class"),
                document.getBoolean("included in model"))*/
    }

    public String getEmployeeName(long employeeId, long employeeNumber) {
        try {
            MongoCursor employeesQuery = database.getEmployeesCollection().find(new Document("_id", employeeId).append("employee number", employeeNumber))
                    .projection(include("name")).iterator();
            Document document = (Document) employeesQuery.next();
            return document.getString("name");
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    public EmployeeType getEmployeeType(long employeeId, long employeeNumber){
        try {
            MongoCursor employeesQuery = database.getEmployeesCollection().find(new Document("_id", employeeId).append("employee number", employeeNumber))
                    .projection(include("admin")).iterator();
            Document document = (Document) employeesQuery.next();
             if(document.getBoolean("admin")){
                 return EmployeeType.ADMIN;
             }else{
                 return EmployeeType.STANDARD;
             }
        }catch (NoSuchElementException e){
            return EmployeeType.UNDEFINED;
        }
    }

    public BranchType getBranch(long employeeId, long employeeNumber){
        try {
            MongoCursor employeesQuery = database.getEmployeesCollection().find(new Document("_id", employeeId).append("employee number", employeeNumber))
                    .projection(include("branch")).iterator();
            Document document = (Document) employeesQuery.next();
            return BranchType.getBranchTypeByDatabaseValue(document.getString("branch"));
        }catch (NoSuchElementException e){
            return BranchType.UNDEFINED;
        }
    }

    public Long getId(long employeeNumber){
        try {
            MongoCursor employeesQuery = database.getEmployeesCollection().find(new Document("employee number", employeeNumber))
                    .projection(include("_id")).iterator();
            Document document = (Document) employeesQuery.next();
            return  Long.valueOf(document.getInteger("_id"));
        }catch (NoSuchElementException e) {
            return null;
        }
    }

    public Long getEmployeeNumber(long employeeId){
        try {
            MongoCursor employeesQuery = database.getEmployeesCollection().find(new Document("_id", employeeId))
                    .projection(include("employee number")).iterator();
            Document document = (Document) employeesQuery.next();
            return  Long.valueOf(document.getInteger("employee number"));
        }catch (NoSuchElementException e) {
            return null;
        }
    }

    public Integer getNumberOfImages(long employeeId, long employeeNumber){
        try {
            MongoCursor employeesQuery = database.getEmployeesCollection().find(new Document("_id", employeeId).append("employee number", employeeNumber))
                    .projection(include("number of images")).iterator();
            Document document = (Document) employeesQuery.next();
            return  document.getInteger("number of images");
        }catch (NoSuchElementException e) {
            return 0;
        }
    }

    public Double getAccuracy(long employeeId, long employeeNumber){
        try {
            MongoCursor employeesQuery = database.getEmployeesCollection().find(new Document("_id", employeeId).append("employee number", employeeNumber))
                    .projection(include("accuracy")).iterator();
            Document document = (Document) employeesQuery.next();
            return document.getDouble("accuracy");
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    public Boolean getIncludedInModel(long employeeId, long employeeNumber){
        try {
            MongoCursor employeesQuery = database.getEmployeesCollection().find(new Document("_id", employeeId).append("employee number", employeeNumber))
                    .projection(include("included in model")).iterator();
            Document document = (Document) employeesQuery.next();
            return document.getBoolean("included in model");
        }catch (NoSuchElementException e) {
            return false;
        }
    }

}
package com.biometricsystem.api.service;
import com.mongodb.client.MongoCursor;
import com.biometricsystem.api.employee.BranchType;
import com.biometricsystem.api.employee.Employee;
import org.bson.Document;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.NoSuchElementException;


@Service
public class BranchService extends EmployeeService {

    public ArrayList<Employee> getBranchEmployees(BranchType branch) {
        try {
            MongoCursor branchQuery = database.getEmployeesCollection().find(new Document("branch", branch.getDatabaseValue())).iterator();
            int numberOfBranchEmployees = (int) database.getEmployeesCollection().countDocuments(new Document("branch", branch.getDatabaseValue()));
            ArrayList<Employee> employeesList = new ArrayList<>(numberOfBranchEmployees);
            Employee currentEmployee;
            while (branchQuery.hasNext()) {
                Document document = (Document) branchQuery.next();
                currentEmployee = getEmployeeFromDocument(document);
                employeesList.add(currentEmployee);
            }
            return employeesList;
        } catch (NoSuchElementException e) {
            return null;
        }
    }

}
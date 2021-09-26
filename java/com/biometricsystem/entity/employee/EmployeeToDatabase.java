package com.biometricsystem.entity.employee;
import com.biometricsystem.entity.employee.Employee;
import com.biometricsystem.security.EmployeeType;


public class EmployeeToDatabase extends Employee {

    public EmployeeToDatabase(int id, int employeeNumber, String name, String branch,String employeeType, String imagesDirectoryPath){
        super(id,employeeNumber,name,branch, EmployeeType.getEmployeeTypeByName(employeeType).getDatabaseValue(),0,0,-1,imagesDirectoryPath,false);
    }

}
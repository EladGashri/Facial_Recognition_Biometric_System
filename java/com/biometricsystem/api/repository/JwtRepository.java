package com.biometricsystem.api.repository;
import com.biometricsystem.database.Database;
import com.biometricsystem.entity.employee.Employee;
import com.biometricsystem.security.EmployeeDetails;
import com.biometricsystem.security.EmployeeType;
import com.biometricsystem.security.jwt.JwtUtil;
import com.mongodb.client.MongoCursor;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;


@Repository
public class JwtRepository {

    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private Database database;

    public String getCtoJwt(){
        MongoCursor query=database.getEmployeesCollection().find(new Document("employee type", EmployeeType.CTO.getDatabaseValue())).iterator();
        Document document=(Document) query.next();
        Employee cto=Employee.getEmployeeFromDocument(document);
        return jwtUtil.generateInfiniteToken(new EmployeeDetails(cto));
    }

}
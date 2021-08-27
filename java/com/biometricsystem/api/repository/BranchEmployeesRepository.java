package com.biometricsystem.api.repository;
import com.biometricsystem.branch.BranchLocation;
import com.biometricsystem.database.Database;
import com.mongodb.MongoException;
import org.bson.Document;
import java.util.Iterator;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Repository;
import org.springframework.beans.factory.annotation.Autowired;


@Repository
public class BranchEmployeesRepository {

    @Autowired
    private Database database;

    public int getNumberOfBranchEmployees(BranchLocation branch){
        try {
            return (int) database.getEmployeesCollection().countDocuments(new Document("branch", branch.getDatabaseValue()));
        }catch (MongoException e){
            e.printStackTrace();
            System.err.println("could not find the number of employees. returned 0");
            return 0;
        }
    }

    public Iterator<Document> getBranchEmployees(BranchLocation branch) {
        try {
            return database.getEmployeesCollection().find(new Document("branch", branch.getDatabaseValue())).iterator();
        } catch (NoSuchElementException e) {
            return null;
        }
    }

}

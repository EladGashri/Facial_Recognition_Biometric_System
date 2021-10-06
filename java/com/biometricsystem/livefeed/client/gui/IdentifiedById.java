package com.biometricsystem.livefeed.client.gui;
import com.biometricsystem.database.Database;
import com.biometricsystem.entity.employee.Employee;
import com.biometricsystem.main.BiometricSystem;
import com.biometricsystem.security.EmployeeDetails;
import com.biometricsystem.security.EmployeeType;
import com.biometricsystem.security.jwt.JwtUtil;
import com.mongodb.client.MongoCursor;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;
import org.bson.Document;
import org.springframework.http.HttpStatus;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;


public class IdentifiedById extends Thread{

    private final static String VERIFICATION_URI_STRING = BiometricSystem.API_HOST+"/employees/verification";
    private final static String ENTRY_NOW_URI_STRING = BiometricSystem.API_HOST+"/attendances/entry-now";
    private long id;
    private long employeeNumber;
    private LiveFeedGUIController controller;

    public IdentifiedById(long id, long employeeNumber, LiveFeedGUIController controller){
        this.id=id;
        this.employeeNumber=employeeNumber;
        this.controller=controller;
        setPriority(Thread.MIN_PRIORITY);
    }

    @Override
    public void run(){
        try {
            CloseableHttpResponse verificationResponse = sendVerificationRequest();
            if (verificationResponse.getStatusLine().getStatusCode() == HttpStatus.OK.value()) {
                System.out.println("employee identified By ID and employee number");
                CloseableHttpResponse entryNowResponse = sendEntryNowRequest();
                if (entryNowResponse.getStatusLine().getStatusCode() == HttpStatus.CREATED.value()) {
                    System.out.println("employee entry registered");
                    controller.openGate();
                }
            } else {
                System.out.println("invalid ID or employee number");
            }
        }catch( NullPointerException e){
            e.printStackTrace();
        }
    }

    private URI getVerificationUri(){
        try{
            URIBuilder uriBuilder = new URIBuilder(VERIFICATION_URI_STRING);
            uriBuilder.addParameter("id", String.valueOf(id)).setParameter("employee-number", String.valueOf(employeeNumber));
            return uriBuilder.build();
        }catch (URISyntaxException e){
            e.printStackTrace();
            return null;
        }
    }

    private CloseableHttpResponse sendVerificationRequest(){
        try {
            HttpGet verificationRequest = new HttpGet();
            URI verificationUri = getVerificationUri();
            verificationRequest.setURI(verificationUri);
            String jwt = getAdminJwt();
            verificationRequest.setHeader(JwtUtil.HEADER, JwtUtil.TOKEN_PREFIX + jwt);
            return HttpClients.createDefault().execute(verificationRequest);
        }catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private URI getEntryNowUri(){
        try{
            URIBuilder uriBuilder = new URIBuilder(ENTRY_NOW_URI_STRING);
            uriBuilder.setParameter("employee-number", String.valueOf(employeeNumber));
            return uriBuilder.build();
        }catch (URISyntaxException e){
            e.printStackTrace();
            return null;
        }
    }

    private CloseableHttpResponse sendEntryNowRequest(){
        try {
            HttpPut entryNowUriRequest = new HttpPut();
            URI entryNowUri = getEntryNowUri();
            entryNowUriRequest.setURI(entryNowUri);
            String jwt = getAdminJwt();
            entryNowUriRequest.setHeader(JwtUtil.HEADER, JwtUtil.TOKEN_PREFIX + jwt);
            return HttpClients.createDefault().execute(entryNowUriRequest);
        }catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String getAdminJwt(){
        MongoCursor query=new Database().getEmployeesCollection().find(new Document("employee type", EmployeeType.ADMIN.getDatabaseValue())).iterator();
        Document document=(Document) query.next();
        Employee cto=Employee.getEmployeeFromDocument(document);
        return new JwtUtil().generateInfiniteToken(new EmployeeDetails(cto));
    }

}
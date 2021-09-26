package com.biometricsystem.livefeed.client.gui;
import com.biometricsystem.api.repository.AttendanceRepository;
import com.biometricsystem.api.service.EmployeeService;
import com.biometricsystem.api.service.ImagesService;
import com.biometricsystem.database.Time;
import com.biometricsystem.entity.employee.Employee;
import com.biometricsystem.entity.image.CapturedFrame;
import org.springframework.beans.factory.annotation.Autowired;


public class IdentifiedById extends Thread{

    @Autowired
    private AttendanceRepository attendanceRegisterService;;
    @Autowired
    private ImagesService imagesService;
    @Autowired
    private EmployeeService employeeService;
    private CapturedFrame currentFrame;
    private long id;
    private long employeeNumber;
    private LiveFeedGUIController controller;

    public IdentifiedById(CapturedFrame currentFrame, long id, long employeeNumber, LiveFeedGUIController controller){
        this.currentFrame=currentFrame;
        this.id=id;
        this.employeeNumber=employeeNumber;
        this.controller=controller;
        setPriority(Thread.MIN_PRIORITY);
    }

    @Override
    public void run(){
        Employee employee=employeeService.getEmployee(id,employeeNumber);
        currentFrame.setEmployee(employee);
        if (currentFrame.getEmployee()!=null) {
            System.out.println(currentFrame.getEmployee().getName()+"Identified By Id");
            imagesService.saveImageToDatabase(currentFrame);
            imagesService.saveImageToDirectory(currentFrame);
            try {
                attendanceRegisterService.checkAndRegisterEntry(currentFrame.getEmployee());
            } catch (Time.TimeException timeException) {
                timeException.printStackTrace();
            }
            controller.openGate();
        } else {
            System.out.println("Invalid employee ID and employee number");
        }
    }

}
package com.biometricsystem.livefeed;
import com.biometricsystem.image.CapturedFrame;
import com.biometricsystem.database.Database;
import com.biometricsystem.image.FaceRecognitionResult;
import com.biometricsystem.api.employee.Employee;
import com.biometricsystem.api.service.SingleEmployeeService;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import javax.swing.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicBoolean;


public class LiveFeedStandGUI extends Thread{

    /*@Autowired*/
    private Database database;
    /*@Autowired*/
    private SingleEmployeeService employeeService;
    private LiveFeedStand stand;
    private Mat frame;
    private AtomicBoolean entryStatus;
    private AtomicBoolean pause;
    private JPanelOpenCV jPanel;
    private JFrame jFrame;
    private final DateTimeFormatter formatter= DateTimeFormatter.ofPattern("dd-MM-yyyy HH-mm-ss");
    private final ImageIcon logo=new ImageIcon("src\\main\\resources\\static\\images\\logos\\amdocs.jpg");
    private long numberOfFacesRecognized, numberOfUnrecognizedFaces;
    private final short numberOfFramesUntilIdentificationById=10;

    public LiveFeedStandGUI(LiveFeedStand liveFeedStand, AtomicBoolean entryStatus){
        stand=liveFeedStand;
        //delete later and find a way to transfer the database as a bean
        database=new Database();
        //delete later and find a way to transfer the employeeService as a bean
        employeeService=new SingleEmployeeService();
        this.entryStatus= entryStatus;
        frame=new Mat();
        jPanel=new JPanelOpenCV();
        jFrame=new JFrame();
        jFrame.setIconImage(logo.getImage());
        pause=new AtomicBoolean(false);
    }

    @Override
    public void run() {
        boolean[] employeesEntryToday=stand.getEmployeeEntryToday();
        //get CapturedFrame via UDP
        CapturedFrame currentFrame=new CapturedFrame(new Mat());
        while (stand.getDate().equals(LocalDate.now())){
            if (currentFrame.getResult() == FaceRecognitionResult.FACE_RECOGNIZED) {
                entryStatus.set(true);
                System.out.println("FACE RECOGNIZED IN RESULTS ANALYZER\n");
                if (!employeesEntryToday[currentFrame.getModelClass()] || LiveFeed.OVERRIDE_ENTRY) {
                    faceRecognized(currentFrame);
                }
            } else if (currentFrame.getResult() == FaceRecognitionResult.FACE_DETECTED_AND_NOT_RECOGNIZED) {
                System.out.println("FACE DETECTED AND NOT RECOGNIZED IN RESULTS ANALYZER\n");
                noFaceRecognized(currentFrame);
                if (numberOfUnrecognizedFaces%numberOfFramesUntilIdentificationById==0) {
                    pause.set(true);
                    identifyById(currentFrame);
                    pause.set(false);
                }
            }else if (currentFrame.getResult() == FaceRecognitionResult.FACE_NOT_DETECTED) {
                System.out.println("FACE NOT DETECTED IN RESULTS ANALYZER\n");
                noFaceDetected();
            }
        }
    }


    public void faceRecognized(CapturedFrame currentFrame) {
        numberOfFacesRecognized++;
        int employeeId = currentFrame.getModelClass();
        currentFrame.setName(stand.getIdToName());
        String employeeName = currentFrame.getName();
        String frameTitle = "Face recognized as " + employeeName + " with " + currentFrame.getRecognitionProbabilityString() + "% probability. Entry allowed.";
        System.out.print("id detected: " + employeeId + "\nrecognition probability: " + currentFrame.getRecognitionProbabilityString() +
                "%%\nface recognized number " + numberOfFacesRecognized + " as " + employeeName + "\n");
        pause.set(true);
        jPanel.window(jFrame, jPanel.MatToBufferedImage(currentFrame.getValues()), frameTitle, 0, 0);
        try{
            Thread.sleep(5000);
        }catch (InterruptedException ignored){}
        jFrame.setVisible(false);
        jPanel.setVisible(false);
        pause.set(false);
        Imgcodecs.imwrite(stand.getFacesRecognizedDirectory() + "\\" + employeeName + " " + LocalDateTime.now().format(formatter) + ".jpg", currentFrame.getValues());
    }


    public void noFaceRecognized(CapturedFrame currentFrame) {
        numberOfUnrecognizedFaces++;
        System.out.print("unrecognized face number " + numberOfUnrecognizedFaces + "\nrecognition probability " + currentFrame.getRecognitionProbabilityString()  + "%\n");
    }

    public void noFaceDetected() {
        System.out.println("No face detected");
    }

    public void identifyById(CapturedFrame currentFrame){
        JPanelOpenCV.idNumberDialogBox dialog= jPanel.new idNumberDialogBox();
        pause.set(true);
        dialog.setIdAndNumber(currentFrame, jFrame);
        try{
            Thread.sleep(5000);
        }catch (InterruptedException ignored){}
        // the employee didn't enter an ID
        if (!dialog.wasOkPressed()) {
            System.out.println("employee ID and employee number not entered");
        } else {
            Employee employee=employeeService.getEmployeeFromDatabase(dialog.getEmployeeId(),dialog.getEmployeeNumber());
            //the employee entered a correct ID
            if (employee!=null){
                currentFrame.setModelClass(dialog.getEmployeeId());
                entryStatus.set(true);
                currentFrame.setName(stand.getIdToName());
                currentFrame.saveImageToDatabase(database);
                // checking if the employee needs to register entry to the database
                stand.checkAndRegisterEntry(currentFrame);
            // the employee entered an incorrect ID
            }else{
                System.out.println("Invalid employee ID and employee number");
            }
        }
        jFrame.setVisible(false);
        jPanel.setVisible(false);
        pause.set(false);
    }

    public void printEntryStatus(){
        if (entryStatus.get()){
            System.out.println("entry allowed\n");
        }else{
            System.out.println("entry denied\n");
        }
    }

}



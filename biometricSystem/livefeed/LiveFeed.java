package com.biometricsystem.livefeed;
import java.time.LocalDate;
import java.util.Arrays;
import com.biometricsystem.image.CapturedFrame;
import com.biometricsystem.database.Database;
import com.biometricsystem.database.Time;
import com.mongodb.MongoException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;
import org.bson.Document;
import org.springframework.stereotype.Controller;
import static com.mongodb.client.model.Projections.include;
import java.time.LocalDateTime;
import java.io.File;


@Controller
public class LiveFeed {

    /*@Autowired*/
    public Database database;
    public static final boolean OVERRIDE_ENTRY=true;
    public static final double FACE_RECOGNITION_THRESHOLD=0.9;
    private final LocalDate date;
    private String[] idToName;
    private int numberOfEmployees;
    private boolean[] employeesEntryToday;
    private static final File facesRecognizedDirectory=new File("src\\main\\resources\\static\\images\\faces recognized");

    public LiveFeed() {
        //delete later and find a way to transfer the database as a bean
        database=new Database();
        numberOfEmployees=(int) database.getNumberOfEmployees();
        idToName = new String[numberOfEmployees];
        Arrays.fill(idToName, "None");
        employeesEntryToday = new boolean[numberOfEmployees];
        Arrays.fill(employeesEntryToday, false);
        date=LocalDate.now();
        if (!facesRecognizedDirectory.exists()) {
            facesRecognizedDirectory.mkdir();
        }
        updateAll();
    }

    public LocalDate getDate(){
        return date;
    }

    public boolean[] getEmployeeEntryToday(){
        return employeesEntryToday;
    }


    public static File getFacesRecognizedDirectory(){
        return facesRecognizedDirectory;
    }

    public int getNumberOfEmployees(){
        return numberOfEmployees;
    }

    public void updateAll(){
        numberOfEmployees=(int) database.getNumberOfEmployees();
        idToName=new String[numberOfEmployees];
        Arrays.fill(idToName,"None");
        updateIdToName();
        employeesEntryToday=new boolean[numberOfEmployees];
        Arrays.fill(employeesEntryToday,false);
        updateEmployeesEntryTodayArray(date);
    }

    public String[] getIdToName() {
        return idToName;
    }

    public void updateEmployeeEntryToday(int idDetected) {
        employeesEntryToday[idDetected]=true;
    }

    public void updateEmployeesEntryTodayArray(LocalDate date){
        try {
            Document dateDocument = new Document("year", date.getYear()).append("month", date.getMonthValue()).append("day", date.getDayOfMonth());
            Document employeesEntryFind = new Document("date", dateDocument).append("entry", new Document("$ne", null));
            FindIterable attendanceQuery = database.getAttendanceCollection().find(employeesEntryFind);
            MongoCursor employeeIdValues = attendanceQuery.projection(include("employee id")).iterator();
            Document nextEmployee;
            while (employeeIdValues.hasNext()) {
                nextEmployee = (Document) employeeIdValues.next();
                employeesEntryToday[nextEmployee.getInteger("employee id")] = true;
            }
        }catch (MongoException e){
            System.err.println("could not update EmployeesEntryToday due to database failure");
        }
    }

    public void updateIdToName(){
        try {
            FindIterable attendanceQuery = database.getEmployeesCollection().find();
            MongoCursor employeesValues = attendanceQuery.projection(include("_id", "name")).iterator();
            Document nextEmployee;
            while (employeesValues.hasNext()) {
                nextEmployee = (Document) employeesValues.next();
                idToName[nextEmployee.getInteger("_id")] = nextEmployee.getString("name");
            }
            //DELETE LATER
            idToName[152]="Elad Gashri";
            idToName[605]="Yonatan Jenudi";
        }catch (MongoException e){
            System.err.println("could not update IdToName due to database failure");
        }
    }

    public void registerEntry(LocalDate date, int idDetected, String[] idToName) throws Time.TimeException {
        try{
            Time entryTime=new Time(LocalDateTime.now().getHour(),LocalDateTime.now().getMinute(),LocalDateTime.now().getSecond());
            Document entryDocument= new Document("entry", entryTime.getDocument(false));
            Document dateDocument=new Document("year",date.getYear()).append("month",date.getMonthValue()).append("day",date.getDayOfMonth());
            Document employeesEntryFind=new Document("employee id",idDetected).append("date",dateDocument);
            Document attendanceQuery= (Document) database.getAttendanceCollection().find(employeesEntryFind).first();
            String name=idToName[idDetected];
            if(attendanceQuery!=null) {
                if (OVERRIDE_ENTRY) {
                    Document exitDocument=(Document)attendanceQuery.get("exit");
                    if (exitDocument!=null){
                        Time exitTime=new Time((int) exitDocument.get("hour"),(int) exitDocument.get("minute"),(int) exitDocument.get("second"));
                        if (!entryTime.checkIfEntryBeforeExit(exitTime)){
                            throw new Time.TimeException("entry time must be before exit time");
                        }
                        Time totalTime=entryTime.getTimeDifference(exitTime);
                        Document updateOperation = new Document("$set", entryDocument.append("total",totalTime.getDocument(true)));
                        database.getAttendanceCollection().updateOne(attendanceQuery, updateOperation);
                        System.out.println("database entry and total updated for " + name);

                    }else {
                        Document updateOperation = new Document("$set", entryDocument);
                        database.getAttendanceCollection().updateOne(attendanceQuery, updateOperation);
                        System.out.println("database entry updated for " + name);
                    }
                } else {
                    System.out.println( name + " already registered entry today. must allow overrideEntry in order to update entry");
                }
            }else{
                database.insertAttendanceDocument(idDetected, date, entryTime, null);
                System.out.println("database entry registered for " + name);
            }
        }catch (MongoException e){
            System.err.println("could not register entry due to database failure");
        }

    }

    public void registerExit(LocalDate date, int idDetected, String[] idToName) throws Time.TimeException{
        try{
            Time exitTime=new Time(LocalDateTime.now().getHour(),LocalDateTime.now().getMinute(),LocalDateTime.now().getSecond());
            Document dateDocument=new Document("year",date.getYear()).append("month",date.getMonthValue()).append("day",date.getDayOfMonth());
            Document employeesEntryFind=new Document("employee id",idDetected).append("date",dateDocument).append("entry", new Document("$ne",null));
            Document entryQuery= (Document) database.getAttendanceCollection().find(employeesEntryFind).first();
            String name=idToName[idDetected];
            if(entryQuery==null) {
                System.out.println(name + " didn't register entry today and therefore cannot register exit");
            }
            else{
                Document findAlreadyRegisteredExit=new Document("employee_id",idDetected).append("date",dateDocument)
                        .append("exit",new Document("$ne",null));
                Document exitQuery= (Document) database.getAttendanceCollection().find(findAlreadyRegisteredExit).first();
                if (!OVERRIDE_ENTRY && exitQuery!=null) {
                    System.out.println(name + " already registered exit today. must allow overrideExit in order to update exit");
                }else{
                    Document entryDocument=(Document) entryQuery.get("entry");
                    Time entryTime=new Time((int) entryDocument.get("hour"),(int) entryDocument.get("minute"),(int) entryDocument.get("second"));
                    if (!entryTime.checkIfEntryBeforeExit(exitTime)){
                        throw new Time.TimeException("entry time must be before exit time");
                    }
                    Time totalTime=entryTime.getTimeDifference(exitTime);
                    Document updateOperation= new Document("$set",new Document("exit",exitTime.getDocument(false)).append("total",totalTime.getDocument(true)));
                    database.getAttendanceCollection().updateOne(entryQuery,updateOperation);
                    if (exitQuery!=null){
                        System.out.println("database exit and total updated for " + name);
                    }else{
                        System.out.println("database exit and total registered for " + name);
                    }
                }
            }
        }catch (MongoException e){
            System.err.println("could not register exit due to database failure");
        }
    }

    public boolean needToRegisterEntry(int id){
        return !employeesEntryToday[id] || OVERRIDE_ENTRY;
    }

    public void checkAndRegisterEntry(CapturedFrame currentFrame) {
        if (needToRegisterEntry(currentFrame.getModelClass())) {
            try {
                registerEntry(date,currentFrame.getModelClass(),idToName);
                updateEmployeeEntryToday(currentFrame.getModelClass());
            } catch (Time.TimeException e) {
                System.err.println("cannot register entry. registered exit today is before entry time");
            }
        } else {
            System.out.println(currentFrame.getName() + " already registered entry today");
        }
    }

    public LiveFeed getLiveFeed(){
        return this;
    }

}
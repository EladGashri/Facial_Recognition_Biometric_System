package com.biometricsystem.api.employee;
import org.springframework.data.mongodb.core.mapping.Document;


@Document(collection="employees")
public class Employee{

    private final long id;
    private final long employeeNumber;
    private String name;
    private BranchType branch;
    private EmployeeType type;
    private int numberOfImages;
    private double modelAccuracy;
    private long modelClass;
    private boolean includedInModel;
    public final static int MINIMUM_NUMBER_OF_IMAGES_FOR_MODEL=4;

    public Employee(long id, long employeeNumber){
        this.id=id;
        this.employeeNumber = employeeNumber;
        name=null;
        branch=BranchType.UNDEFINED;
        type=EmployeeType.UNDEFINED;
    }

    public Employee(long id, long employeeNumber, String name, String branch, boolean type){/* int numberOfImages, double modelAccuracy, long modelClass, boolean includedInModel) {*/
        this.id = id;
        this.employeeNumber = employeeNumber;
        this.name = name;
        this.branch = BranchType.getBranchTypeByDatabaseValue(branch);
        this.type = type?EmployeeType.ADMIN:EmployeeType.STANDARD;
        //delete after the database has been updated and add the part below
        this.numberOfImages = 0;
        this.modelAccuracy = 0;
        this.includedInModel = false;
    }
        /*this.numberOfImages = numberOfImages;
        this.modelAccuracy = modelAccuracy;
        this.modelClass=modelClass;
        this.includedInModel = includedInModel;
    }*/

    public String getPassword(){
        return String.valueOf(id);
    }

    public String getUsername(){
        return String.valueOf(employeeNumber);
    }

    public void setBranch(BranchType branch) {
        this.branch = branch;
    }

    public void setType(EmployeeType type) {
        this.type = type;
    }

    public void setNumberOfImages(int numberOfImages) {
        this.numberOfImages = numberOfImages;
    }

    public void setModelAccuracy(double modelAccuracy) {
        this.modelAccuracy = modelAccuracy;
    }

    public void setIncludedInModel(boolean includedInModel) {
        this.includedInModel = includedInModel;
    }

    @Override
    public boolean equals(Object employeeObject){
        Employee employee=(Employee) employeeObject;
        return id==employee.getId();
    }

    public boolean isIncludedInModel(){
        return includedInModel;
    }

    public int getNumberOfImages() {
        return numberOfImages;
    }

    public int getNumberOfMissingImagesForModel(){
        return Math.max(0,Employee.MINIMUM_NUMBER_OF_IMAGES_FOR_MODEL-numberOfImages);
    }

    public void setName(String name) {
        this.name=name;
    }

    public long getId(){
        return id;
    }

    public long getEmployeeNumber() {
        return employeeNumber;
    }

    public String getName() {
        return name;
    }

    public BranchType getBranch() {
        if (branch==null){
            branch=BranchType.UNDEFINED;
        }
        return branch;
    }

    public EmployeeType getType() {
        return type;
    }

}
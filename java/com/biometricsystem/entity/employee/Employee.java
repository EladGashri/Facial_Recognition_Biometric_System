package com.biometricsystem.entity.employee;
import com.biometricsystem.branch.BranchLocation;
import com.biometricsystem.entity.image.ImageFromDatabase;
import com.biometricsystem.security.EmployeeType;
import javax.persistence.Entity;
import javax.persistence.Id;


@Entity
public class Employee{

    @Id
    private long id;
    private long employeeNumber;
    private String name;
    private BranchLocation branch;
    private EmployeeType employeeType;
    private int numberOfImages;
    private Integer modelClass;
    private double modelAccuracy;
    private String imagesDirectoryPath;
    private boolean includedInModel;
    private ImageFromDatabase[] images;
    public final static int MINIMUM_NUMBER_OF_IMAGES_FOR_MODEL=4;

    public Employee(long id, long employeeNumber, String name, String branch, int employeeType, int numberOfImages, double modelAccuracy,Integer modelClass,String imagesDirectoryPath, boolean includedInModel) {
        this.id = id;
        this.employeeNumber = employeeNumber;
        this.name = name;
        this.branch = BranchLocation.getBranchLocationByDatabaseValue(branch);
        this.employeeType = EmployeeType.getEmployeeTypeByDatabaseValue(employeeType);
        this.numberOfImages = numberOfImages;
        this.modelAccuracy = modelAccuracy;
        this.modelClass=modelClass;
        this.imagesDirectoryPath=imagesDirectoryPath;
        this.includedInModel = includedInModel;
    }

    public static Employee getEmployeeFromDocument(org.bson.Document document){
        try {
            return new Employee(document.getInteger("_id"),
                    document.getInteger("employee number"),
                    document.getString("name"),
                    document.getString("branch"),
                    document.getInteger("employee type"),
                    document.getInteger("number of images"),
                    document.getDouble("model accuracy"),
                    document.getInteger("model class"),
                    document.getString("images directory path"),
                    document.getBoolean("included in model"));
        }catch(java.lang.ClassCastException e){
            return new Employee(document.getLong("_id"),
                    document.getLong("employee number"),
                    document.getString("name"),
                    document.getString("branch"),
                    document.getInteger("employee type"),
                    document.getInteger("number of images"),
                    document.getDouble("model accuracy"),
                    document.getInteger("model class"),
                    document.getString("images directory path"),
                    document.getBoolean("included in model"));
        }
    }

    public Employee(long id, long employeeNumber){
        this(id,employeeNumber,null,null,EmployeeType.UNDEFINED.getDatabaseValue(),0,0,null,null,false);
    }

    public void setImages(ImageFromDatabase[] images){
        this.images=images;
    }

    public ImageFromDatabase[] getImages(){
        return images;
    }

    public String getPassword(){
        return String.valueOf(id);
    }

    public String getUsername(){
        return String.valueOf(employeeNumber);
    }

    public void setBranch(BranchLocation branch) {
        this.branch = branch;
    }

    public void setEmployeeType(EmployeeType employeeType) {
        this.employeeType = employeeType;
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

    public int getModelClass() {
        return modelClass;
    }

    @Override
    public boolean equals(Object object){
        if (!(object instanceof Employee)){
            return false;
        }
        Employee employee=(Employee) object;
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

    public BranchLocation getBranch() {
        if (branch==null){
            branch= BranchLocation.UNDEFINED;
        }
        return branch;
    }

    public EmployeeType getEmployeeType() {
        return employeeType;
    }

    public void setImagesDirectoryPath(String imagesDirectoryPath) {
        this.imagesDirectoryPath = imagesDirectoryPath;
    }

    public void setModelClass(Integer modelClass) {
        this.modelClass = modelClass;
    }


    public String getImagesDirectoryPath() {
        return imagesDirectoryPath;
    }

    public double getModelAccuracy() {
        return modelAccuracy;
    }

}
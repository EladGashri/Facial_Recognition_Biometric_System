package com.biometricsystem.entity.employee;
import com.biometricsystem.branch.BranchLocation;
import com.biometricsystem.entity.image.ImageFromDatabase;
import com.biometricsystem.security.EmployeeType;


public class EmployeeDto{

    private long id;
    private long employeeNumber;
    private String name;
    private BranchLocation branch;
    private EmployeeType employeeType;
    private int numberOfImages;
    private Integer modelClass;
    private boolean includedInModel;
    private ImageFromDatabase[] images;


    public EmployeeDto(long id, long employeeNumber, String name, String branch, Integer employeeType, int numberOfImages, int modelClass, boolean includedInModel) {
        this.id=id;
        this.employeeNumber = employeeNumber;
        this.name = name;
        this.branch = BranchLocation.getBranchLocationByDatabaseValue(branch);
        this.employeeType = EmployeeType.getEmployeeTypeByDatabaseValue(employeeType);
        this.numberOfImages = numberOfImages;
        this.modelClass=modelClass;
        this.includedInModel = includedInModel;
    }

    public static EmployeeDto getEmployeeDtoFromDocument(org.bson.Document document){
        try {
            return new EmployeeDto(document.getInteger("_id"),
                    document.getInteger("employee number"),
                    document.getString("name"),
                    document.getString("branch"),
                    document.getInteger("employee type"),
                    document.getInteger("number of images"),
                    document.getInteger("model class"),
                    document.getBoolean("included in model"));
        }catch(java.lang.ClassCastException e){
            return new EmployeeDto(document.getLong("_id"),
                    document.getLong("employee number"),
                    document.getString("name"),
                    document.getString("branch"),
                    document.getInteger("employee type"),
                    document.getInteger("number of images"),
                    document.getInteger("model class"),
                    document.getBoolean("included in model"));
        }
    }

    public long receiveId(){
        return id;
    }

    public int receiveModelClass(){
        return modelClass;
    }

    public void setEmployeeNumber(long employeeNumber) {
        this.employeeNumber = employeeNumber;
    }

    public void setName(String name) {
        this.name = name;
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

    public void setIncludedInModel(boolean includedInModel) {
        this.includedInModel = includedInModel;
    }

    public void setImages(ImageFromDatabase[] images){
        this.images=images;
    }

    public long getEmployeeNumber() {
        return employeeNumber;
    }

    public String getName() {
        return name;
    }

    public BranchLocation getBranch() {
        return branch;
    }

    public EmployeeType getEmployeeType() {
        return employeeType;
    }

    public int getNumberOfImages() {
        return numberOfImages;
    }

    public boolean isIncludedInModel() {
        return includedInModel;
    }

    public ImageFromDatabase[] getImages() {
        return images;
    }

    public int getNumberOfMissingImagesForModel(){
        return Math.max(0, Employee.MINIMUM_NUMBER_OF_IMAGES_FOR_MODEL-numberOfImages);
    }

}
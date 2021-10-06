package com.biometricsystem.api.service;
import com.biometricsystem.api.repository.EmployeesRepository;
import com.biometricsystem.api.repository.ImagesRepository;
import com.biometricsystem.entity.employee.Employee;
import com.biometricsystem.entity.image.ImageFromDatabase;
import com.biometricsystem.entity.image.ImageForIdentification;
import com.biometricsystem.entity.image.UploadedImage;
import com.mongodb.MongoException;
import org.opencv.imgcodecs.Imgcodecs;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.NoSuchElementException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.server.UnsupportedMediaTypeStatusException;


@Service
public class ImagesService {

    @Autowired
    private EmployeeService employeesService;
    @Autowired
    private EmployeesRepository employeesRepository;
    @Autowired
    private ImagesRepository imagesRepository;

    public ImageFromDatabase[] getEmployeeImages(long employeeId){
        return imagesRepository.getEmployeeImagesFromDatabaseAsArray(employeeId);
    }

    public ImageFromDatabase[] getEmployeeImagesByEmployeeNumber(long employeeNumber){
        long employeeId=employeesRepository.getId(employeeNumber);
        return imagesRepository.getEmployeeImagesFromDatabaseAsArray(employeeId);
    }

    public ImageFromDatabase getImage(long employeeNumber, String imageId){
        long employeeId=employeesRepository.getId(employeeNumber);
        return imagesRepository.getImage(employeeId,imageId);
    }

    public String getPath(Employee employee) {
        try {
            int numberOfEmployeeImagesInt = employeesRepository.getNumberOfImages(employee.getId(), employee.getEmployeeNumber());
            String numberOfEmployeeImagesStr;
            if (String.valueOf(numberOfEmployeeImagesInt).length() < Employee.MINIMUM_NUMBER_OF_IMAGES_FOR_MODEL) {
                numberOfEmployeeImagesStr = "0".repeat(Employee.MINIMUM_NUMBER_OF_IMAGES_FOR_MODEL - String.valueOf(numberOfEmployeeImagesInt).length()) + numberOfEmployeeImagesInt;
            } else {
                numberOfEmployeeImagesStr = String.valueOf(numberOfEmployeeImagesInt);
            }
            String employeeImagesPath = employeesRepository.getImagesDirectoryPath(employee.getId(), employee.getEmployeeNumber());
            return employeeImagesPath + "//" + employee.getName() + "_" + numberOfEmployeeImagesStr + ".jpg";
        } catch (MongoException | NullPointerException | NoSuchElementException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean addImage(Long employeeNumber, MultipartFile image) throws MongoException, IOException, UnsupportedMediaTypeStatusException {
        String type = image.getContentType();
        if (UploadedImage.isImageCorrectType(type)){
            UploadedImage uploadedImage = new UploadedImage(image.getBytes());
            uploadedImage.initializeFaceImage();
            if (uploadedImage.wasAFaceDetected()){
                uploadedImage.setPath(image.getOriginalFilename());
                uploadedImage.identify();
                if (saveImageToDatabase(uploadedImage, employeeNumber)) {
                    return true;
                } else {
                    throw new MongoException("couldn't insert image to the database");
                }
            } else {
                return false;
            }
        }else{
            throw new UnsupportedMediaTypeStatusException("Must only transfer jpg, jpeg or png");
        }
    }

    public boolean saveImageToDatabase(ImageForIdentification image){
        return saveImageToDatabase(image,null,null);
    }

    public boolean saveImageToDatabase(ImageForIdentification image, long employeeNumber){
        long employeeId=employeesRepository.getId(employeeNumber);
        return saveImageToDatabase(image,employeeId,employeeNumber);
    }

    public boolean saveImageToDatabase(ImageForIdentification image,Long employeeId, Long employeeNumber) {
        if(image.getEmployee()==null){
            Employee employee= employeesService.getEmployee(employeeId,employeeNumber);
            image.setEmployee(employee);
        }
        image.setPath(getPath(image.getEmployee()));
        boolean inserted=imagesRepository.saveImageToDatabase(image);
        if(inserted){
            employeesService.checkIncludedInModelAfterImageInsertionOrDeletion(image.getEmployee(),1);
        }
        return inserted;
    }

    public boolean deleteImageFromDatabase(long employeeNumber, String imageId) {
        long employeeId=employeesRepository.getId(employeeNumber);
        boolean deleted=imagesRepository.DeleteImageFromDatabase(imageId,employeeId);
        Employee employee=employeesService.getEmployee(employeeId,employeeNumber);
        if(deleted){
            employeesService.checkIncludedInModelAfterImageInsertionOrDeletion(employee,-1);
        }
        return deleted;
    }

    public void saveImageToDirectory(ImageForIdentification image){
        Imgcodecs.imwrite(image.getPath(), image.getValues());
    }

}
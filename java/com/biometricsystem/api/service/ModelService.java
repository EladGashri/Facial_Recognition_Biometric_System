package com.biometricsystem.api.service;
import com.biometricsystem.entity.image.UploadedImage;
import com.biometricsystem.model.FaceClassifier;
import org.opencv.core.Core;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.UnsupportedMediaTypeStatusException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;


@Service
public class ModelService {

    @Autowired
    private FaceClassifier faceClassifier;
    @Autowired
    private ImagesService imagesService;
    public final static String PYTHON_PATH=System.getProperty("user.home")+"\\Anaconda3\\python";
    public final static String SCRIPT_PATH=System.getProperty("user.home")+"\\IdeaProjects\\FacialRecognitionBiometricSystem\\src\\main\\python\\new_model.py";
    public final static String MODELS_DIRECTORY="src\\main\\resources\\models";
    public final static String MODEL_FILE_EXTENSION=".zip";

    public UploadedImage identifyImage(MultipartFile image) throws UnsupportedMediaTypeStatusException, IOException {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        String type = image.getContentType();
        if (UploadedImage.isImageCorrectType(type)) {
            UploadedImage uploadedImage = new UploadedImage(image.getBytes());
            uploadedImage.initializeFaceImage();
            uploadedImage.identify(faceClassifier);
            uploadedImage.setEmployeeImages(imagesService.getEmployeeImages(uploadedImage.getEmployee().getId()));
            return uploadedImage;
        }else{
            throw new UnsupportedMediaTypeStatusException("Must only transfer jpg, jpeg or png");
        }
    }

    public Integer trainModelAndUpdateDatabase(){
        String newModel=MODELS_DIRECTORY+"\\"+LocalDate.now().toString()+MODEL_FILE_EXTENSION;
        ProcessBuilder processBuilder = new ProcessBuilder(PYTHON_PATH,SCRIPT_PATH,newModel);
        //processBuilder.directory(new File(System.getProperty("user.home")+"\\Anaconda3"));
        Process process;
        int exitCode;
        try {
            processBuilder.redirectErrorStream(true);
            process = processBuilder.start();
            BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String inputLine;
            while ((inputLine = input.readLine()) != null) {
                System.out.println(inputLine);
            }
            exitCode = process.waitFor();
        } catch (IOException|InterruptedException e) {
            e.printStackTrace();
            return null;
        }
        faceClassifier.setNewModel(newModel);
        return exitCode;
    }

}

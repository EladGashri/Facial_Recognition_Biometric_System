package com.biometricsystem.api.controller;
import com.biometricsystem.entity.image.ImageDto;
import com.biometricsystem.entity.image.UploadedImage;
import com.biometricsystem.api.service.ModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.UnsupportedMediaTypeStatusException;

import java.io.IOException;


@RestController
@RequestMapping("/model")
public class ModelController{

    @Autowired
    private ModelService modelService;

    @PostMapping("/training")
    public ResponseEntity<String> trainModel(){
        int exitCode=modelService.trainModelAndUpdateDatabase();
        if (exitCode==0){
            System.out.println("The model has been trained");
            return new ResponseEntity<>("The model has been trained", HttpStatus.CREATED);
        }else{
            System.err.println("Python script returned "+exitCode+" exit code. The model has not been trained.");
            return new ResponseEntity<>("Python script returned "+exitCode+" exit code", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/identification")
    public ResponseEntity<?> predictUploadedImage(@RequestParam("image") MultipartFile image) {
        try {
            ImageDto imageDTO = modelService.identifyImage(image);
            return new ResponseEntity<>(imageDTO, HttpStatus.OK);
        }catch (IOException e){
            return new ResponseEntity<>("Could not read image", HttpStatus.BAD_REQUEST);
        }catch (UnsupportedMediaTypeStatusException e){
            return new ResponseEntity<>("Must only transfer jpg, jpeg or png images", HttpStatus.UNSUPPORTED_MEDIA_TYPE);
        }
    }

}
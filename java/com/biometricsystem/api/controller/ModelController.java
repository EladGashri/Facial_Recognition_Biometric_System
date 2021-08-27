package com.biometricsystem.api.controller;
import com.biometricsystem.entity.image.UploadedImage;
import com.biometricsystem.api.service.ModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.UnsupportedMediaTypeStatusException;

import java.io.IOException;


@RestController
@RequestMapping("/model")
public class ModelController{

    @Autowired
    private ModelService modelService;

    @PostMapping("/training")
    public ResponseEntity<UploadedImage> trainModel(){
        int exitCode=modelService.trainModelAndUpdateDatabase();
        if (exitCode==0){
            System.out.println("The model has been trained");
            return ResponseEntity.status(HttpStatus.CREATED).build();
        }else{
            System.err.println("Python script returned "+exitCode+" exit code");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/identification")
    public ResponseEntity<UploadedImage> predictUploadedImage(@RequestParam("image") MultipartFile image) {
        try {
            UploadedImage uploadedImage = modelService.identifyImage(image);
            return ResponseEntity.ok(uploadedImage);
        }catch (IOException e){
            return ResponseEntity.badRequest().build();
        }catch (UnsupportedMediaTypeStatusException e){
            return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).build();
        }
    }

}
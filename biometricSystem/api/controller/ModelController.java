package com.biometricsystem.api.controller;
import com.biometricsystem.image.UploadedImage;
import com.biometricsystem.api.service.ModelService;
import org.opencv.core.Core;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;


@RestController
@RequestMapping("/model")
public class ModelController{

    @Autowired
    private ModelService modelService;

    @PostMapping
    public ResponseEntity<UploadedImage> predictUploadedImage(@RequestParam("image") MultipartFile image) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        String type=image.getContentType();
        System.out.println(type);
        if (type.equals("image/jpg") || type.equals("image/jpeg") || type.equals("image/png")) {
            try {
                UploadedImage uploadedImage = new UploadedImage(image.getBytes());
                modelService.identifyImage(uploadedImage);
                return ResponseEntity.ok(uploadedImage);
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        }else {
            return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).build();
        }
    }

}
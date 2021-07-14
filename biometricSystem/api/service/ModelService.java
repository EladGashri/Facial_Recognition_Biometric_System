package com.biometricsystem.api.service;
import com.biometricsystem.image.FaceRecognitionResult;
import com.biometricsystem.image.ImageForIdentification;
import com.biometricsystem.livefeed.LiveFeed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class ModelService {

    @Autowired
    private LiveFeed liveFeed;

    public void identifyImage(ImageForIdentification image){
        image.setFaceImage();
        image.identify();
        image.setResult();
        if (image.getResult()== FaceRecognitionResult.FACE_RECOGNIZED){
            image.setName(liveFeed.getIdToName());
        }
    }

}
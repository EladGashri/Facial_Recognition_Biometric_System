package com.biometricsystem.image;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;


public class UploadedImage extends ImageForIdentification {

    public UploadedImage(byte[] bytes){
        super(Imgcodecs.imdecode(new MatOfByte(bytes), Imgcodecs.IMREAD_UNCHANGED));
    }

}

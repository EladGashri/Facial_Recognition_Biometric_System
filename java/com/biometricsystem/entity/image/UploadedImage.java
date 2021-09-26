package com.biometricsystem.entity.image;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;


public class UploadedImage extends ImageForIdentification {

    public UploadedImage(byte[] frameValuesAsBytesArray){
        super(Imgcodecs.imdecode(new MatOfByte(frameValuesAsBytesArray),Imgcodecs.IMREAD_UNCHANGED));
    }

    public static boolean isImageCorrectType(String type) {
        return (type.equals("image/jpg") || type.equals("image/jpeg") || type.equals("image/png"));
    }

}
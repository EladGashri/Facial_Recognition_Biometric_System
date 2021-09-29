package com.biometricsystem.entity.image;
import com.biometricsystem.api.service.EmployeeService;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;


public class CapturedFrame extends ImageForIdentification {
    private InetAddress address;
    private Integer port;
    private byte[] faceImageAsByteArray;

    public CapturedFrame(Mat frameValues){
        super(frameValues);
    }

    public CapturedFrame(byte[] faceImageAsByteArray,InetAddress address,Integer port){
        super(null);
        this.faceImageAsByteArray=faceImageAsByteArray;
        setFaceImageFromByteArray();
        values=faceImage;
        this.address=address;
        this.port=port;
        faceDetected=true;
    }

    public static CapturedFrame getCapturedFrameFromWrapper(CapturedFrameWrapper wrapper){
        byte[] faceImageAsByteArray=CapturedFrame.getFaceImageFromCompressedFaceImage(wrapper.getFaceImageAsCompressedByteArray());
        CapturedFrame frame=new CapturedFrame(faceImageAsByteArray,null,null);
        frame.setResult(wrapper.getResult());
        frame.setRecognitionProbability(wrapper.getRecognitionProbability());
        if (wrapper.getModelClass()!=null) {
            frame.setEmployee(wrapper.getModelClass());
        }
        return frame;
    }

    @Override
    public byte[] getFaceImageAsByteArray(){
        if (faceImageAsByteArray==null) {
            faceImageAsByteArray=super.getFaceImageAsByteArray();
        }
        return faceImageAsByteArray;
    }

    @Override
    public Mat getFaceImage() {
        setFaceImageFromByteArray();
        return faceImage;
    }

    private void setFaceImageFromByteArray(){
        if (faceImage==null){
            faceImage=Imgcodecs.imdecode(new MatOfByte(faceImageAsByteArray),Imgcodecs.IMREAD_UNCHANGED);
        }
    }

    public void setRecognitionProbability(double recognitionProbability){
        this.recognitionProbability=recognitionProbability;
    }

    public byte[] getFaceImageAsCompressedByteArray() {
        Deflater deflater = new Deflater();
        byte[] faceImage=getFaceImageAsByteArray();
        deflater.setInput(faceImage);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(faceImage.length);
        deflater.finish();
        byte[] buffer = new byte[1024];
        int count;
        while (!deflater.finished()){
            count = deflater.deflate(buffer);
            outputStream.write(buffer, 0, count);
        }
        try{
            outputStream.close();
        }catch (IOException e){
            e.printStackTrace();
        }
        return outputStream.toByteArray();
    }

    public static byte[] getFaceImageFromCompressedFaceImage(byte[] compressedFaceImage){
        Inflater inflater = new Inflater();
        inflater.setInput(compressedFaceImage);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(compressedFaceImage.length);
        byte[] buffer = new byte[1024];
        try{
            while (!inflater.finished()){
                int count = inflater.inflate(buffer);
                outputStream.write(buffer, 0, count);
            }
            outputStream.close();
        }catch (IOException| DataFormatException e) {
            e.printStackTrace();
        }
        return outputStream.toByteArray();
    }

    public InetAddress getAddress(){
        return address;
    }

    public int getPort(){
        return port;
    }

    public void setResult(FaceRecognitionResult result){
        this.result=result;
    }

}
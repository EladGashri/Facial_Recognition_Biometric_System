package com.biometricsystem.livefeed.server;
import com.biometricsystem.entity.image.CapturedFrame;
import com.biometricsystem.entity.image.CapturedFrameWrapper;
import org.opencv.core.Core;
import java.io.IOException;
import java.net.*;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;


public class FrameAnalyzer implements Runnable{

    private final MultiThreadedQueue<DatagramPacket> receivedPacketsQueue;
    private final MultiThreadedQueue<CapturedFrame> framesQueue;
    ByteArrayOutputStream byteArrayOutputStream;
    ObjectOutputStream objectOutputStream;

    public FrameAnalyzer(MultiThreadedQueue<DatagramPacket> receivedPacketsQueue, MultiThreadedQueue<CapturedFrame> framesQueue) {
        this.receivedPacketsQueue=receivedPacketsQueue;
        this.framesQueue=framesQueue;
    }

    @Override
    public void run(){
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        DatagramPacket currentPacket;
        CapturedFrame currentFrame;
        try {
            DatagramSocket socket=new DatagramSocket();
            while (true) {
                byteArrayOutputStream = new ByteArrayOutputStream();
                objectOutputStream=new ObjectOutputStream(byteArrayOutputStream);
                currentPacket = receivedPacketsQueue.dequeue();
                currentFrame = getCapturedFrameFromPacket(currentPacket);
                currentFrame.identify();
                sendPacket(socket, currentFrame);
                framesQueue.enqueue(currentFrame);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendPacket(DatagramSocket socket, CapturedFrame currentFrame) {
        try {
            objectOutputStream.writeObject(new CapturedFrameWrapper(currentFrame));
            objectOutputStream.flush();
            byte[] bytesToSend=byteArrayOutputStream.toByteArray();
            byteArrayOutputStream.flush();
            socket.send(new DatagramPacket(bytesToSend, bytesToSend.length, currentFrame.getAddress(), currentFrame.getPort()));
            byteArrayOutputStream.reset();
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    private CapturedFrame getCapturedFrameFromPacket(DatagramPacket packet){
        byte[] faceImage=CapturedFrame.getFaceImageFromCompressedFaceImage(packet.getData());
        return new CapturedFrame(faceImage,packet.getAddress(),packet.getPort());
    }

}
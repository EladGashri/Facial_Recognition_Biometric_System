package com.biometricsystem.livefeed.client;
import com.biometricsystem.branch.BranchLocation;
import com.biometricsystem.entity.image.CapturedFrame;
import com.biometricsystem.livefeed.LiveFeedManager;
import com.biometricsystem.livefeed.client.gui.LiveFeedGUI;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import java.io.IOException;
import java.net.*;
import java.util.concurrent.atomic.AtomicBoolean;


public class LiveFeedStand {

    private final BranchLocation branch;
    private VideoCapture capture;
    private Mat frame;
    private AtomicBoolean streaming;
    private DatagramSocket socket;

    public LiveFeedStand(BranchLocation branch) {
        this.branch=branch;
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        nu.pattern.OpenCV.loadLocally();
        capture=new VideoCapture(0);
        frame=new Mat();
        streaming=new AtomicBoolean(true);
        try{
            socket = new DatagramSocket();
        }catch(SocketException e){
            e.printStackTrace();
            socket=null;
        }
    }

    public void startLiveFeed() {
        new LiveFeedGUI(this).start();
        CapturedFrame currentFrame;
        DatagramPacket packet=new DatagramPacket(new byte[LiveFeedManager.MAX_PACKET_SIZE], LiveFeedManager.MAX_PACKET_SIZE, LiveFeedManager.getServerAddress(), LiveFeedManager.LIVE_FEED_SERVER_PORT);
        System.out.println("Live feed is starting");
        while (capture.isOpened() && capture.read(frame) && !frame.empty()){
            if(!streaming.get()){
                synchronized (this) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            currentFrame=new CapturedFrame(frame);
            currentFrame.initializeFaceImage();
            if (currentFrame.wasAFaceDetected()){
                System.out.println("A face was detected in the live feed");
                sendPacket(packet,currentFrame);
            }
            LiveFeedManager.callGarbageCollector(currentFrame);
        }
        if (!capture.isOpened() || !capture.read(frame)) {
            System.err.println("No access to camera");
        }
    }

    private void sendPacket(DatagramPacket packet, CapturedFrame currentFrame){
        byte[] faceImage=currentFrame.getFaceImageAsCompressedByteArray();
        packet.setData(faceImage);
        packet.setLength(faceImage.length);
        try {
            socket.send(packet);
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public synchronized void pauseLiveFeed(){
        streaming.set(false);
    }

    public synchronized void resumeLiveFeed(){
        streaming.set(true);
        synchronized (this) {
            notify();
        }
    }

    public boolean isStreaming(){
        return streaming.get();
    }

    public BranchLocation getBranch(){
        return branch;
    }

    public DatagramSocket getSocket(){
        return socket;
    }

}
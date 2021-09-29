package com.biometricsystem.livefeed;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.io.File;


public final class LiveFeedManager {

    public final static String LIVE_FEED_SERVER_ADDRESS="localhost";
    public final static int LIVE_FEED_SERVER_PORT = 10000;
    public final static File FACES_RECOGNIZED_DIRECTORY=new File("src\\main\\resources\\static\\images\\faces recognized");
    public final static double FACE_RECOGNITION_THRESHOLD=0.8;
    public final static int MAX_PACKET_SIZE=65507;

    static {
        if (!FACES_RECOGNIZED_DIRECTORY.exists()) {
            FACES_RECOGNIZED_DIRECTORY.mkdir();
        }
    }

    public static InetAddress getServerAddress(){
        try{
            return InetAddress.getByName(LiveFeedManager.LIVE_FEED_SERVER_ADDRESS);
        }catch (UnknownHostException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void callGarbageCollector(Object... objects){
        for(Object object:objects){
            object=null;
        }
        System.gc();
    }

}
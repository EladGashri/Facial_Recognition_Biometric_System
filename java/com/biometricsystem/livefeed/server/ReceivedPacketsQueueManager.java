package com.biometricsystem.livefeed.server;
import com.biometricsystem.livefeed.LiveFeedManager;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;


public class ReceivedPacketsQueueManager implements Runnable {

    private final MultiThreadedQueue<DatagramPacket> receivedPacketsQueue;

    public ReceivedPacketsQueueManager(MultiThreadedQueue<DatagramPacket> receivedPacketsQueue) {
        this.receivedPacketsQueue = receivedPacketsQueue;
    }

    @Override
    public void run() {
        try {
            DatagramSocket socket = new DatagramSocket(LiveFeedManager.LIVE_FEED_SERVER_PORT);
            DatagramPacket packet = new DatagramPacket(new byte[LiveFeedManager.MAX_PACKET_SIZE], LiveFeedManager.MAX_PACKET_SIZE);
            while (true) {
                socket.receive(packet);
                receivedPacketsQueue.enqueue(packet);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
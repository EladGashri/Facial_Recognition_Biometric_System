package com.biometricsystem.livefeed.server;
import com.biometricsystem.entity.image.CapturedFrame;

import java.net.DatagramPacket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class LiveFeedThreadPoolManager {
    private final static int ANALYZER_NUMBER_OF_THREADS=5;
    private final static int REGISTER_NUMBER_OF_THREADS=5;
    private final static int QUEUE_MANAGER_NUMBER_OF_THREADS=1;
    private final static int THREAD_POOL_SIZE=ANALYZER_NUMBER_OF_THREADS+REGISTER_NUMBER_OF_THREADS+QUEUE_MANAGER_NUMBER_OF_THREADS;
    private final ExecutorService executor;
    private MultiThreadedQueue<DatagramPacket> receivedPacketsQueue;
    private MultiThreadedQueue<CapturedFrame> framesQueue;

    public LiveFeedThreadPoolManager() {
        executor=Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        receivedPacketsQueue=new MultiThreadedQueue<>();
        framesQueue=new MultiThreadedQueue<>();
    }

    public void initializeThreadPool(){
        for (int i = 0; i < ANALYZER_NUMBER_OF_THREADS; i++) {
            executor.execute(new FrameAnalyzer(receivedPacketsQueue,framesQueue));
        }
        for (int i = 0; i < QUEUE_MANAGER_NUMBER_OF_THREADS; i++) {
            executor.execute(new ReceivedPacketsQueueManager(receivedPacketsQueue));
        }
        for (int i = 0; i < REGISTER_NUMBER_OF_THREADS; i++) {
            executor.execute(new EntryRegister(framesQueue));
        }
        executor.shutdown();
    }

}
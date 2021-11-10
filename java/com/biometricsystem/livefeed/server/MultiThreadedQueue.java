package com.biometricsystem.livefeed.server;
import java.util.ArrayList;


public class MultiThreadedQueue <T> extends ArrayList<T> {

    public synchronized void enqueue(T toAdd){
        add(toAdd);
        notify();
    }

    public synchronized T dequeue(){
        while(size()==0){
            try {
                wait();
            }catch (InterruptedException e){
                e.printStackTrace();
            }
        }
        return remove(0);
    }

}

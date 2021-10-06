package com.biometricsystem.livefeed.client.gui;
import com.biometricsystem.entity.image.CapturedFrame;
import com.biometricsystem.entity.image.CapturedFrameWrapper;
import com.biometricsystem.entity.image.FaceRecognitionResult;
import com.biometricsystem.livefeed.LiveFeedManager;
import com.biometricsystem.livefeed.client.LiveFeedStand;
import com.biometricsystem.branch.BranchGate;
import com.biometricsystem.branch.BranchGateSetter;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.UnknownHostException;


public class LiveFeedGUIController {
    private LiveFeedStand stand;
    private LiveFeedGUIView view;
    private LiveFeedGUIModel model;
    private DatagramSocket socket;
    private boolean identifiedById;
    private Timer timer;

    public LiveFeedGUIController(LiveFeedStand stand, LiveFeedGUIView view, LiveFeedGUIModel model) {
        this.stand = stand;
        this.view = view;
        this.model = model;
        socket=stand.getSocket();
        identifiedById=false;
        seViewActionListeners();
        timer=new Timer(LiveFeedGUI.GUI_VISIBILITY_DURATION, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                timer.stop();
                resume();
            }
        });
    }

    public void startGui() {
        byte[] buffer = new byte[LiveFeedManager.MAX_PACKET_SIZE];
        try {
            DatagramPacket packet = new DatagramPacket(buffer,LiveFeedManager.MAX_PACKET_SIZE);
            CapturedFrameWrapper wrapper;
            CapturedFrame currentFrame;
            ByteArrayInputStream byteArrayInputStream;
            ObjectInputStream objectInputStream;
            while (true) {
                socket.receive(packet);
                byteArrayInputStream = new ByteArrayInputStream(packet.getData());
                objectInputStream = new ObjectInputStream(byteArrayInputStream);
                wrapper = (CapturedFrameWrapper) objectInputStream.readObject();
                currentFrame=CapturedFrame.getCapturedFrameFromWrapper(wrapper);
                model.setCurrentFrame(currentFrame);
                LiveFeedManager.callGarbageCollector(currentFrame,byteArrayInputStream,objectInputStream);
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void setPanelImageAndTitle(byte[] frameAsByteArray, String title){
        view.setTitle(title);
        view.setPanelIcon(new ImageIcon(frameAsByteArray));
    }

    public void openGate(){
        try {
            BranchGate gate = BranchGate.getAddressToGate().get(stand.getBranch().getAddress());
            new Thread(new BranchGateSetter(gate)).start();
        }catch (UnknownHostException e){
            e.printStackTrace();
        }
    }

    public void pause(){
        stand.pauseLiveFeed();
        timer.start();
    }

    public void resume(){
        view.clearPanel();
        view.setTitle(LiveFeedGUIView.TITLE);
        view.hideIdentificationComponents();
        stand.resumeLiveFeed();
    }

    public void setRecognizedFace(byte[] frameAsByteArray, String title){
        setPanelImageAndTitle(frameAsByteArray,title);
        view.makeCancelButtonVisible();
        pause();
    }

    public void setIdentificationComponents(byte[] frameAsByteArray,String title){
        view.setTitle(title);
        view.setPanelIcon(new ImageIcon(frameAsByteArray));
        view.showIdentificationComponents();
        pause();
    }

    public int[] getIdAndEmployeeNumber(){
        if (identifiedById) {
            identifiedById=false;
            int identifiedId = Integer.parseInt(view.getIdentifiedId());
            int identifiedEmployeeNumber = Integer.parseInt(view.getIdentifiedEmployeeNumber());
            view.resetLabels();
            return new int[]{identifiedId, identifiedEmployeeNumber};
        }else{
            return null;
        }
    }

    public void seViewActionListeners(){
        view.setOKButtonActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                timer.stop();
                identifiedById=true;
                resume();
            }
        });
        view.setCancelButtonActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                timer.stop();
                resume();
            }
        });

    }

}
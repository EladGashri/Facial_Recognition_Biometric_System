package com.biometricsystem.livefeed.client.gui;
import com.biometricsystem.livefeed.client.LiveFeedStand;


public class LiveFeedGUI extends Thread {

    private final LiveFeedStand stand;
    public final static int GUI_VISIBILITY_DURATION=10000; //10 seconds in milliseconds
    public LiveFeedGUI(LiveFeedStand stand) {
        this.stand = stand;
    }

    @Override
    public void run() {
        setPriority(Thread.MAX_PRIORITY);
        LiveFeedGUIView view = new LiveFeedGUIView();
        LiveFeedGUIModel model = new LiveFeedGUIModel();
        LiveFeedGUIController controller = new LiveFeedGUIController(stand, view, model);
        model.setController(controller);
        controller.startGui();
    }

}
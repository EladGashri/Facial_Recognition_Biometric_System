package com.biometricsystem.livefeed.client;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import com.biometricsystem.branch.BranchLocation;
import org.slf4j.LoggerFactory;


public class LiveFeedClient {

    public static void main(String[] args) {
        LoggerContext logger=(LoggerContext) LoggerFactory.getILoggerFactory();
        logger.getLogger("org.mongodb.driver").setLevel(Level.ERROR);
        logger.getLogger("ai.djl").setLevel(Level.ERROR);
        System.setProperty("java.awt.headless", "false");
        LiveFeedStand liveFeedStand = new LiveFeedStand(BranchLocation.TA);
        liveFeedStand.startLiveFeed();
    }

}
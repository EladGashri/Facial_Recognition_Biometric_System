package com.biometricsystem.livefeed.server;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import com.biometricsystem.model.FaceClassifier;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;


public class LiveFeedServer {

    public static void main(String[] args) {
        LoggerContext logger=(LoggerContext) LoggerFactory.getILoggerFactory();
        logger.getLogger("org.mongodb.driver").setLevel(Level.ERROR);
        logger.getLogger("ai.djl").setLevel(Level.ERROR);
        System.setProperty("java.awt.headless", "false");
        LiveFeedThreadPoolManager poolsGenerator = new LiveFeedThreadPoolManager();
        poolsGenerator.initializeThreadPool();
    }

}
package com.biometricsystem;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import com.biometricsystem.livefeed.LiveFeedManager;
import com.biometricsystem.livefeed.server.LiveFeedThreadPoolManager;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;


@ComponentScan("com.biometricsystem.*")
@SpringBootApplication
public class BiometricSystem {

    private final static String API_ADDRESS = LiveFeedManager.LIVE_FEED_SERVER_ADDRESS;
    private final static String API_PORT = "8080";
    public final static String API_HOST = "http://"+API_ADDRESS+":"+API_PORT;

    public static void main(String[] args){
        LoggerContext logger=(LoggerContext) LoggerFactory.getILoggerFactory();
        logger.getLogger("org.mongodb.driver").setLevel(Level.ERROR);
        logger.getLogger("ai.djl").setLevel(Level.ERROR);
        logger.getLogger("org.apache").setLevel(Level.ERROR);
        System.setProperty("java.awt.headless", "false");
        ConfigurableApplicationContext app=SpringApplication.run(BiometricSystem.class, args);
        LiveFeedThreadPoolManager poolsGenerator = new LiveFeedThreadPoolManager();
        poolsGenerator.initializeThreadPool();
    }

}

package com.biometricsystem;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class BiometricSystem {

    public static void main(String[] args) {
        LoggerContext logger=(LoggerContext) LoggerFactory.getILoggerFactory();
        logger.getLogger("org.mongodb.driver").setLevel(Level.ERROR);
        logger.getLogger("ai.djl").setLevel(Level.ERROR);
        System.setProperty("java.awt.headless", "false");
        ConfigurableApplicationContext app=SpringApplication.run(BiometricSystem.class, args);
    }
}

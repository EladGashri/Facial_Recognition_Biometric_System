package com.biometricsystem.model;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class ModelUpdateScheduler {

    @Autowired
    private ModelUpdater modelUpdater;

    public ModelUpdateScheduler(ModelUpdater modelUpdater) {
        this.modelUpdater=modelUpdater;
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(modelUpdater,12,24, TimeUnit.HOURS);
    }

}
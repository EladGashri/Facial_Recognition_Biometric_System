package com.biometricsystem.model;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class ModelUpdateScheduler {

    private static final int INITIAL_DELAY_HOURS = 0;
    private static final int FIXED_RATE_HOURS = 24;

    @Autowired
    private ModelUpdater modelUpdater;

    public ModelUpdateScheduler(ModelUpdater modelUpdater) {
        this.modelUpdater=modelUpdater;
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(modelUpdater,INITIAL_DELAY_HOURS,FIXED_RATE_HOURS, TimeUnit.HOURS);
    }

}

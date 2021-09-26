package com.biometricsystem.database;
import org.bson.Document;

public class TotalTime extends Time{

    public final static int SECONDS_IN_MINUTE=60;
    public final static int MINUTES_IN_HOUR=60;

    public TotalTime(int hour,int minute,int second){
        super(hour,minute,second);
    }

    @Override
    public Document castToDocument(){
        return new Document("hours", hour).append("minutes",minute).append("seconds",second);
    }

    public void add(TotalTime totalTime){
        hour+=totalTime.getHour()+((minute+totalTime.getMinute())/MINUTES_IN_HOUR);
        minute=((minute+totalTime.getMinute())%MINUTES_IN_HOUR)+((second+totalTime.getSecond())/SECONDS_IN_MINUTE);
        second=(second+totalTime.getSecond())%SECONDS_IN_MINUTE;
    }

}

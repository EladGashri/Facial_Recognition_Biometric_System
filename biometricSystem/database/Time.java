package com.biometricsystem.database;
import org.bson.Document;


public class Time {
    private int hour;
    private int minute;
    private int second;

    public Time(int hour,int minute,int second) {
        this.hour=hour;
        this.minute=minute;
        this.second=second;
    }

    public int getHour(){
        return hour;
    }

    public int getMinute() {
        return minute;
    }

    public int getSecond() {
        return second;
    }

    public Time getTimeDifference(Time exitTime) throws TimeException {
        if (!this.checkIfEntryBeforeExit(exitTime)){
            throw new TimeException("entry time must be before exit time");
        }
        int subtractFromHours=(exitTime.getMinute() < minute)? 1 : 0;
        int totalHours=exitTime.getHour() - hour - subtractFromHours;

        int subtractFromMinutes=(exitTime.getSecond() < second)? 1 : 0;
        int totalMinutes=exitTime.getMinute() - minute - subtractFromMinutes;
        if (totalMinutes<0){
            totalMinutes+=60;
        }
        int totalSeconds=exitTime.getSecond() - second;
        if (totalSeconds<0){
            totalSeconds+=60;
        }
        return new Time(totalHours,totalMinutes,totalSeconds);
    }

    public boolean checkIfEntryBeforeExit(Time exitTime){
        if ((hour>exitTime.getHour()) ||
                (hour==exitTime.getHour() && minute>exitTime.getMinute()) ||
                (hour==exitTime.getHour() && minute==exitTime.getMinute() && second>exitTime.getSecond())){
            return false;
        }else{
            return true;
        }
    }

    public Document getDocument(boolean total){
        if (total){
            return new Document("hours", hour).append("minutes",minute).append("seconds",second);
        }else{
            return new Document("hour", hour).append("minute", minute).append("second", second);
        }
    }

    public static class TimeException extends Exception{
        public TimeException(String message){
            super();
        }
    }


}
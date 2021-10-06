package com.biometricsystem.database;
import org.bson.Document;


public class Time {

    protected int hour;
    protected int minute;
    protected int second;

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

    public TotalTime getTotalTimeDifference(Time exitTime) throws TimeException {
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
        return new TotalTime(totalHours,totalMinutes,totalSeconds);
    }

    public static Time parse(String timeString){
        String[] timeStringArray=timeString.split("-");
        return new Time(Integer.parseInt(timeStringArray[0]),Integer.parseInt(timeStringArray[1]),Integer.parseInt(timeStringArray[2]));
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

    public Document castToDocument(){
        return new Document("hour", hour).append("minute", minute).append("second", second);
    }

    public static class TimeException extends Exception{
        public TimeException(String message){
            super(message);
        }
    }

}
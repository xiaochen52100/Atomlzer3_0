package com.example.atomlzer30;

public class TaskData {
    private double progess;
    private int lastTime;

    public void setLastTime(int lastTime) {
        this.lastTime = lastTime;
    }

    public void setProgess(double progess) {
        this.progess = progess;
    }

    public int getLastTime() {
        return lastTime;
    }

    public double getProgess() {
        return progess;
    }
}

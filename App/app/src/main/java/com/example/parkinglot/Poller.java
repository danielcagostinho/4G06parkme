package com.example.parkinglot;

import java.util.Timer;
import java.util.TimerTask;

public class Poller {

    private long interval;
    private TimerTask task;

    private Timer timer;
    public Poller(Runnable scheduledTask, long intervalTime) {
        this.interval = intervalTime;
        task = new TimerTask() {

            @Override
            public void run() {
                scheduledTask.run();
            }
        };

        timer = new Timer();
        StartPoll();
    }

    public void StartPoll() {
        timer.schedule(task, interval);
    }

    public void StopPoll() {
        timer.cancel();
        task.cancel();
    }



}

package org.example.network;

import java.util.concurrent.atomic.AtomicInteger;

public class HeartBeat extends Thread {
    private AtomicInteger seconds;
    ApiActionHelper apiActionHelper;

    public HeartBeat() {
        super();
        apiActionHelper = ApiActionHelper.getInstance();
        seconds = new AtomicInteger(0);
        apiActionHelper.setAction("heart beat received", () -> {
            seconds.setPlain(0);
        });
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                break;
            }
            if (seconds.incrementAndGet() == 60) break;
        }
        apiActionHelper.performAction("shutdown");
    }
}

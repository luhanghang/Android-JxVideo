package com.bsht;

import com.bsht.net.Callback;

public class TimerTask extends java.util.TimerTask {
    private Callback callback;

    public TimerTask(Callback callback) {
        super();
        this.callback = callback;
    }

    public void run() {
        callback.keepAlive();
    }
}

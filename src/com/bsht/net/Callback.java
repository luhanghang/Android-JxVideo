package com.bsht.net;

public interface Callback {
    public void parseCommand(byte[] response);
    public void keepAlive();
    public void playAudio(byte[] audioData, int length);
    public void log(Object msg);
}

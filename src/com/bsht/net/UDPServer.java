package com.bsht.net;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class UDPServer implements Runnable {
    private Callback callback;
    private DatagramSocket socket;
    DatagramPacket in;
    private boolean run = true;

    public UDPServer(int port, byte[] block ,Callback callback) {
        this.callback = callback;
        try {
            socket = new DatagramSocket(port);
            in = new DatagramPacket(block, block.length);
        } catch (Exception e) {
            callback.log("UDPServer Error:" + e.getMessage());
        }
        Thread thread = new Thread(this);
        thread.start();
    }

    public void send(DatagramPacket sendPacket) {
        try {
            socket.send(sendPacket);
        } catch (Exception e) {
            callback.log("UDPServer send error:" + e.getMessage());
        }
    }

    public void destroy() {
        run = false;
        socket.close();
        socket = null;
    }

    public void run() {
        try {
            while (run) {
                socket.receive(in);
                callback.playAudio(in.getData(), in.getLength());
            }
        } catch (Exception e) {
            callback.log("UDPServer Receive Error:" + e.getMessage());
        }
    }
}
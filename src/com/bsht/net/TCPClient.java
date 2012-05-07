package com.bsht.net;

import java.io.*;
import java.net.*;

public class TCPClient implements Runnable {
    private Socket clientSocket;
    private OutputStream dos;
    private InputStream is;
    private Callback callback;
    private byte[] response = new byte[256];
    private boolean run = true;

    public TCPClient(String host, int port, Callback callback) {
        try {
            clientSocket = new Socket(host, port);
            clientSocket.setSoTimeout(1000);
            dos = clientSocket.getOutputStream();
            is = clientSocket.getInputStream();
            this.callback = callback;
            Thread thread = new Thread(this);
            thread.start();
        } catch (Exception e) {
            callback.log("TCPClient Error:" + e.getMessage());
        }
    }

    public void send(byte[] data) {
        try {
            dos.write(data, 0, data.length);
        } catch (Exception e) {
            callback.log("TCPClient send Error:" + e.getMessage());
        }
    }

    public void destroy() {
        try {
            run = false;
            clientSocket.close();
        } catch (Exception e) {
            if (callback != null)
                callback.log("TCPClient destroy Error:" + e.getMessage());
        }
        clientSocket = null;
    }

    public void run() {
        while (run) {
            try {
                int size = is.read(response);
                if (size > 0) {
                    byte[] response_data = new byte[size];
                    System.arraycopy(response, 0, response_data, 0, size);
                    callback.parseCommand(response_data);
                }
                Thread.sleep(100);
            } catch (Exception e) {
                //callback.log("TCPClient run Error:" + e.getMessage());
            }
        }
    }
//    public static byte[] send(String host, int port, byte[] data) throws Exception {
//        Socket clientSocket = new Socket(host, port);
//        clientSocket.setSoTimeout(1000);
//        OutputStream dos = clientSocket.getOutputStream();
//        InputStream is = clientSocket.getInputStream();
//        dos.write(data, 0, data.length);
//        byte[] response = new byte[65535];
//        int size = is.read(response);
//        clientSocket.close();
//        byte[] response_data;
//        if (size > 0) {
//            response_data = new byte[size];
//            System.arraycopy(response, 0, response_data, 0, size);
//            return response_data;
//        }
//        return null;
//    }
}

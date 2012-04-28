package com.bsht.net;

import java.io.*;
import java.net.*;

public class TCPClient {
    public static byte[] send(String host, int port, byte[] data) throws Exception {
        Socket clientSocket = new Socket(host, port);
        OutputStream dos = clientSocket.getOutputStream();
        InputStream is = clientSocket.getInputStream();
        dos.write(data, 0, data.length);
        byte[] response = new byte[65535];
        int size = is.read(response);
        clientSocket.close();
        byte[] response_data;
        if (size > 0) {
            response_data = new byte[size];
            System.arraycopy(response, 0, response_data, 0, size);
            return response_data;
        }
        return null;
    }
}

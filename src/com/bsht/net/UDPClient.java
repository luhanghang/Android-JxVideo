package com.bsht.net;

import java.io.IOException;
import java.net.*;

public class UDPClient {
    private static final int DEFAULT_RECEIVE_BUFFER_SIZE = 8192;
    private static final int DEFAULT_SO_TIMEOUT = 3000;

    private SocketAddress serverSocketAddress;
    private byte[] sendData;
    private DatagramPacket output;

    private int receiveBufferSize; // in bytes
    private DatagramSocket ds;

    public UDPClient(String serverAddress, int serverPort) throws SocketException {
        this(serverAddress, serverPort, new byte[1], DEFAULT_RECEIVE_BUFFER_SIZE, DEFAULT_SO_TIMEOUT);
    }

    public UDPClient(String serverAddress, int serverPort, byte[] sendData) throws SocketException {
        this(serverAddress, serverPort, sendData, DEFAULT_RECEIVE_BUFFER_SIZE, DEFAULT_SO_TIMEOUT);
    }

    public UDPClient(String serverAddress, int serverPort, byte[] sendData, int receiveBufferSize)
            throws SocketException {
        this(serverAddress, serverPort, sendData, receiveBufferSize, DEFAULT_SO_TIMEOUT);
    }

    public UDPClient(String serverAddress, int serverPort, byte[] sendData, int receiveBufferSize, int timeout)
            throws SocketException {
        SocketAddress sa = new InetSocketAddress(serverAddress, serverPort);
        this.serverSocketAddress = sa;
        this.sendData = sendData;
        this.output = new DatagramPacket(sendData, sendData.length, sa);

        this.receiveBufferSize = receiveBufferSize;
        this.ds = new DatagramSocket(0);
        this.ds.setSoTimeout(timeout);
    }

    public void setServerSocketAddress(SocketAddress serverSocketAddress) {
        this.serverSocketAddress = serverSocketAddress;
    }

    public void setSendData(byte[] sendData) {
        this.sendData = sendData;
    }

    public byte[] sendAndReceive() throws IOException {
        send();
        return receive();
    }

    public void send() throws IOException {
        output.setSocketAddress(serverSocketAddress);
        output.setData(sendData);
        output.setLength(sendData.length);

        ds.connect(serverSocketAddress);
        ds.send(output);
    }

    public byte[] receive() throws IOException {
        byte[] response = null;
        DatagramPacket input = new DatagramPacket(new byte[receiveBufferSize], receiveBufferSize);
        // next line blocks until the response is received
        ds.receive(input);
        int numBytes = input.getLength();
        response = new byte[numBytes];
        System.arraycopy(input.getData(), 0, response, 0, numBytes);

        // may return null
        return response;
    }

    public void close() {
        this.ds.close();
    }
}
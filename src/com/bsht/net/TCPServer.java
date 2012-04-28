package com.bsht.net;

import com.bsht.Utils;

import java.net.ServerSocket;
import java.net.Socket;

class Command {
    static short command = 0x0001;
    static short method = 0;
    static short client_id = 0;
    static long session_id = 0;
    static short length = 0;
    static int cmd_size = 12;

    public static byte[] get_command() {
        byte[] cmd = new byte[cmd_size];
        Utils.putShort(cmd, command, 0);
        Utils.putShort(cmd, method, 2);
        Utils.putShort(cmd, client_id, 4);
        Utils.putLong(cmd, session_id, 6);
        Utils.putShort(cmd, length, 10);
        return cmd;
    }

    static public void parse_reply(byte[] response) {
        command = Utils.getShort(response, 4);
        method = Utils.getShort(response, 6);
        client_id = Utils.getShort(response, 8);
        session_id = Utils.getLong(response, 10);
        length = Utils.getShort(response, 14);
    }

    static public String to_string() {
        return "Command\n-------------\ncommand:" + command + "\nmethod:" + method + "\nclient_id:" + client_id + "\nsession_id:" + session_id;
    }
}

class Packet {
    static short version = 0x01;
    static short size;    //all_size
    static int packet_size = 4;

    public static byte[] get_package() {
        byte[] p = new byte[packet_size];
        Utils.putShort(p, version, 0);
        Utils.putShort(p, size, 2);
        return p;
    }

    static public void parse_reply(byte[] response) {
        version = Utils.getShort(response, 0);
        size = Utils.getShort(response, 2);
    }

    static public String to_string() {
        return "Packet\n-------------\nversion:" + version + "\nsize:" + size;
    }
}

class Register {
    static long address = 0;
    static short port = 0;
    static short uuid_size; //length of imei
    static int register_size = 8;

    public static byte[] get_register() {
        byte[] reg = new byte[register_size];
        Utils.putLong(reg, address, 0);
        Utils.putShort(reg, port, 4);
        Utils.putShort(reg, uuid_size, 6);
        return reg;
    }
}

class Register_Reply {
    static byte result; //0 success ,non 0 failed
    static byte level;  //0
    static short client_id; //client_id
    static long session_id;  //session_id

    static public void parse_reply(byte[] response) {
        result = response[16];
        level = (byte) (response[17] & 0xff >> 0);
        client_id = Utils.getShort(response, 18);
        session_id = Utils.getLong(response, 20);
    }

    static public String to_string() {
        return "Reply\n--------------\nresult:" + result + "\nlevel:" + level + "\nclient_id:" + client_id + "\nsession_id:" + session_id;
    }
}

class Frame_Header {
    static long session_id;
    static short frame_size;
    static short frame_type; // 1 key frame
    static long frame_seq;

    public static byte[] get_frame_header() {
        byte[] header = new byte[12];
        Utils.putLong(header, session_id, 0);
        Utils.putShort(header, frame_size, 4);
        Utils.putShort(header, frame_type, 6);
        Utils.putLong(header, frame_seq, 8);
        return header;
    }
}

public class TCPServer implements Runnable {
    public void run() {
        try {
            ServerSocket s = new ServerSocket(8765);
            Socket connectionSocket = s.accept();
            byte[] b = new byte[65535];
            UDPClient uc = new UDPClient(UDPServer.host, UDPServer.udp_port);
            int size;
            int seq = 0;
            while ((size = connectionSocket.getInputStream().read(b)) > 0) {
                Frame_Header.frame_size = (short) size;
                Frame_Header.frame_seq = seq++;
                byte[] fb = Frame_Header.get_frame_header();
                byte[] d = new byte[size + fb.length];
                System.arraycopy(fb, 0, d, 0, fb.length);
                System.arraycopy(b, 0, d, fb.length, size);
                uc.setSendData(d);
                uc.send();
            }
            uc.close();
        }
        catch (Exception e) {

        }
    }

//    public static void main(String[] args) throws Exception {
//        TCPServer t = new TCPServer();
//        System.out.println(t.register());
//    }
}

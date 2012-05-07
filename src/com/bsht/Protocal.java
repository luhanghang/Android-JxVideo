package com.bsht;

public class Protocal {
    public final static short COMMAND_REGISTER = 0x0001;
    public final static int COMMAND_MAKEKEYFRAME = 0x0002;
    public final static int COMMAND_ENABLE_AUDIO = 0x0004;
}

class Command {
    static short command;
    static short method = 0;
    static short client_id = 0;
    static int session_id = 0;
    static short length = 0;
    static int cmd_size = 16;

    public static byte[] get_command() {
        byte[] cmd = new byte[cmd_size];
        Utils.putShort(cmd, command, 0);
        Utils.putShort(cmd, method, 2);
        Utils.putShort(cmd, client_id, 4);
        Utils.putInt(cmd, session_id, 6);
        Utils.putShort(cmd, length, 10);
        Utils.putInt(cmd,0,12);
        return cmd;
    }

    static public void parse_reply(byte[] response) {
        int offset = Packet.packet_size;
        command = Utils.getShort(response, offset);
        method = Utils.getShort(response, offset + 2);
        client_id = Utils.getShort(response, offset + 4);
        session_id = Utils.getInt(response, offset + 8);
        length = Utils.getShort(response, offset + 12);
    }

    static public String to_string() {
        return "Command\n-------------\ncommand:" + command + "\nmethod:" + method + "\nclient_id:" + client_id + "\nsession_id:" + session_id + "\nlength:" + length;
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

//    static public void parse_reply(byte[] response) {
//        version = Utils.getShort(response, 0);
//        size = Utils.getShort(response, 2);
//    }
//
//    static public String to_string() {
//        return "Packet\n-------------\nversion:" + version + "\nsize:" + size;
//    }
}

class Register {
    static int address = 0;
    static short port = 0;
    static short uuid_size; //length of imei
    static int register_size = 8;

    public static byte[] get_register() {
        byte[] reg = new byte[register_size];
        Utils.putInt(reg, address, 0);
        Utils.putShort(reg, port, 4);
        Utils.putShort(reg, uuid_size, 6);
        return reg;
    }
}

class Register_Reply {
    static byte result; //0 success ,non 0 failed
    static byte level;  //0
    static short client_id; //client_id
    static int session_id;  //session_id

    static public String parse_reply(byte[] response) {
        int offset = Packet.packet_size + Command.cmd_size;
        result = response[offset];
        level = response[offset + 1];
        client_id = Utils.getShort(response, offset + 2);
        session_id = Utils.getInt(response, offset + 4);
        return "Register_Reply\nresult:" + result + "\nlevel:" + level + "\nclient_id:" + client_id + "\nsession_id:" + session_id;
    }
//
//    static public String to_string() {
//        return "Reply\n--------------\nresult:" + result + "\nlevel:" + level + "\nclient_id:" + client_id + "\nsession_id:" + session_id;
//    }
}

class Frame_Header {
    static int session_id;
    static short size;
    static short frame_type; // 1 key frame
    static int frame_seq;
    static int frame_size = 12;

    public static byte[] get_frame_header() {
        byte[] header = new byte[frame_size];
        Utils.putInt(header, session_id, 0);
        Utils.putShort(header, size, 4);
        Utils.putShort(header, frame_type, 6);
        Utils.putInt(header, frame_seq, 8);
        //Utils.putInt(header, 0, 12);
        return header;
    }

    public static String log() {
        return "sessionid:" + session_id + "/size:" + size + "/frame_type:" + frame_type + "/frame_seq:" + frame_seq;
    }
}

class Keep_Alive_Message {
	static int session_id;
	static int next_time = 500;
    static int msg_size = 8;

    public static byte[] get_keep_alive_message() {
        byte[] message = new byte[msg_size];
        Utils.putInt(message, session_id, 0);
        Utils.putInt(message, next_time, 4);
        return message;
    }

    public static String to_string() {
        return "Keep Alive Message\nSession Id:" + session_id + "\nnext time:" + next_time;
    }
}

class Enable_Audio_Header {
	static int remote;
	static short port;
	static short result;

    public static void parse_enable_audio_header(byte[] response) {
        int offset = Packet.packet_size + Command.cmd_size;
        remote = Utils.getInt(response, offset);
        port = Utils.getShort(response, offset + 4);
        result = Utils.getShort(response, offset + 6);
    }

    public static String to_string() {
        return "Enable_Audio_Header\nremote=" + remote + "\nport=" + port + "\nresult=" + result;
    }
}

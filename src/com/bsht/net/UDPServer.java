package com.bsht.net;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class UDPServer implements Runnable {
    public static String host = "192.168.1.254";
    public static int udp_port = 8001;
    public static int tcp_port = 8001;

    public void run() {
        try {
            DatagramSocket socket = new DatagramSocket(8765);
            UDPClient uc = new UDPClient(host, udp_port);
            int seq = 0;
            while (true) {
                byte block[] = new byte[65535];
                DatagramPacket inpacket = new DatagramPacket(block, block.length);
                socket.receive(inpacket);
                int size = inpacket.getLength();
                byte inblock[] = inpacket.getData();

                Frame_Header.frame_size = (short) size;
                Frame_Header.frame_seq = seq++;
                byte[] fb = Frame_Header.get_frame_header();
                byte[] d = new byte[size + fb.length];
                //byte[] d = new byte[size];
                System.arraycopy(fb, 0, d, 0, fb.length);
                System.arraycopy(inblock, 0, d, fb.length, size);
                //System.arraycopy(inblock,0,d,0,size);
                uc.setSendData(d);
                uc.send();
            }
        } catch (Exception e) {

        }
    }

    public static boolean register(String uuid) throws Exception {
        Register.uuid_size = (short) uuid.length();
        Packet.size = (short) (Command.cmd_size + Register.register_size + Packet.packet_size + uuid.length());

        byte[] p = Packet.get_package();
        byte[] c = Command.get_command();
        byte[] r = Register.get_register();
        byte[] data = new byte[p.length + c.length + r.length + Register.uuid_size];
        System.arraycopy(p, 0, data, 0, p.length);
        System.arraycopy(c, 0, data, p.length, c.length);
        System.arraycopy(r, 0, data, p.length + c.length, r.length);
        System.arraycopy(uuid.getBytes(), 0, data, p.length + c.length + r.length, Register.uuid_size);

        byte[] response = TCPClient.send(host, tcp_port, data);
        if (response != null) {
            Packet.parse_reply(response);
            Command.parse_reply(response);
            Register_Reply.parse_reply(response);
            //System.out.println(Register_Reply.to_string());
            Frame_Header.session_id = Register_Reply.session_id;
            Frame_Header.frame_type = 0;
            return Register_Reply.result == 0;
        }
        return false;
    }
}
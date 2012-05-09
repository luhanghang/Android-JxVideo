package com.bsht;

public class Utils {
    public static void putShort(byte b[], short s, int index) {
        b[index + 1] = (byte) (s >> 8);
        b[index] = (byte) s;
    }

    public static short getShort(byte[] b, int index) {
        return (short) (((b[index + 1] & 0xff) << 8) | b[index] & 0xff);
    }

    public static void putInt(byte[] bb, int x, int index) {
        bb[index + 3] = (byte) (x >> 24);
        bb[index + 2] = (byte) (x >> 16);
        bb[index + 1] = (byte) (x >> 8);
        bb[index] = (byte) x;
    }

    public static int getInt(byte[] bb, int index) {
        return ((bb[index + 3] & 0xff) << 24)
                | ((bb[index + 2] & 0xff) << 16)
                | ((bb[index + 1] & 0xff) << 8)
                | bb[index] & 0xff;
    }
}

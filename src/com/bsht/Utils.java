package com.bsht;

/**
 * Created by IntelliJ IDEA.
 * User: luhang
 * Date: Jul 7, 2010
 * Time: 5:04:37 PM
 * To change this template use File | Settings | File Templates.
 */
public class Utils {
    public static void putShort(byte b[], short s, int index) {
        b[index + 1] = (byte) (s >> 8);
        b[index + 0] = (byte) (s >> 0);
    }

    public static short getShort(byte[] b, int index) {
        return (short) (((b[index + 1] << 8) | b[index] & 0xff));
    }

    public static void putInt(byte[] bb, int x, int index) {
        bb[index + 3] = (byte) (x >> 24);
        bb[index + 2] = (byte) (x >> 16);
        bb[index + 1] = (byte) (x >> 8);
        bb[index + 0] = (byte) (x >> 0);
    }

    public static int getInt(byte[] bb, int index) {
        return (int) ((((bb[index + 0] & 0xff) << 24)
                | ((bb[index + 1] & 0xff) << 16)
                | ((bb[index + 2] & 0xff) << 8) | ((bb[index + 3] & 0xff) << 0)));
    }

    public static void putLong(byte[] bb, long x, int index) {
//        bb[index + 7] = (byte) (x >> 56);
//        bb[index + 6] = (byte) (x >> 48);
//        bb[index + 5] = (byte) (x >> 40);
//        bb[index + 4] = (byte) (x >> 32);
        bb[index + 3] = (byte) (x >> 24);
        bb[index + 2] = (byte) (x >> 16);
        bb[index + 1] = (byte) (x >> 8);
        bb[index + 0] = (byte) (x >> 0);
    }

//    public static long getLong(byte[] bb, int index) {
//        return ((((long) bb[index + 0] & 0xff) << 56)
//                | (((long) bb[index + 1] & 0xff) << 48)
//                | (((long) bb[index + 2] & 0xff) << 40)
//                | (((long) bb[index + 3] & 0xff) << 32)
//                | (((long) bb[index + 4] & 0xff) << 24)
//                | (((long) bb[index + 5] & 0xff) << 16)
//                | (((long) bb[index + 6] & 0xff) << 8) | (((long) bb[index + 7] & 0xff) << 0));
//    }

    public static long getLong(byte[] bb, int index) {
        return ((((long) bb[index + 3] & 0xff) << 24)
                | (((long) bb[index + 2] & 0xff) << 16)
                | (((long) bb[index + 1] & 0xff) << 8) | (((long) bb[index + 0] & 0xff) << 0));
    }
}

package com.bsht.jxvideo;

/**
 * Created by IntelliJ IDEA.
 * User: luhang
 * Date: 10/10/11
 * Time: 11:08 AM
 * To change this template use File | Settings | File Templates.
 */
public class Jxaudio {
    public Jxaudio() {
        //System.loadLibrary("jxcodec");
    }

    public native long create(int type);
    public native int decode(long handle,byte[] input,int input_buffer_size, byte[] output,int output_buffer_size);
    public native int destroy(long handle);
}

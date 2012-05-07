package com.bsht.jxvideo;

public class Jxcodec {
    public Jxcodec() {
        System.loadLibrary("jxcodec");
    }

    public native long create(int type,int rate,int width,int height,int fps,int gop);
    public native int encode(long handle,byte[] input,byte[] output,int output_buffer_size);
    public native int destroy(long handle);
}

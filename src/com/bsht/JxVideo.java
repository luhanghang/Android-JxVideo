package com.bsht;

import android.app.Activity;
//import android.app.AlertDialog;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.media.*;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.*;

import com.bsht.jxvideo.G711;
import com.bsht.jxvideo.Jxaudio;
import com.bsht.jxvideo.Jxcodec;
import com.bsht.net.Callback;
import com.bsht.net.TCPClient;
import com.bsht.net.UDPServer;

import java.net.*;
import java.util.Enumeration;
import java.util.Timer;

public class JxVideo extends Activity
        implements SurfaceHolder.Callback, Camera.PreviewCallback, Callback {


    public static final int WIDTH = 320;
    public static final int HEIGHT = 240;

    private int gop;
    private int frameRate;
    private int bitRate;
    private String gateway;

    private final static int TOGGLE_VIDEO = 1;
    private final static int TOGGLE_AUDIO = 2;
    private final static int PREFERENCES = 3;
    private final static int SHOWUUID = 4;
    private final static int UDP_PORT = 8001;
    private final static int TCP_PORT = 8001;
    private final static int AUDIO_RECEIVE_UDP_LISTEN_PORT = 8000;

    private final static int AUDIO_DECODER_G711 = 0;
    //private final static int AUDIO_DECODER_MPEG2 = 1;

    private int audio_decoder = 0;
    private Camera mCamera;
    private boolean mPreviewRunning = false;
    private Jxcodec jxcodec;
    private Jxaudio jxaudio;
    private long codec_handle;
    private long audio_decoder_handle;
    private byte[] encoded = new byte[655350];
    private byte[] audio_decoded = new byte[8192];

    private DatagramSocket clientSocket;
    private InetAddress ipaddress;

    private boolean videoOn = true;
    private boolean audioOn = true;

    private int video_frame_seq = 0;
    private int audio_frame_seq = 0;

    private AudioRecord recordInstance;

    private static int[] FREQUENCIES = {8000, 32000};
    private static int[] CHANNELS = {AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.CHANNEL_CONFIGURATION_STEREO};
    private static final int AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;

    private int bufferSize;
    private short[] tempBuffer;

    private TCPClient tcpClient;
    private UDPServer us;

    private boolean enableAudio = false;
    private int lastKeepAlive = 0;
    private Timer timer;
    private AudioTrack audioTrack;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.main);

        SurfaceView mSurfaceView = (SurfaceView) this.findViewById(R.id.surface_camera);
        SurfaceHolder mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(this);
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
        log("surfaceChanged");
        if (jxcodec == null) {
            jxcodec = new Jxcodec();
            codec_handle = jxcodec.create(0, bitRate, WIDTH, HEIGHT, frameRate, gop);
            log("bitRate:" + bitRate + "/frameRate:" + frameRate + "/gop:" + gop);
            log("video_encoder_handle:" + codec_handle);
        }

        if (jxaudio == null) {
            jxaudio = new Jxaudio();
            audio_decoder_handle = jxaudio.create(0);
            log("audio_decoder_handle:" + audio_decoder_handle);
        }

        if (mPreviewRunning) {
            mCamera.stopPreview();
        }

        mCamera.setDisplayOrientation(90);
        Camera.Parameters p = mCamera.getParameters();
        //int fmt = p.getPreviewFormat();
        //new AlertDialog.Builder(this).setMessage("Format" + fmt).show();//??
        //PixelFormat.YCbCr_420_SP;

        p.setPreviewSize(WIDTH, HEIGHT);

        mCamera.setPreviewCallback(this);
        mCamera.setParameters(p);
        try {
            mCamera.setPreviewDisplay(holder);
        } catch (Exception ex) {
            log("Error:" + ex.getMessage());
        }
        mCamera.startPreview();
        mPreviewRunning = true;
    }

    public void surfaceCreated(SurfaceHolder holder) {
        videoOn = true;
        audioOn = true;
        log("surfaceCreated");
        readPreference();
        log("Gateway is:" + gateway);
        timer = new Timer();

        int audioPlayBufferSize;
        log("audio_decoder:" + audio_decoder);
        bufferSize = AudioRecord.getMinBufferSize(FREQUENCIES[AUDIO_DECODER_G711], CHANNELS[AUDIO_DECODER_G711], AUDIO_ENCODING);
        audioPlayBufferSize = AudioTrack.getMinBufferSize(FREQUENCIES[audio_decoder], CHANNELS[audio_decoder], AUDIO_ENCODING);
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, FREQUENCIES[audio_decoder], CHANNELS[audio_decoder], AUDIO_ENCODING, audioPlayBufferSize, AudioTrack.MODE_STREAM);


        log("audiobuffersize:" + bufferSize);

        byte[] audioPlayData = new byte[audioPlayBufferSize];
        //byte[] audioPlayData = new byte[480];

        tcpClient = new TCPClient(gateway, TCP_PORT, this);
        us = new UDPServer(AUDIO_RECEIVE_UDP_LISTEN_PORT, audioPlayData, this);

        try {
            clientSocket = new DatagramSocket();
            ipaddress = InetAddress.getByName(gateway);
            log("Socket created");
            this.register();
        } catch (Exception e) {
            videoOn = false;
            audioOn = false;
            log("Error!:" + e.getMessage());
        }
        mCamera = Camera.open();
        this.startAudio();

        TimerTask tt = new TimerTask(this);
        timer.schedule(tt, 0, 1000);
    }

    private void startAudio() {
        recordInstance = new AudioRecord(MediaRecorder.AudioSource.MIC, FREQUENCIES[AUDIO_DECODER_G711], CHANNELS[AUDIO_DECODER_G711], AUDIO_ENCODING, bufferSize);
        //Log.e("JxVde", recordInstance.getState() + "");
        recordInstance.startRecording();
        tempBuffer = new short[bufferSize];
    }

    private void sendAudio() {
        if (recordInstance == null) return;
        int audioBufferRead = recordInstance.read(tempBuffer, 0, bufferSize);
//        log("audioBufferRead:" + audioBufferRead);
//        log("bufferSize:" + bufferSize);

        Frame_Header.frame_seq = audio_frame_seq++;
        Frame_Header.size = (short) audioBufferRead;

        byte[] fb = Frame_Header.get_frame_header();
        byte[] d = new byte[audioBufferRead + fb.length];
        System.arraycopy(fb, 0, d, 0, fb.length);
        //log(Frame_Header.log());
        for (int i = 0; i < audioBufferRead; i++) {
            d[fb.length + i] = G711.linear2alaw(tempBuffer[i]);
            //log(i + ":" + tempBuffer[i]);
        }
        DatagramPacket sendPacket = new DatagramPacket(d, d.length, ipaddress, UDP_PORT + 1);
        try {
            clientSocket.send(sendPacket);
        } catch (Exception e) {
            log(e.getMessage());
        }
    }

    public void log(Object msg) {
        //Log.e("JxVideo", msg + "");
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        videoOn = false;
        audioOn = false;
        mPreviewRunning = false;

        if (timer != null) {
            timer.cancel();
            timer = null;
        }

        if (clientSocket != null) {
            clientSocket.close();
            clientSocket = null;
        }

        if (audioTrack != null) {
            audioTrack.stop();
            audioTrack.release();
            audioTrack = null;
        }

        if (us != null) {
            us.destroy();
            us = null;
        }

        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }

        if (recordInstance != null) {
            recordInstance.stop();
            recordInstance.release();
            recordInstance = null;
        }

        if (jxaudio != null) {
            jxaudio.destroy(audio_decoder_handle);
            jxaudio = null;
        }

        if (jxcodec != null) {
            jxcodec.destroy(codec_handle);
            jxcodec = null;
        }

        if (tcpClient != null) {
            tcpClient.destroy();
            tcpClient = null;
        }

        log("surfaceDestroyed");
    }

    public void onPreviewFrame(byte[] data, Camera camera) {
        if (Frame_Header.session_id == 0) return;
        if (audioOn)
            this.sendAudio();
        if (!videoOn) return;
        int size = jxcodec.encode(codec_handle, data, encoded, encoded.length);
        Frame_Header.size = (short) size;
        Frame_Header.frame_seq = video_frame_seq++;
        byte[] fb = Frame_Header.get_frame_header();
        byte[] d = new byte[size + fb.length];
        System.arraycopy(fb, 0, d, 0, fb.length);
        System.arraycopy(encoded, 0, d, fb.length, size);
        DatagramPacket sendPacket = new DatagramPacket(d, d.length, ipaddress, UDP_PORT);
        try {
            clientSocket.send(sendPacket);
        } catch (Exception e) {
            log(e.getMessage());
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, TOGGLE_VIDEO, Menu.NONE, getString(R.string.videoOff));
        menu.add(Menu.NONE, TOGGLE_AUDIO, Menu.NONE, getString(R.string.audioOff));
        menu.add(Menu.NONE, PREFERENCES, Menu.NONE, getString(R.string.preferences))
                .setAlphabeticShortcut('p');
        menu.add(Menu.NONE, SHOWUUID, Menu.NONE, getString(R.string.showUUID));
        return (super.onCreateOptionsMenu(menu));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case TOGGLE_VIDEO:
                this.videoOn = !this.videoOn;
                break;
            case TOGGLE_AUDIO:
                this.audioOn = !this.audioOn;
                break;
            case PREFERENCES:
                startActivity(new Intent(this, JxVideoPreferences.class));
                break;
            case SHOWUUID:
                Dialog dialog = new AlertDialog.Builder(this)
                        .setTitle("UUID")
                        .setMessage(getUUID()).setNegativeButton(getString(R.string.close), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.dismiss();
                            }
                        })
                        .create();
                dialog.show();
                break;
        }
        return (super.onOptionsItemSelected(item));
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem mi = menu.getItem(0);
        mi.setTitle(getString(this.videoOn ? R.string.videoOff : R.string.videoOn));
        mi = menu.getItem(1);
        mi.setTitle(getString(this.audioOn ? R.string.audioOff : R.string.audioOn));
        return true;
    }

    private void readPreference() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        gateway = prefs.getString("gateway", "");
        bitRate = Integer.parseInt(prefs.getString("bitrate", "300")) * 1000;
        frameRate = Integer.parseInt(prefs.getString("framerate", "8"));
        gop = Integer.parseInt(prefs.getString("gop", "50"));
        audio_decoder = Integer.parseInt(prefs.getString("audioDecoder", "1"));
    }

    private void register() throws Exception {
        String uuid = this.getUUID();
        Register.uuid_size = (short) uuid.length();
        Packet.size = (short) (Command.cmd_size + Register.register_size + Packet.packet_size + uuid.length());
        Command.command = Protocal.COMMAND_REGISTER;
        byte[] p = Packet.get_package();
        byte[] c = Command.get_command();
        byte[] r = Register.get_register();
        byte[] data = new byte[Packet.size];
        System.arraycopy(p, 0, data, 0, p.length);
        System.arraycopy(c, 0, data, p.length, c.length);
        System.arraycopy(r, 0, data, p.length + c.length, r.length);
        System.arraycopy(uuid.getBytes(), 0, data, p.length + c.length + r.length, Register.uuid_size);
        tcpClient.send(data);
    }

    public void parseCommand(byte[] response) {
        Command.parse_reply(response);
        log(Command.to_string());
        switch (Command.command) {
            case Protocal.COMMAND_REGISTER:
                Register_Reply.parse_reply(response);
                Frame_Header.session_id = Register_Reply.session_id;
                Keep_Alive_Message.session_id = Frame_Header.session_id;
                Frame_Header.frame_type = 0;
                break;
            case Protocal.COMMAND_ENABLE_AUDIO:
                Enable_Audio_Header.parse_enable_audio_header(response);
                log(Enable_Audio_Header.to_string());
                enableAudio = Enable_Audio_Header.result == 1;
                if (enableAudio) {
                    audioTrack.play();
                    sendKeepAlive();
                } else {
                    audioTrack.stop();
                }
                break;
            case Protocal.COMMAND_MAKEKEYFRAME:
                log("Make Key Frame");
                break;
        }
    }

    public void keepAlive() {
        if (Frame_Header.session_id == 0 || !enableAudio) {
            lastKeepAlive = 0;
            return;
        }
        if (++lastKeepAlive >= 10) {
            this.sendKeepAlive();
        }
    }

    private void sendKeepAlive() {
        if (Frame_Header.session_id == 0) return;
        byte[] msg = Keep_Alive_Message.get_keep_alive_message();
        DatagramPacket sendPacket = new DatagramPacket(msg, msg.length, ipaddress, Enable_Audio_Header.port);
        try {
            us.send(sendPacket);
        } catch (Exception e) {
            log("Error:clientSocket.send(sendPacket);");
        }
        lastKeepAlive = 0;
    }

    public void playAudio(byte[] audioData, int length) {
        if (audio_decoder == AUDIO_DECODER_G711) {
            short[] sample = new short[length];
            for (int i = 0; i < length; i++) {
                sample[i] = (short) G711.alaw2linear(audioData[i]);
            }
            audioTrack.write(sample, 0, length);
        } else {
            int len = jxaudio.decode(audio_decoder_handle, audioData, length, audio_decoded, audio_decoded.length);
            log("dec len:" + len);
            audioTrack.write(audio_decoded, 0, len);
        }
    }

    private String getUUID() {
        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        return tm.getDeviceId();
        //return "2234567890abcde";
    }

    private void getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface
                    .getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf
                        .getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        log(inetAddress.getHostAddress().toString());
                    }
                }
            }
        } catch (SocketException ex) {
            Log.e("WifiPreference IpAddress", ex.toString());
        }
    }
}

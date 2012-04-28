package com.bsht;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.TabHost;
import org.apache.commons.logging.Log;

import java.net.Socket;

public class JxVideo extends Activity implements SurfaceHolder.Callback {
    TabHost tabs;
    ListView lv;
    SurfaceView monitor;
    Socket socket;
    ParcelFileDescriptor pfd;
    //private String SDPATH = Environment.getExternalStorageDirectory().getAbsolutePath();

    //private Camera camera;
    //private boolean previewRunning = false;
    private MediaRecorder mr;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            socket = new Socket("192.168.1.113", 1234,false);
        }   catch (Exception e) {
            log("new socket:",e.getMessage());
        }
        pfd = ParcelFileDescriptor.fromSocket(socket);
        init_ui();
        init_media_recorder();
        tabs.setCurrentTab(1);
    }

    private void init_ui() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        this.setContentView(R.layout.main);

        tabs = (TabHost) this.findViewById(R.id.tabhost);
        tabs.setup();
        TabHost.TabSpec spec = tabs.newTabSpec("List");
        spec.setContent(R.id.list);
        spec.setIndicator(this.getString(R.string.list));
        tabs.addTab(spec);
        spec = tabs.newTabSpec("Monitor");
        spec.setContent(R.id.monitor);
        spec.setIndicator(this.getString(R.string.monitor));
        tabs.addTab(spec);

        //tabs.getTabWidget().getLayoutParams().height = 50;
        lv = (ListView) this.findViewById(R.id.list);
    }

    private void init_media_recorder() {
        monitor = (SurfaceView) this.findViewById(R.id.monitor);
        SurfaceHolder holder = monitor.getHolder();
        holder.addCallback(this);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        mr = new MediaRecorder();
        mr.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        //mr.setAudioSource(MediaRecorder.AudioSource.MIC);
        mr.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mr.setVideoSize(320, 240);
        mr.setVideoFrameRate(5);
        mr.setVideoEncoder(MediaRecorder.VideoEncoder.MPEG_4_SP);
        MediaPlayer.
        //mr.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mr.setMaxDuration(0);

        //mr.setMaxFileSize(0);

    }

    public void surfaceCreated(SurfaceHolder holder) {
        try {
            //socket = new Socket("192.168.1.109", 1234, false);
            mr.setPreviewDisplay(holder.getSurface());
            mr.setOutputFile(pfd.getFileDescriptor());
            //mr.setOutputFile(SDPATH + "/test.mp4");
            mr.prepare();
            mr.start();
        } catch (Exception e) {
            log("mr.prepare()",e.getMessage());
        }
    }

    public void log(String funname,String str) {
        String msg = funname + ":" + str;
        AlertDialog ed = new AlertDialog.Builder(this).create();
        ed.setMessage(msg);
        ed.setButton(getString(R.string.close), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dlg, int i) {

            }
        });
        ed.show();
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        mr.release();
    }

    public void surfaceChanged(SurfaceHolder holder,
                               int format, int width, int height) {

    }

    protected void onDestroy() {
//        try {
//            pfd.close();
//            socket.close();
//        }catch (Exception e) {
//            log("on destroy",e.getMessage());
//        }
    }
}
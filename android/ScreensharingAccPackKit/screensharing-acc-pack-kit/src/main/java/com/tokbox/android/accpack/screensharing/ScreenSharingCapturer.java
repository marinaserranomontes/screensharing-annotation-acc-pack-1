package com.tokbox.android.accpack.screensharing;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.YuvImage;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;

import com.opentok.android.BaseVideoCapturer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ScreenSharingCapturer extends BaseVideoCapturer{
    private static final String LOG_TAG = ScreenSharingCapturer.class.getSimpleName();
    private Context mContext;

    private boolean capturing = false;
    private View contentView;

    private int fps = 15;
    private int width = 0;
    private int height = 0;
    private int[] frame;

    private Bitmap bmp;
    private Canvas canvas;

    private Handler mHandler = new Handler();

    private ImageReader mImageReader;

    private ByteBuffer ybuffer;
    private ByteBuffer ubuffer;
    private ByteBuffer vbuffer;

    private int yPixelStride;
    private int uPixelStride;
    private int vPixelStride;

    private int yRowStride;
    private int uRowStride;
    private int vRowStride;

    private int orientation = 0;
    private boolean mirrored = false;

    private Runnable newFrame = new Runnable() {
        @TargetApi(Build.VERSION_CODES.KITKAT)
        @Override
        public void run() {
            if (capturing) {
            frame = null;
                frame = null;
                if (frame == null ) {

                        provideBufferFramePlanar(ybuffer,
                                ubuffer,
                                vbuffer,
                                yPixelStride,
                                yRowStride,
                                uPixelStride,
                                uRowStride,
                                vPixelStride,
                                vRowStride,
                                width,
                                height,
                                0,
                                false);
                    }

                    mHandler.postDelayed(newFrame, 1000 / fps);

                }
        }
    };


    /* Constructor
     * @param context Application context
     * @param view Screensharing content view
     * @param imageReader to access to the image data rendered in the screensharing
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public ScreenSharingCapturer(Context context, View view, ImageReader imageReader) {
        this.mContext = context;
        this.contentView = view;
        this.mImageReader = imageReader;
        this.mImageReader.setOnImageAvailableListener(new ImageAvailableListener(), null);

        this.width = contentView.getWidth();
        this.height = contentView.getHeight();

    }

    @Override
    public void init() {

    }

    @Override
    public int startCapture() {
        capturing = true;

        mHandler.postDelayed(newFrame, 1000 / fps);
        return 0;
    }

    @Override
    public int stopCapture() {
        capturing = false;
        mHandler.removeCallbacks(newFrame);
        return 0;
    }

    @Override
    public boolean isCaptureStarted() {
        return capturing;
    }

    @Override
    public CaptureSettings getCaptureSettings() {

        CaptureSettings settings = new CaptureSettings();
        settings.fps = fps;
        settings.width = width;
        settings.height = height;
        settings.format = ARGB;
        return settings;
    }

    @Override
    public void destroy() {

    }

    @Override
    public void onPause() {

    }

    @Override
    public void onResume() {

    }


    @TargetApi(Build.VERSION_CODES.KITKAT)
    private class ImageAvailableListener implements ImageReader.OnImageAvailableListener {

    @Override
    public void onImageAvailable(ImageReader reader) {
        Log.i(LOG_TAG, "in OnImageAvailable");
        FileOutputStream fos = null;
        Image img = null;
            //get the image
            Image image = reader.acquireNextImage();

            ybuffer = image.getPlanes()[0].getBuffer();
            ubuffer = image.getPlanes()[1].getBuffer();
            vbuffer= image.getPlanes()[2].getBuffer();

            yPixelStride = image.getPlanes()[0].getPixelStride();
            uPixelStride = image.getPlanes()[1].getPixelStride();;
            vPixelStride = image.getPlanes()[2].getPixelStride();;

            yRowStride = image.getPlanes()[0].getRowStride();
            uRowStride = image.getPlanes()[1].getRowStride();
            vRowStride = image.getPlanes()[2].getRowStride();

            width = image.getWidth();
            height = image.getHeight();

        Log.i("MARINAS", "IMAGE GET WIDTH: "+width);
        Log.i("MARINAS", "IMAGE GET WIDTH: "+height);


        image.close();

        }

    }
}

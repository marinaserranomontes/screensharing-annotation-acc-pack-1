package com.tokbox.android.screensharingsample;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.opentok.android.OpentokError;
import com.tokbox.android.annotations.AnnotationsToolbar;
import com.tokbox.android.annotations.AnnotationsView;
import com.tokbox.android.annotations.utils.AnnotationsVideoRenderer;
import com.tokbox.android.logging.OTKAnalytics;
import com.tokbox.android.logging.OTKAnalyticsData;
import com.tokbox.android.otsdkwrapper.listeners.AdvancedListener;
import com.tokbox.android.otsdkwrapper.listeners.BasicListener;
import com.tokbox.android.otsdkwrapper.listeners.ListenerException;
import com.tokbox.android.otsdkwrapper.listeners.PausableAdvancedListener;
import com.tokbox.android.otsdkwrapper.listeners.PausableBasicListener;
import com.tokbox.android.otsdkwrapper.utils.MediaType;
import com.tokbox.android.otsdkwrapper.utils.OTConfig;
import com.tokbox.android.otsdkwrapper.utils.PreviewConfig;
import com.tokbox.android.otsdkwrapper.utils.StreamStatus;
import com.tokbox.android.otsdkwrapper.wrapper.OTWrapper;
import com.tokbox.android.screensharingsample.config.OpenTokConfig;
import com.tokbox.android.screensharingsample.ui.PreviewCameraFragment;
import com.tokbox.android.screensharingsample.ui.PreviewControlFragment;
import com.tokbox.android.screensharingsample.ui.RemoteControlFragment;
import com.tokbox.android.screensharingsample.ui.ScreenSharingBar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements PreviewControlFragment.PreviewControlCallbacks,
        RemoteControlFragment.RemoteControlCallbacks, PreviewCameraFragment.PreviewCameraCallbacks, AnnotationsView.AnnotationsListener, ScreenSharingBar.ScreenSharingBarListener {

    private final String LOG_TAG = MainActivity.class.getSimpleName();

    private final String[] permissions = {Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private final int permsRequestCode = 200;

    //OpenTok calls
    private OTWrapper mWrapper;

    private RelativeLayout mPreviewViewContainer;
    private RelativeLayout mRemoteViewContainer;
    private RelativeLayout mAudioOnlyView;
    private RelativeLayout mLocalAudioOnlyView;
    private RelativeLayout.LayoutParams layoutParamsPreview;
    private RelativeLayout mCameraFragmentContainer;
    private RelativeLayout mActionBarContainer;

    private TextView mAlert;
    private ImageView mAudioOnlyImage;

    //UI control bars fragments
    private PreviewControlFragment mPreviewFragment;
    private RemoteControlFragment mRemoteFragment;
    private PreviewCameraFragment mCameraFragment;
    private FragmentTransaction mFragmentTransaction;

    ProgressDialog mProgressDialog;

    //annotations
    private AnnotationsToolbar mAnnotationsToolbar;
    private AnnotationsVideoRenderer mRemoteRenderer;
    private AnnotationsVideoRenderer mScreensharingRenderer;
    private AnnotationsView mRemoteAnnotationsView;

    //screensharing
    private AnnotationsView mScreenAnnotationsView;
    private View mScreenSharingView;
    private ScreenSharingBar mScreensharingBar;

    private TextView mCallToolbar;

    private boolean isRemoteAnnotations = false;
    private boolean isScreensharing = false;
    private boolean isAnnotations = false;
    private CountDownTimer mCountDownTimer;
    private String mRemoteConnId;
    private int mOrientation;

    //permissions
    private boolean mAudioPermission = false;
    private boolean mVideoPermission = false;
    private boolean mWriteExternalStoragePermission = false;
    private boolean mReadExternalStoragePermission = false;

    //status
    private boolean isConnected = false;
    private boolean isLocal = false;
    private boolean isCallInProgress = false;

    //Remote
    private String mRemoteId;
    private String mScreenRemoteId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(LOG_TAG, "onCreate");

        requestWindowFeature(Window.FEATURE_ACTION_BAR);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPreviewViewContainer = (RelativeLayout) findViewById(R.id.publisherview);
        mRemoteViewContainer = (RelativeLayout) findViewById(R.id.subscriberview);
        mAlert = (TextView) findViewById(R.id.quality_warning);
        mAudioOnlyView = (RelativeLayout) findViewById(R.id.audioOnlyView);
        mLocalAudioOnlyView = (RelativeLayout) findViewById(R.id.localAudioOnlyView);
        mCameraFragmentContainer = (RelativeLayout) findViewById(R.id.camera_preview_fragment_container);
        mActionBarContainer = (RelativeLayout) findViewById(R.id.actionbar_preview_fragment_container);

        mAnnotationsToolbar = (AnnotationsToolbar) findViewById(R.id.annotations_bar);

        mCallToolbar = (TextView) findViewById(R.id.call_toolbar);

        //request Marshmallow camera permission
        if (ContextCompat.checkSelfPermission(this, permissions[1]) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, permissions[0]) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(permissions, permsRequestCode);
            }
        } else {
            mVideoPermission = true;
            mAudioPermission = true;
            mWriteExternalStoragePermission = true;
            mReadExternalStoragePermission = true;
        }

        //init the sdk wrapper
        OTConfig config =
                new OTConfig.OTConfigBuilder(OpenTokConfig.SESSION_ID, OpenTokConfig.TOKEN,
                        OpenTokConfig.API_KEY).name("one-to-one-sample-app").subscribeAutomatically(true).subscribeToSelf(false).build();

        mWrapper = new OTWrapper(MainActivity.this, config);

        //set listener to receive the communication events, and add UI to these events
        mWrapper.addBasicListener(mBasicListener);
        mWrapper.addAdvancedListener(mAdvancedListener);

        //use a custom video renderer for the annotations. It will be applied to the remote. It will be applied before to start subscribing
        mRemoteRenderer = new AnnotationsVideoRenderer(this);
        mScreensharingRenderer = new AnnotationsVideoRenderer(this);
        mWrapper.setRemoteVideoRenderer(mRemoteRenderer, true);

        //connect
        if (mWrapper != null) {
            mWrapper.connect();
        }

        //show connections dialog
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("Please wait");
        mProgressDialog.setMessage("Connecting...");
        mProgressDialog.show();

        //init controls fragments
        if (savedInstanceState == null) {
            mFragmentTransaction = getSupportFragmentManager().beginTransaction();
            initCameraFragment(); //to swap camera
            initPreviewFragment(); //to enable/disable local media
            mFragmentTransaction.commitAllowingStateLoss();
        }

        //get orientation
        mOrientation = getResources().getConfiguration().orientation;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        reloadViews();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mWrapper != null) {
            mWrapper.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mWrapper != null) {
            mWrapper.resume(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_menu, menu);
        return true;
    }

    @Override
    public void onRequestPermissionsResult(final int permsRequestCode, final String[] permissions,
                                           int[] grantResults) {
        switch (permsRequestCode) {
            case 200:
                mVideoPermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                mAudioPermission = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                mReadExternalStoragePermission = grantResults[2] == PackageManager.PERMISSION_GRANTED;
                mWriteExternalStoragePermission = grantResults[3] == PackageManager.PERMISSION_GRANTED;

                if (!mVideoPermission || !mAudioPermission || !mReadExternalStoragePermission || !mWriteExternalStoragePermission) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle(getResources().getString(R.string.permissions_denied_title));
                    builder.setMessage(getResources().getString(R.string.alert_permissions_denied));
                    builder.setPositiveButton("I'M SURE", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    builder.setNegativeButton("RE-TRY", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                requestPermissions(permissions, permsRequestCode);
                            }
                        }
                    });
                    builder.show();
                }

                break;
        }
    }

    public OTWrapper getWrapper() {
        return mWrapper;
    }

    public boolean isCallInProgress() {
        return isCallInProgress;
    }

    public void showRemoteControlBar(View v) {
        if (mRemoteFragment != null && (mRemoteId != null)) {
            mRemoteFragment.show();
        }
    }

    public boolean isScreensharing() {
        return isScreensharing;
    }

    @Override
    public void onScreenSharing() {
        if (isScreensharing) {
            isScreensharing = false;
            mWrapper.stopPublishingMedia(true);
            showAVCall(true);
            showAnnotationsToolbar(false);
            mPreviewFragment.restartScreensharing(); //restart screensharing UI
            mWrapper.startPublishingMedia(new PreviewConfig.PreviewConfigBuilder().
                name("Tokboxer").build(), false); //restar av call
            isCallInProgress = true;
            isAnnotations = false;
            ((ViewGroup)mScreenSharingView).removeView(mScreenAnnotationsView);
            showScreensharingBar(false);
        }
        else{
            isScreensharing = true;
            showAVCall(false);
            mWrapper.stopPublishingMedia(false); //stop call
            isCallInProgress = false;
            PreviewConfig.PreviewConfigBuilder builder = new PreviewConfig.PreviewConfigBuilder().name("TokboxerScreen").renderer(mScreensharingRenderer);
            mWrapper.startPublishingMedia(builder.build(), true); //start screensharing
        }
    }

    @Override
    public void onAnnotations() {
        if (!isAnnotations) {
            showAnnotationsToolbar(true);
            isAnnotations = true;
        } else {
            showAnnotationsToolbar(false);
            isAnnotations = false;
        }
    }

    //Audio remote button event
    @Override
    public void onDisableRemoteAudio(boolean audio) {
        if (mWrapper != null) {
            mWrapper.enableReceivedMedia(mRemoteId, MediaType.AUDIO, audio);
        }
    }

    //Video remote button event
    @Override
    public void onDisableRemoteVideo(boolean video) {
        if (mWrapper != null) {
            mWrapper.enableReceivedMedia(mRemoteId, MediaType.VIDEO, video);
        }
    }

    //Camera control button event
    @Override
    public void onCameraSwap() {
        if (mWrapper != null) {
            mWrapper.cycleCamera();
        }
    }

    //ScreensharingBar event
    @Override
    public void onClose() {
        onScreenSharing();
    }

    public void onCallToolbar(View view) {
        showAll();
    }

    //Video local button event
    @Override
    public void onDisableLocalVideo(boolean video) {
        if (mWrapper != null) {
            mWrapper.enableLocalMedia(MediaType.VIDEO, video);

            if ( mRemoteId != null || mScreenRemoteId != null ) {
                if (!video) {
                    mAudioOnlyImage = new ImageView(this);
                    mAudioOnlyImage.setImageResource(R.drawable.avatar);
                    mAudioOnlyImage.setBackgroundResource(R.drawable.bckg_audio_only);
                    mPreviewViewContainer.addView(mAudioOnlyImage, layoutParamsPreview);
                } else {
                    mPreviewViewContainer.removeView(mAudioOnlyImage);
                }
            } else {
                if (!video) {
                    mLocalAudioOnlyView.setVisibility(View.VISIBLE);
                    mPreviewViewContainer.addView(mLocalAudioOnlyView);
                } else {
                    mLocalAudioOnlyView.setVisibility(View.GONE);
                    mPreviewViewContainer.removeView(mLocalAudioOnlyView);
                }
            }
        }
    }

    @Override
    public void onDisableLocalAudio(boolean audio) {
        if (mWrapper != null) {
            mWrapper.enableLocalMedia(MediaType.AUDIO, audio);
        }
    }

    //Call button event
    @Override
    public void onCall() {
        Log.i(LOG_TAG, "OnCall");
        if ( mWrapper != null && isConnected ) {
            if ( !isCallInProgress ) {
                mWrapper.startPublishingMedia(new PreviewConfig.PreviewConfigBuilder().
                        name("Tokboxer").build(), false);

                if ( mPreviewFragment != null ) {
                    mPreviewFragment.setEnabled(true);
                }
                isCallInProgress = true;
            } else {
                mWrapper.stopPublishingMedia(false);
                isCallInProgress = false;
                cleanViewsAndControls();
            }
        }
    }

    //Annotations events
    @Override
    public void onScreencaptureReady(Bitmap bmp) {
        saveScreencapture(bmp);
    }

    @Override
    public void onAnnotationsSelected(AnnotationsView.Mode mode) {
        if (mode.equals(AnnotationsView.Mode.Pen) || mode.equals(AnnotationsView.Mode.Text)) {
            showAll();
            //show minimized calltoolbar
            mCallToolbar.setVisibility(View.VISIBLE);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mAnnotationsToolbar.getLayoutParams();
            params.addRule(RelativeLayout.ABOVE, mCallToolbar.getId());
            mAnnotationsToolbar.setLayoutParams(params);
            mAnnotationsToolbar.setVisibility(View.VISIBLE);
        }
    }
    @Override
    public void onAnnotationsDone() {
        restartAnnotations();
        isAnnotations = false;
    }

    @Override
    public void onError(String error) {

    }

    //Basic Listener from OTWrapper
    private BasicListener mBasicListener =
            new PausableBasicListener(new BasicListener<OTWrapper>() {

                @Override
                public void onConnected(OTWrapper otWrapper, int participantsCount, String connId, String data) throws ListenerException {
                    Log.i(LOG_TAG, "Connected to the session. Number of participants: " + participantsCount);
                    if (mWrapper.getOwnConnId() == connId) {
                        isConnected = true;
                        mProgressDialog.dismiss();
                    }
                    else {
                        mRemoteConnId = connId;
                    }
                }

                @Override
                public void onDisconnected(OTWrapper otWrapper, int participantsCount, String connId, String data) throws ListenerException {
                    Log.i(LOG_TAG, "Connection dropped: " + connId);
                    if (connId == mWrapper.getOwnConnId()) {
                        Log.i(LOG_TAG, "Disconnected to the session");
                        cleanViewsAndControls();
                    }
                }

                @Override
                public void onPreviewViewReady(OTWrapper otWrapper, View localView) throws ListenerException {
                    if (isScreensharing) {
                        mScreenSharingView = localView;
                    }
                    else {
                        setLocalView(localView);
                    }
                }

                @Override
                public void onPreviewViewDestroyed(OTWrapper otWrapper, View localView) throws ListenerException {
                    Log.i(LOG_TAG, "Local preview view is destroyed");
                    setLocalView(null);
                }

                @Override
                public void onRemoteViewReady(OTWrapper otWrapper, View remoteView, String remoteId, String data) throws ListenerException {
                    Log.i(LOG_TAG, "Remove view is ready");
                    if (remoteId == mRemoteId) {
                        if (isCallInProgress()) {
                            setRemoteView(remoteView, remoteId);
                        }
                    }
                    else {
                        if ( mWrapper.getRemoteStreamStatus(remoteId).getType() == StreamStatus.StreamType.SCREEN ) {
                            setRemoteView(remoteView, remoteId);
                            mScreenRemoteId = remoteId;
                        }
                    }
                }

                @Override
                public void onRemoteViewDestroyed(OTWrapper otWrapper, View remoteView, String remoteId) throws ListenerException {
                    Log.i(LOG_TAG, "Remote view is destroyed");
                    setRemoteView(null, remoteId);
                    if ( remoteId == mRemoteId ){
                        mRemoteId = null;
                    }
                    else {
                        if ( remoteId == mScreenRemoteId ){
                            mScreenRemoteId = null;
                        }
                    }
                    reloadViews();
                }

                @Override
                public void onStartedPublishingMedia(OTWrapper otWrapper, boolean screensharing) throws ListenerException {
                    Log.i(LOG_TAG, "Local started streaming video.");

                    //Check if there are some connected remotes
                    checkRemotes();

                    if (screensharing) {
                        screenAnnotations();
                    }
                }

                @Override
                public void onStoppedPublishingMedia(OTWrapper otWrapper, boolean isScreensharing) throws ListenerException {
                    Log.i(LOG_TAG, "Local stopped streaming video.");
                }

                @Override
                public void onRemoteJoined(OTWrapper otWrapper, String remoteId) throws ListenerException {
                    Log.i(LOG_TAG, "A new remote joined.");
                    if (mRemoteId == null) { //one-to-one, the first to arrive, will be the used
                        mRemoteId = remoteId;
                        initRemoteFragment(remoteId);
                    }
                    else {
                        //check remote screen
                        if ( mWrapper.getRemoteStreamStatus(remoteId).getType() == StreamStatus.StreamType.SCREEN ) {
                            mScreenRemoteId = remoteId;
                        }
                    }
                }

                @Override
                public void onRemoteLeft(OTWrapper otWrapper, String remoteId) throws ListenerException {
                    Log.i(LOG_TAG, "A new remote left.");
                    if (mRemoteId != null && remoteId == mRemoteId) { //one-to-one
                        mRemoteId = null;
                    }
                    else {
                        if ( mScreenRemoteId != null && remoteId == mScreenRemoteId ){
                            mScreenRemoteId = null;
                        }
                    }
                }

                @Override
                public void onRemoteVideoChanged(OTWrapper otWrapper, String remoteId, String reason, boolean videoActive, boolean subscribed) throws ListenerException {
                    Log.i(LOG_TAG, "Remote video changed");
                    if (isCallInProgress) {
                        if (reason.equals("quality")) {
                            //network quality alert
                            mAlert.setBackgroundResource(R.color.quality_alert);
                            mAlert.setTextColor(MainActivity.this.getResources().getColor(R.color.white));
                            mAlert.bringToFront();
                            mAlert.setVisibility(View.VISIBLE);
                            mAlert.postDelayed(new Runnable() {
                                public void run() {
                                    mAlert.setVisibility(View.GONE);
                                }
                            }, 7000);
                        }

                        if (!videoActive) {
                            onAudioOnly(true); //video is not active
                        } else {
                            onAudioOnly(false);
                        }
                    }
                }

                @Override
                public void onError(OTWrapper otWrapper, OpentokError error) throws ListenerException {
                    Log.i(LOG_TAG, "Error " + error.getErrorCode() + "-" + error.getMessage());
                    Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
                    mWrapper.disconnect(); //end communication
                    mProgressDialog.dismiss();
                    cleanViewsAndControls(); //restart views
                }
            });

    //Advanced Listener from OTWrapper
    private AdvancedListener mAdvancedListener =
            new PausableAdvancedListener(new AdvancedListener<OTWrapper>() {

                @Override
                public void onCameraChanged(OTWrapper otWrapper) throws ListenerException {
                    Log.i(LOG_TAG, "The camera changed");
                }

                @Override
                public void onReconnecting(OTWrapper otWrapper) throws ListenerException {
                    Log.i(LOG_TAG, "The session is reconnecting.");
                    Toast.makeText(MainActivity.this, R.string.reconnecting, Toast.LENGTH_LONG).show();
                }

                @Override
                public void onReconnected(OTWrapper otWrapper) throws ListenerException {
                    Log.i(LOG_TAG, "The session reconnected.");
                    Toast.makeText(MainActivity.this, R.string.reconnected, Toast.LENGTH_LONG).show();
                }

                @Override
                public void onVideoQualityWarning(OTWrapper otWrapper, String remoteId) throws ListenerException {
                    Log.i(LOG_TAG, "The quality has degraded");

                    mAlert.setBackgroundResource(R.color.quality_warning);
                    mAlert.setTextColor(MainActivity.this.getResources().getColor(R.color.warning_text));

                    mAlert.bringToFront();
                    mAlert.setVisibility(View.VISIBLE);
                    mAlert.postDelayed(new Runnable() {
                        public void run() {
                            mAlert.setVisibility(View.GONE);
                        }
                    }, 7000);
                }

                @Override
                public void onVideoQualityWarningLifted(OTWrapper otWrapper, String remoteId) throws ListenerException {
                    Log.i(LOG_TAG, "The quality has improved");
                }

                @Override
                public void onError(OTWrapper otWrapper, OpentokError error) throws ListenerException {
                    Log.i(LOG_TAG, "Error " + error.getErrorCode() + "-" + error.getMessage());
                    Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
                    mWrapper.disconnect(); //end communication
                    mProgressDialog.dismiss();
                    cleanViewsAndControls(); //restart views
                }
            });

    private void checkRemotes(){
        if ( mRemoteId != null ){
            if (!mWrapper.isReceivedMediaEnabled(mRemoteId, MediaType.VIDEO)){
                onAudioOnly(true);
            }
            else {
                setRemoteView(mWrapper.getRemoteStreamStatus(mRemoteId).getView(), mRemoteId);
            }
        }
        if ( mScreenRemoteId != null ){
            setRemoteView(mWrapper.getRemoteStreamStatus(mScreenRemoteId).getView(), mScreenRemoteId);
        }
    }

    private void saveScreencapture(Bitmap bmp) {
        if (bmp != null) {
            Bitmap annotationsBmp = null;
            Bitmap overlayBmp = null;
            if ( mRemoteAnnotationsView != null ){
                annotationsBmp= getBitmapFromView(mRemoteAnnotationsView);
                overlayBmp = mergeBitmaps(bmp, annotationsBmp);
            }
            else {
                overlayBmp = bmp;
            }

            String filename;
            Date date = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            filename = sdf.format(date);
            try {
                String path = Environment.getExternalStorageDirectory().toString();
                OutputStream fOut = null;
                File file = new File(path, filename + ".jpg");
                fOut = new FileOutputStream(file);

                overlayBmp.compress(Bitmap.CompressFormat.JPEG, 85, fOut);
                fOut.flush();
                fOut.close();

                MediaStore.Images.Media.insertImage(getContentResolver()
                        , file.getAbsolutePath(), file.getName(), file.getName());

                openScreenshot(file);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private Bitmap getBitmapFromView(View view) {
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(),Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        Drawable bgDrawable =view.getBackground();
        if (bgDrawable!=null)
            bgDrawable.draw(canvas);
        view.draw(canvas);
        return returnedBitmap;
    }

    private Bitmap mergeBitmaps(Bitmap bmp1, Bitmap bmp2){
        Bitmap bmpOverlay = Bitmap.createBitmap(bmp1.getWidth(), bmp1.getHeight(), bmp1.getConfig());
        bmp2 = Bitmap.createScaledBitmap(bmp2, bmp1.getWidth(), bmp1.getHeight(),
                true);
        Canvas canvas = new Canvas(bmpOverlay);
        canvas.drawBitmap(bmp1, 0,0, null);
        canvas.drawBitmap(bmp2, 0,0, null);

        return bmpOverlay;
    }

    private void openScreenshot(File imageFile) {
        Uri uri = Uri.fromFile(imageFile);
        Intent intentSend = new Intent();
        intentSend.setAction(Intent.ACTION_SEND);
        intentSend.setType("image/*");

        intentSend.putExtra(Intent.EXTRA_SUBJECT, "");
        intentSend.putExtra(Intent.EXTRA_TEXT, "");
        intentSend.putExtra(Intent.EXTRA_STREAM, uri);
        startActivity(Intent.createChooser(intentSend, "Share Screenshot"));
    }

    private void restartAnnotations() {
        mCallToolbar.setVisibility(View.GONE);
        showAnnotationsToolbar(false);
    }

    private void showAnnotationsToolbar(boolean show) {
        if (show) {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mAnnotationsToolbar.getLayoutParams();
            params.addRule(RelativeLayout.ABOVE, mActionBarContainer.getId());
            mAnnotationsToolbar.setLayoutParams(params);
            mAnnotationsToolbar.setVisibility(View.VISIBLE);
            mActionBarContainer.setVisibility(View.VISIBLE);
        } else {
            mCallToolbar.setVisibility(View.GONE);
            mAnnotationsToolbar.setVisibility(View.GONE);
            mActionBarContainer.setVisibility(View.VISIBLE);
            if (mCountDownTimer != null) {
                mCountDownTimer.cancel();
                mCountDownTimer = null;
            }
            mAnnotationsToolbar.restart();
        }
    }

    private void showAll() {
        mCallToolbar.setVisibility(View.GONE);
        showAnnotationsToolbar(true);
        mCountDownTimer = new CountDownTimer(3000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                mCallToolbar.setVisibility(View.VISIBLE);
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mAnnotationsToolbar.getLayoutParams();
                params.addRule(RelativeLayout.ABOVE, mCallToolbar.getId());
                mAnnotationsToolbar.setLayoutParams(params);
                mAnnotationsToolbar.setVisibility(View.VISIBLE);
                mActionBarContainer.setVisibility(View.GONE);
            }
        }.start();
    }

    private void showScreensharingBar(boolean show){
        WindowManager wm = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
        if ( show ) {
            mScreensharingBar = new ScreenSharingBar(MainActivity.this, this);

            //add screensharing bar on top of the screen
            WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_PANEL,
                    0 | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);
            params.gravity = Gravity.LEFT | Gravity.TOP;
            params.x = 0;
            params.y = 0;


            wm.addView(mScreensharingBar, params);
        }
        else {
            wm.removeView(mScreensharingBar);
            mScreensharingBar = null;
        }
    }

    //Private methods
    private void initPreviewFragment() {
        mPreviewFragment = new PreviewControlFragment();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.actionbar_preview_fragment_container, mPreviewFragment).commit();

        if (isRemoteAnnotations || isAnnotations) {
            mPreviewFragment.enableAnnotations(true);
        }
    }

    private void initRemoteFragment(String remoteId) {
        mRemoteFragment = new RemoteControlFragment();

        Bundle args = new Bundle();
        args.putString("remoteId", remoteId);
        mRemoteFragment.setArguments(args);
        getSupportFragmentManager().beginTransaction()
                .add(R.id.actionbar_remote_fragment_container, mRemoteFragment).commit();
    }

    private void initCameraFragment() {
        mCameraFragment = new PreviewCameraFragment();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.camera_preview_fragment_container, mCameraFragment).commit();
    }

    private void cleanViewsAndControls() {
        mRemoteViewContainer.removeAllViews();
        if (isLocal) {
            isLocal = false;
            mPreviewViewContainer.removeAllViews();
        }
        if (mPreviewFragment != null)
            mPreviewFragment.restart();
        if (mRemoteFragment != null)
            mRemoteFragment.restart();
        if (mActionBarContainer != null)
            mActionBarContainer.setBackground(null);
    }

    private void reloadViews(){
        mRemoteViewContainer.removeAllViews();

        if ( mRemoteId != null ){
            setRemoteView(mWrapper.getRemoteStreamStatus(mRemoteId).getView(), mRemoteId);
        }
        if ( mScreenRemoteId != null ) {
            setRemoteView(mWrapper.getRemoteStreamStatus(mScreenRemoteId).getView(), mScreenRemoteId);
        }
    }

    private void showAVCall(boolean show) {
        if (show) {
            mPreviewViewContainer.setVisibility(View.VISIBLE);
            mRemoteViewContainer.setVisibility(View.VISIBLE);
            mCameraFragmentContainer.setVisibility(View.VISIBLE);
            mAnnotationsToolbar.setVisibility(View.GONE);
            mCallToolbar.setVisibility(View.GONE);
        } else {
            mPreviewViewContainer.setVisibility(View.GONE);
            mRemoteViewContainer.setVisibility(View.GONE);
            mCameraFragmentContainer.setVisibility(View.GONE);
        }
    }

    private void setLocalView(View localView){
        if (localView != null) {
            mPreviewViewContainer.removeAllViews();
            mRemoteViewContainer.removeAllViews();

            isLocal = true;
            layoutParamsPreview = new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            if ( mRemoteId != null || mScreenRemoteId != null ) {
                layoutParamsPreview.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM,
                        RelativeLayout.TRUE);
                layoutParamsPreview.addRule(RelativeLayout.ALIGN_PARENT_RIGHT,
                        RelativeLayout.TRUE);
                layoutParamsPreview.width = (int) getResources().getDimension(R.dimen.preview_width);
                layoutParamsPreview.height = (int) getResources().getDimension(R.dimen.preview_height);
                layoutParamsPreview.rightMargin = (int) getResources().getDimension(R.dimen.preview_rightMargin);
                layoutParamsPreview.bottomMargin = (int) getResources().getDimension(R.dimen.preview_bottomMargin);
            }
            mPreviewViewContainer.addView(localView, layoutParamsPreview);
        }
        else {
            mPreviewViewContainer.removeAllViews();
        }
    }
    private void screenAnnotations() {
        try {
            if ( isScreensharing ) {
                mScreenAnnotationsView = new AnnotationsView(this, mWrapper.getSession(), OpenTokConfig.API_KEY, true);
                //size of annotations screen, by default will be all the screen
                //take into account the calltoolbar as well
                mCallToolbar.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
                mAnnotationsToolbar.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
                mAnnotationsToolbar.getChildAt(0).measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED); //color toolbar

                Point display = new Point();
                getWindowManager().getDefaultDisplay().getSize(display);
                int height = display.y - mAnnotationsToolbar.getMeasuredHeight() - mCallToolbar.getMeasuredHeight() - mAnnotationsToolbar.getChildAt(0).getMeasuredHeight();
                int width =  getWindow().getDecorView().getRootView().getWidth();

                mScreenAnnotationsView.setLayoutParams(new ViewGroup.LayoutParams(width, height));
                mScreenAnnotationsView.attachToolbar(mAnnotationsToolbar);
                mScreenAnnotationsView.setVideoRenderer(mScreensharingRenderer);
                mScreenAnnotationsView.setAnnotationsListener(this);

                ((ViewGroup) mScreenSharingView).addView(mScreenAnnotationsView);
                mPreviewFragment.enableAnnotations(true);
                showScreensharingBar(true);
            }
        } catch (Exception e) {
            Log.i(LOG_TAG, "Exception - enableRemoteAnnotations: " + e);
        }
    }

    private void remoteAnnotations() {
        try {
            mRemoteAnnotationsView = new AnnotationsView(this, mWrapper.getSession(), OpenTokConfig.API_KEY, mRemoteConnId);
            mRemoteAnnotationsView.setVideoRenderer(mRemoteRenderer);
            mRemoteAnnotationsView.attachToolbar(mAnnotationsToolbar);
            mRemoteAnnotationsView.setAnnotationsListener(this);
            ((ViewGroup) mRemoteViewContainer).addView(mRemoteAnnotationsView);
            mPreviewFragment.enableAnnotations(true);
        } catch (Exception e) {
            Log.i(LOG_TAG, "Exception - enableRemoteAnnotations: " + e);
        }
    }

    private void setRemoteView(View remoteView, String remoteId){
        if (mPreviewViewContainer.getChildCount() > 0) {
            setLocalView(mPreviewViewContainer.getChildAt(0)); //main preview view
        }

        if (remoteView != null) {
            if (mWrapper.getRemoteStreamStatus(remoteId).getType() == StreamStatus.StreamType.SCREEN) {
                Log.i(LOG_TAG, "remote view screen");
                mRemoteViewContainer.removeAllViews();
                //force landscape
                if (mWrapper.getRemoteStreamStatus(remoteId).getWidth() > mWrapper.getRemoteStreamStatus(remoteId).getHeight()) {
                    forceLandscape();
                }
                //show remote view
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                        this.getResources().getDisplayMetrics().widthPixels, this.getResources()
                        .getDisplayMetrics().heightPixels);
                mRemoteViewContainer.addView(remoteView, layoutParams);
                remoteAnnotations();
                isRemoteAnnotations = true;
            } else {
                //show remote view
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                        this.getResources().getDisplayMetrics().widthPixels, this.getResources()
                        .getDisplayMetrics().heightPixels);
                mRemoteViewContainer.removeView(remoteView);
                mRemoteViewContainer.addView(remoteView, layoutParams);
                mRemoteViewContainer.setClickable(true);
                if ( mRemoteFragment != null )
                    mRemoteFragment.show();
            }
        } else { //view null --> remove view
            if (mRemoteViewContainer.getChildCount() > 0 ) {
                mRemoteViewContainer.removeAllViews();
            }
            mRemoteViewContainer.setClickable(false);
            mAudioOnlyView.setVisibility(View.GONE);
            restartAnnotations();
            restartOrientation();
        }
    }

    private void onAudioOnly(boolean enabled) {
        if (enabled) {
            mWrapper.getRemoteStreamStatus(mRemoteId).getView().setVisibility(View.GONE);
            mAudioOnlyView.setVisibility(View.VISIBLE);
        }
        else {
            mAudioOnlyView.setVisibility(View.GONE);
            mWrapper.getRemoteStreamStatus(mRemoteId).getView().setVisibility(View.VISIBLE);
        }
    }

    private int dpToPx(int dp) {
        double screenDensity = this.getResources().getDisplayMetrics().density;
        return (int) (screenDensity * (double) dp);
    }

    private void forceLandscape() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    private void restartOrientation() {
        setRequestedOrientation(mOrientation);
    }
}


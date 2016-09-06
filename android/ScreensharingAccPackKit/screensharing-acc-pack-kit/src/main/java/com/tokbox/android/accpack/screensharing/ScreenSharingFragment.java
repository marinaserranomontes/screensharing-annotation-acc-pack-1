package com.tokbox.android.accpack.screensharing;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Presentation;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.opentok.android.Connection;
import com.opentok.android.OpentokError;
import com.opentok.android.PublisherKit;
import com.opentok.android.Session;
import com.opentok.android.Stream;
import com.opentok.android.Subscriber;
import com.tokbox.android.accpack.AccPackSession;


import com.tokbox.android.annotations.AnnotationsToolbar;
import com.tokbox.android.annotations.AnnotationsView;
import com.tokbox.android.accpack.screensharing.config.OpenTokConfig;
import com.tokbox.android.annotations.utils.AnnotationsVideoRenderer;
import com.tokbox.android.logging.OTKAnalytics;
import com.tokbox.android.logging.OTKAnalyticsData;

import java.util.Random;
import java.util.UUID;


public class ScreenSharingFragment extends Fragment implements AccPackSession.SessionListener, PublisherKit.PublisherListener, AccPackSession.SignalListener, ScreenSharingBar.ScreenSharingBarListener{

    private static final String LOG_TAG = ScreenSharingFragment.class.getSimpleName();
    private static final String STATE_RESULT_CODE = "result_code";
    private static final String STATE_RESULT_DATA = "result_data";
    private static final String ERROR = "ScreenSharing error";
    private static final int REQUEST_MEDIA_PROJECTION = 1;

    private AccPackSession mSession;
    private ScreenPublisher mScreenPublisher;
    private String mApiKey;
    private boolean isConnected;

    private ScreenSharingListener mListener;

    private VirtualDisplay mVirtualDisplay;
    private int mDensity;
    private int mWidth;
    private int mHeight;

    private MediaProjectionManager mMediaProjectionManager;
    private MediaProjection mMediaProjection;
    private ImageReader mImageReader;


    private int mResultCode;
    private Intent mResultData;

    private RelativeLayout mScreenView;

    private AnnotationsView mAnnotationsView;
    private AnnotationsToolbar mAnnotationsToolbar;
    private AnnotationsToolbar mRemoteAnnotationsToolbar;

    private RelativeLayout mScreensharingBar;
    private View mScreensharingLeftView;
    private View mScreensharingRightView;
    private View mScreensharingBottomView;
    private ScreenSharingBar screensharingBar;

    private boolean isStarted = false;
    private boolean isAnnotationsEnabled = false;
    private boolean isRemoteAnnotationsEnabled = false;
    private boolean isAudioEnabled = true;

    private ViewGroup mScreen;

    private AnnotationsVideoRenderer mRenderer;
    private View rootView;

    private OTKAnalyticsData mAnalyticsData;
    private OTKAnalytics mAnalytics;

    private MyPresentation mPresentation = null;


    @Override
    public void onSignalReceived(Session session, String type, String data, Connection connection) {
        if (type.equals("annotations")){
            Log.i(LOG_TAG, "New annotation received");
        }
    }

    @Override
    public void onClose() {
        addLogEvent(OpenTokConfig.LOG_ACTION_CLOSE, OpenTokConfig.LOG_VARIATION_ATTEMPT);
        if (isStarted) {
            mAnnotationsView.restart();
            mAnnotationsView = null;
            stop();
        }
        addLogEvent(OpenTokConfig.LOG_ACTION_CLOSE, OpenTokConfig.LOG_VARIATION_SUCCESS);
    }

    /**
     * Monitors state changes in the ScreenSharingFragment.
     *
     */
    public interface ScreenSharingListener {

        /**
         * Invoked when screensharing started.
         *
         */
        void onScreenSharingStarted();

        /**
         * Invoked when screensharing stopped.
         *
         */
        void onScreenSharingStopped();


        /**
         * Invoked when a screen sharing error occurs.
         *
         * @param error The error message.
         */
        void onScreenSharingError(String error);


        /**
         * Invoked when the annotations view has been added to the screensharing view.
         *
         * @param view The annotations view.
         */
        void onAnnotationsViewReady(AnnotationsView view);

        /**
         * Invoked when the remote annotations view is ready.
         *
         * @param view The remote annotations view.
         */

        void onAnnotationsRemoteViewReady(AnnotationsView view);

        /**
         * Invoked when the close button is clicked.
         *
         */
        void onClosed();

    }

    /*
     * Constructor.
     */
    public ScreenSharingFragment(){

    }

    public static ScreenSharingFragment newInstance(AccPackSession session, String apiKey) {

        ScreenSharingFragment fragment = new ScreenSharingFragment();

        fragment.mSession = session;
        fragment.mSession.setSessionListener(fragment);
        fragment.mApiKey = apiKey;

        return fragment;
    }

    private void init(){
        //Analytics logging
        String source = getContext().getPackageName();

        SharedPreferences prefs = getContext().getSharedPreferences("opentok", Context.MODE_PRIVATE);
        String guidVSol = prefs.getString("guidVSol", null);
        if (null == guidVSol) {
            guidVSol = UUID.randomUUID().toString();
            prefs.edit().putString("guidVSol", guidVSol).commit();
        }

        mAnalyticsData = new OTKAnalyticsData.Builder(OpenTokConfig.LOG_CLIENT_VERSION, source, OpenTokConfig.LOG_COMPONENTID, guidVSol).build();
        mAnalytics = new OTKAnalytics(mAnalyticsData);

        checkSessionInfo();

        addLogEvent(OpenTokConfig.LOG_ACTION_INITIALIZE, OpenTokConfig.LOG_VARIATION_SUCCESS);
    }

    private void checkSessionInfo(){
        if ( mAnalytics != null ){
            if ( mSession != null ) {
                mAnalyticsData.setSessionId(mSession.getSessionId());
                mAnalyticsData.setConnectionId(mSession.getConnection().getConnectionId());
            }
            if ( mApiKey != null ) {
                mAnalyticsData.setPartnerId(mApiKey);
            }
            mAnalytics. setData(mAnalyticsData);
        }
    }

    /*
     * Set the screen sharing listener.
     * @param mListener The screen sharing listener.
     */
    public void setListener(ScreenSharingListener mListener) {
        this.mListener = mListener;
    }

    /*
    * Start sharing the screen.
    */
    public void start(){
        init();
        if (isConnected) {
            checkSessionInfo(); //add session info to the logging

            if (mVirtualDisplay == null) {
                addLogEvent(OpenTokConfig.LOG_ACTION_START, OpenTokConfig.LOG_VARIATION_ATTEMPT);
                startScreenCapture();
            }
        }
    }

    /*
    * Stop sharing the screen.
    */
    public void stop(){
        addLogEvent(OpenTokConfig.LOG_ACTION_END, OpenTokConfig.LOG_VARIATION_ATTEMPT);
        stopScreenCapture();
        if (mScreenPublisher != null) {
            mSession.unpublish(mScreenPublisher);
        }
    }


    /*
    * Check whether screensharing has started.
    * @return <code>true</code> if screensharing started; <code>false</code> otherwise.
    */
    public boolean isStarted() {
        return isStarted;
    }


    /*
    * Enable or disable the annotations in the screensharing.
    * @param annotationsEnabled <code>true</code> if annotations are enabled; <code>false</code> otherwise.
    * @param toolbar The annotations toolbar.
    */
    public void enableAnnotations(boolean annotationsEnabled, AnnotationsToolbar toolbar) {
        isAnnotationsEnabled = annotationsEnabled;
        mAnnotationsToolbar = toolbar;

        //add logging info
        if (annotationsEnabled){
            addLogEvent(OpenTokConfig.LOG_ACTION_ENABLE_ANNOTATIONS, OpenTokConfig.LOG_VARIATION_SUCCESS);
        }
        else {
            addLogEvent(OpenTokConfig.LOG_ACTION_DISABLE_ANNOTATIONS, OpenTokConfig.LOG_VARIATION_SUCCESS);
        }
    }

    public void enableRemoteAnnotations(boolean annotationsEnabled, AnnotationsToolbar toolbar, ViewGroup view, Subscriber subscriber) {
        //TODO connectionId from STREAM instead of sessioonconnectionId
        AnnotationsView remoteAnnotationsView = new AnnotationsView(getContext(), mSession, mApiKey, false, AnnotationsView.ViewType.SubscriberView);

        AnnotationsVideoRenderer renderer = new AnnotationsVideoRenderer(getContext());
        subscriber.setRenderer(renderer);
        remoteAnnotationsView.setVideoRenderer(renderer);

        mRemoteAnnotationsToolbar = toolbar;
        remoteAnnotationsView.attachToolbar(mRemoteAnnotationsToolbar);
        isRemoteAnnotationsEnabled  = annotationsEnabled;

        onAnnotationsRemoteViewReady(remoteAnnotationsView);
        ((ViewGroup)view).addView(remoteAnnotationsView);
    }

    /*
    * Enable or disable the audio in the screensharing.
    * @param enabled <code>true</code> if  the audio is enabled; <code>false</code> otherwise.
    */
    public void enableAudioScreensharing(boolean enabled) {
        isAudioEnabled = enabled;

        //add logging info
        if (enabled){
            addLogEvent(OpenTokConfig.LOG_ACTION_ENABLE_SCREENSHARING_AUDIO, OpenTokConfig.LOG_VARIATION_SUCCESS);
        }
        else {
            addLogEvent(OpenTokConfig.LOG_ACTION_DISABLE_SCREENSHARING_AUDIO, OpenTokConfig.LOG_VARIATION_SUCCESS);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
     /*   if (isStarted) {
            stop();
        }*/
    }

    @Override
    public void onResume() {
        super.onResume();
       /* if (isStarted) {
            start();
        }*/
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        tearDownMediaProjection();
        addLogEvent(OpenTokConfig.LOG_ACTION_DESTROY, OpenTokConfig.LOG_VARIATION_SUCCESS);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mResultCode = savedInstanceState.getInt(STATE_RESULT_CODE);
            mResultData = savedInstanceState.getParcelable(STATE_RESULT_DATA);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.main_layout, container, false);

        mScreensharingBar = (RelativeLayout) rootView.findViewById(R.id.screnesharing_bar);
        mScreensharingLeftView = (View) rootView.findViewById(R.id.left_line);
        mScreensharingRightView = (View) rootView.findViewById(R.id.right_line);
        mScreensharingBottomView = (View) rootView.findViewById(R.id.bottom_line);

        mScreen = container;

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Activity activity = getActivity();
        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        mDensity = metrics.densityDpi;
        mMediaProjectionManager = (MediaProjectionManager)
                activity.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mResultData != null) {
            outState.putInt(STATE_RESULT_CODE, mResultCode);
            outState.putParcelable(STATE_RESULT_DATA, mResultData);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_MEDIA_PROJECTION) {
            if (resultCode != Activity.RESULT_OK) {
                Log.i(LOG_TAG, "User cancelled screensharing permission");
                onScreenSharingError(ERROR + ": User cancelled screensharing permission");
                return;
            }
            Activity activity = getActivity();
            if (activity == null) {
                onScreenSharingError(ERROR + ": Activity is null");
                return;
            }
            Log.i(LOG_TAG, "Starting screen capture");
            mResultCode = resultCode;
            mResultData = data;
            setUpMediaProjection();
            setUpVirtualDisplay();

        }
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setUpMediaProjection() {
        mMediaProjection = mMediaProjectionManager.getMediaProjection(mResultCode, mResultData);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setUpVirtualDisplay() {

        // display metrics
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        mDensity = metrics.densityDpi;
        Display mDisplay = getActivity().getWindowManager().getDefaultDisplay();

        Point size = new Point();
        mDisplay.getRealSize(size);
        mWidth = size.x;
        mHeight = size.y;

        // start capture reader
        mImageReader = ImageReader.newInstance(mWidth, mHeight, PixelFormat.RGBA_8888, 2);
        int flags = DisplayManager.VIRTUAL_DISPLAY_FLAG_PRESENTATION;

        mVirtualDisplay = mMediaProjection.createVirtualDisplay("ScreenCapture", mWidth, mHeight, mDensity, flags, mImageReader.getSurface(), null, null);

        size.set(mWidth, mHeight);

        //create ScreenCapturer
        ScreenSharingCapturer capturer = new ScreenSharingCapturer(getContext(), mScreen, mImageReader);

        mScreenPublisher = new ScreenPublisher(getContext(), "screenPublisher", isAudioEnabled, true, capturer);
        mScreenPublisher.setPublisherVideoType(PublisherKit.PublisherKitVideoType.PublisherKitVideoTypeScreen);
        mScreenPublisher.setPublisherListener(this);

        mRenderer = new AnnotationsVideoRenderer(getContext());
        mScreenPublisher.setRenderer(mRenderer);

        mSession.publish(mScreenPublisher);

    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void tearDownMediaProjection() {
        if (mMediaProjection != null) {
            mMediaProjection.stop();
            mMediaProjection = null;
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void startScreenCapture() {

        if (mMediaProjection != null) {
            Log.i(LOG_TAG, "mMediaProjection != null");

            setUpVirtualDisplay();
        } else if (mResultCode != 0 && mResultData != null) {
            Log.i(LOG_TAG, "mResultCode != 0 && mResultData != null");
            setUpMediaProjection();
            setUpVirtualDisplay();
        } else {
            Log.i(LOG_TAG, "Requesting confirmation");
            startActivityForResult(
                    mMediaProjectionManager.createScreenCaptureIntent(),
                    REQUEST_MEDIA_PROJECTION);
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void stopScreenCapture() {
        if (mVirtualDisplay == null) {
            return;
        }
        mVirtualDisplay.release();
        mVirtualDisplay = null;

        tearDownMediaProjection();
    }

    protected void onScreenSharingStarted(){
        if ( mListener != null ){
            mListener.onScreenSharingStarted();
            addLogEvent(OpenTokConfig.LOG_ACTION_START, OpenTokConfig.LOG_VARIATION_SUCCESS);
        }
    }

    protected void onScreenSharingStopped(){
        if ( mListener != null ){
            mListener.onScreenSharingStopped();
            addLogEvent(OpenTokConfig.LOG_ACTION_END, OpenTokConfig.LOG_VARIATION_SUCCESS);
        }
    }

    protected void onScreenSharingError(String error){
        if ( mListener != null ){
            mListener.onScreenSharingError(error);
        }
    }

    protected void onClosed(){
        if ( mListener != null ){
            mListener.onClosed();
        }
    }

    protected void onAnnotationsViewReady(AnnotationsView view){
        if ( mListener != null ){
            mListener.onAnnotationsViewReady(view);
        }
    }

    protected void onAnnotationsRemoteViewReady(AnnotationsView view){
        if ( mListener != null ){
            mListener.onAnnotationsRemoteViewReady(view);
        }
    }

    @Override
    public void onConnected(Session session) {
        isConnected = true;
    }

    @Override
    public void onDisconnected(Session session) {
        isConnected = false;
    }

    @Override
    public void onStreamReceived(Session session, Stream stream) {

    }

    @Override
    public void onStreamDropped(Session session, Stream stream) {

    }

    @Override
    public void onError(Session session, OpentokError opentokError) {
        stop();
        onScreenSharingError(ERROR + ": "+ opentokError.toString());
    }

    @Override
    public void onStreamCreated(PublisherKit publisherKit, Stream stream) {

        enableScreensharingBar(true);
        onScreenSharingStarted();
        checkAnnotations();
        isStarted = true;


        if ( isAnnotationsEnabled ) {
            if (mAnnotationsView == null) {
                mAnnotationsView = new AnnotationsView(getContext(), mSession, mApiKey, true, AnnotationsView.ViewType.PublisherView);
                mAnnotationsView.attachToolbar(mAnnotationsToolbar);
                mAnnotationsView.setVideoRenderer(mRenderer); //to use screencapture
            }
            onAnnotationsViewReady(mAnnotationsView);
            mScreen.addView(mAnnotationsView);
        }
        screensharingBar = new ScreenSharingBar(getContext(), this);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        mScreen.addView(screensharingBar, params);


    }

    private void enableScreensharingBar(boolean visible){
        if (visible) {
            mScreensharingBottomView.setVisibility(View.VISIBLE);
            mScreensharingRightView.setVisibility(View.VISIBLE);
            mScreensharingLeftView.setVisibility(View.VISIBLE);
        }
        else {
            mScreensharingBottomView.setVisibility(View.GONE);
            mScreensharingRightView.setVisibility(View.GONE);
            mScreensharingLeftView.setVisibility(View.GONE);
        }
    }

    private void checkAnnotations() {
        if (isAnnotationsEnabled) {
            if (mAnnotationsToolbar.getVisibility() == View.VISIBLE) {
                mAnnotationsToolbar.setVisibility(View.GONE);
            } else {
                mAnnotationsToolbar.setVisibility(View.VISIBLE);
                mAnnotationsToolbar.restart();
            }

            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams)mAnnotationsToolbar.getLayoutParams();
            params.leftMargin = dpToPx(5);
            params.rightMargin = dpToPx(5);
            params.bottomMargin = dpToPx(5);

            mAnnotationsToolbar.setLayoutParams(params);
        }
    }

    private int dpToPx(int dp) {
        double screenDensity = this.getResources().getDisplayMetrics().density;
        return (int) (screenDensity * (double) dp);
    }

    @Override
    public void onStreamDestroyed(PublisherKit publisherKit, Stream stream) {
        mScreenPublisher = null;
        mScreen.removeView(screensharingBar);
        mScreen.removeView(mAnnotationsView);
        enableScreensharingBar(false);
        checkAnnotations();
        onScreenSharingStopped();
        onClosed();
        isStarted = false;
    }

    @Override
    public void onError(PublisherKit publisherKit, OpentokError opentokError) {
        onScreenSharingError(ERROR + ": "+opentokError.getMessage());
        if (isStarted()){
            addLogEvent(OpenTokConfig.LOG_ACTION_END, OpenTokConfig.LOG_VARIATION_ERROR);
        }
        else {
            addLogEvent(OpenTokConfig.LOG_ACTION_START, OpenTokConfig.LOG_VARIATION_ERROR);
        }
    }

    //add log events
    private void addLogEvent(String action, String variation){
        if ( mAnalytics!= null ) {
            mAnalytics.logEvent(action, variation);
        }
    }

    MySurfaceView mySurfaceView;

    class MySurfaceView extends SurfaceView implements Runnable{

        Thread thread = null;
        SurfaceHolder surfaceHolder;
        volatile boolean running = false;

        private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        Random random;

        public MySurfaceView(Context context) {
            super(context);
            // TODO Auto-generated constructor stub
            surfaceHolder = getHolder();
            setZOrderOnTop(true);
            surfaceHolder.setFormat(PixelFormat.TRANSLUCENT);
            random = new Random();
        }

        public void onResumeMySurfaceView(){
            running = true;
            thread = new Thread(this);
            thread.start();
        }

        public void onPauseMySurfaceView(){
            boolean retry = true;
            running = false;
            while(retry){
                try {
                    thread.join();
                    retry = false;
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void run() {
            // TODO Auto-generated method stub
            while(running){
                if(surfaceHolder.getSurface().isValid()){
                    Canvas canvas = surfaceHolder.lockCanvas();
                    //... actual drawing on canvas

                    paint.setStyle(Paint.Style.STROKE);
                    paint.setStrokeWidth(3);

                    int w = canvas.getWidth();
                    int h = canvas.getHeight();
                    int x = random.nextInt(w-1);
                    int y = random.nextInt(h-1);
                    int r = random.nextInt(255);
                    int g = random.nextInt(255);
                    int b = random.nextInt(255);
                    paint.setColor(0xff000000 + (r << 16) + (g << 8) + b);
                    //canvas.drawPoint(x, y, paint);

                    x = 50;
                    y = 50;
                    int sideLength = 200;

                    // create a rectangle that we'll draw later
                    Rect rectangle = new Rect(x, y, sideLength, sideLength);

                    // create the Paint and set its color
                    paint = new Paint();
                    paint.setColor(Color.BLUE);
                    canvas.drawRect(rectangle, paint);

                    surfaceHolder.unlockCanvasAndPost(canvas);
                }
            }
        }

    }
    private static void populate(View v, Display display)
    {
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        float density = metrics.density;
        TextView actual = (TextView) v.findViewById(R.id.actual);
        if (actual != null)
        {
            actual.setText(String.format("%dx%d", metrics.widthPixels,
                    metrics.heightPixels));
        }
        TextView df = (TextView) v.findViewById(R.id.density_factor);
        if (df != null)
        {
            df.setText(String.format("%f", density));
        }
        TextView dp = (TextView) v.findViewById(R.id.device_pixels);
        if (dp != null)
        {
            dp.setText(String.format("%dx%d",
                    ((int) ((float) metrics.widthPixels / density)),
                    ((int) ((float) metrics.heightPixels / density))));
        }
    }

    public class MyPresentation extends Presentation
    {

        public MyPresentation(Context outerContext,
                              Display display)
        {
            super(outerContext, display);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.presentation_layout);
            populate(findViewById(R.id.main),
                    getDisplay());
        }
    }
}

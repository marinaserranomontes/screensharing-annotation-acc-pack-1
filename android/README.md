![logo](../tokbox-logo.png)

# OpenTok Screensharing with Annotations Sample App for Android

## Quick start

This section shows you how to prepare, build, and run the sample application.

### Importing the sample app

1. Clone [the OpenTok Screensharing with Annotations Sample App repository](https://github.com/opentok/screensharing-annotation-acc-pack/tree/master/android) from GitHub.
2. Start Android Studio.
3. In the **Quick Start** panel, click **Open an existing Android Studio Project**.
4. Navigate to the **android** folder, select the **OneToOneScreenSharingSample** folder, and click **Choose**.

### Adding the OpenTok Accelerator Core and Annotation libraries

Take a look at [OpenTok Accelerator Core](https://github.com/opentok/accelerator-core-android) and [OpenTok Accelerator Annotation](https://github.com/opentok/accelerator-annotation-android).


### Configure and build the app

Configure the sample app code. Then, build and run the app.

1. Get values for **API Key**, **Session ID**, and **Token**. See the [Screensharing Annotation Sample home page](../README.md) for important information.

2. In Android Studio, open **OpenTokConfig.java** and replace the following empty strings with the corresponding **Session ID**, **Token**, and **API Key** values:

    ```java
    // Replace with a generated Session ID
    public static final String SESSION_ID = "";

    // Replace with a generated token
    public static final String TOKEN = "";

    // Replace with your OpenTok API key
    public static final String API_KEY = "";
    ```
At this point you can try running the app! You can either use a simulator or an actual mobile device.

## Exploring the code

This section describes best practices the sample app code uses to deploy screen sharing with annotations. For detail about the APIs used to develop this library, see the [OpenTok Android SDK Reference](https://tokbox.com/developer/sdks/android/reference/) and [Android API Reference](http://developer.android.com/reference/packages.html).

The sample app design extends the [OpenTok One-to-One Communication Sample App](https://github.com/opentok/one-to-one-sample-apps/tree/master/one-to-one-sample-app/) and [OpenTok Accelerator Core for Android](https://github.com/opentok/accelerator-core-android/) by adding logic using the Screensharing feature in the Core library and the [OpenTok Accelerator Annotation](https://github.com/opentok/accelerator-annotation-android/) classes.


###Initialization

The class main activity implements all the necessary interfaces for the sample app

```java
public class MainActivity extends AppCompatActivity implements OneToOneCommunication.Listener,  
PreviewControlFragment.PreviewControlCallbacks, RemoteControlFragment.RemoteControlCallbacks, 
PreviewCameraFragment.PreviewCameraCallbacks, ScreenSharingFragment.ScreenSharingListener, 
AnnotationsView.AnnotationsListener 
```

First initialize``mComm``, the OneToOneCommunicator object

```java
mComm = new OneToOneCommunication(MainActivity.this, OpenTokConfig.SESSION_ID, OpenTokConfig.TOKEN, OpenTokConfig.API_KEY);
        mComm.setSubscribeToSelf(OpenTokConfig.SUBSCRIBE_TO_SELF);
        //set listener to receive the communication events, and add UI to these events
        mComm.setListener(this);
        mComm.init();
```        

Then, if there's no previous saved instance state, initialice the controls fragments

```java
if (savedInstanceState == null) {
            mFragmentTransaction = getSupportFragmentManager().beginTransaction();
            initCameraFragment(); //to swap camera
            initPreviewFragment(); //to enable/disable local media
            initRemoteFragment(); //to enable/disable remote media
            initScreenSharingFragment();//to start/stop sharing the screen
            mFragmentTransaction.commitAllowingStateLoss();
        }
```        
        
    
###Control Methods

Control the OneToOneCommunicator with these methods

| Method        | Description  |
| ------------- | ------------- |
| `onConfigurationChanged ` | Configuration changed, expect new ``Configuration`` object |
| `onCreateOptionsMenu ` | Create the options menu. |
| `onStart `   | Manage start control. |
| `onStop ` | Manage stop control. |
| `onRequestPermissionsResult `   | Expects permsRequestCode, permissions string and grantResults. 
| `getComm ` | Get OneToOneCommunicator object. |


###Events
Control the local call with these methods

| Events        | Description  |
| ------------- | ------------- |
| `onCall ` | Call button event |
| `onDisableLocalVideo ` | Video local button event. |
| `onDisableRemoteAudio `   | Audio remote button event. |
| `onDisableRemoteVideo` | Video remote button event. |
| `onCameraSwap ` | Camera control button event. |
| `onCallToolbar ` | Call Toolbar Control event.|
| `onClosed ` | Closed control event. |
| `saveScreencapture ` | Save screen capture event. |
| `showRemoteControlBar `   | Audio remote button event. |


###Screensharing events

Events that implement the screensharing functionality

| Events        | Description  |
| ------------- | ------------- 
| `onCall ` | Call button event |
| `isScreensharing ` | Gets Screen Sharing status. |
| `onScreenSharing `   | Screen Sharing control event. |
| `onScreenSharingStarted ` | Screen Sharing started event.|
| `onScreenSharingStopped ` | Screen Sharing stopped event. |
| `onScreenSharingError ` | Screen Sharing error event. Expects the error string |
| `openScreenshot ` | Open screenshot event.|

###Annotation events
Events that implement the annotation functionality


| Events        | Description  |
| ------------- | ------------- |
| `onAnnotations ` | Annotations control event. |
| `onAnnotationsViewReady ` | Annotation view ready event.|
| `restartAnnotations ` | Restart annotations event event. |
| `showAnnotationsToolbar ` | Show annotation toolbar event. |


####OneToOneCommunicator listener events

| Event        | Description  |
| ------------- | ------------- |
| `onInitialized ` | Initialization control event. |
| `onScreencaptureReady ` | Expect the bmp from the screen capture. |
| `onAnnotationsSelected `   | Expects the annotations view mode (``pen``or``text``). |
| `onAnnotationsDone ` | Restarts annotation after is done. |

####OneToOneCommunication callbacks

| Event        | Description  |
| ------------- | ------------- |
| `onError ` | Error callback |
| `onQualityWarning ` | Quality warning callback. |
| `onAudioOnly `   | Audio ronly callback. |
| `onPreviewReady ` | Preview ready callback. |
| `onRemoteViewReady ` | Remote view ready callback. |
| `onReconnecting ` | Reconnecting callback.|
| `onReconnected `   | Reconnected callback. |
| `onCameraChanged ` | Camera changed callback. |
| `remoteAnnotations ` | Camera control button callback. |






![logo](../tokbox-logo.png)

# OpenTok Screensharing with Annotations Sample for Android<br/>Version 1.1.0

## Quick start

This section shows you how to prepare, build, and run the sample application.

### Install the project files

1. Clone [the OpenTok Screensharing with Annotations Sample App repository](https://github.com/opentok/screensharing-annotation-acc-pack/tree/master/android) from GitHub.
2. Start Android Studio.
3. In the **Quick Start** panel, click **Open an existing Android Studio Project**.
4. Navigate to the **android** folder, select the **OneToOneScreenSharingSample** folder, and click **Choose**.


### Adding the Screensharing library

Use one of the following options to install the OpenTok Screensharing library, which includes the Annotations dependency:

  - [Using the repository](#using-the-repository)
  - [Using Maven](#using-maven)
  - [Downloading and Installing the AAR File](#downloading-and-installing-the-aar-file)

**NOTE**: The OpenTok Screensharing Sample App uses the [TokBox Common Accelerator Session Pack](https://github.com/opentok/acc-pack-common), the [OpenTok Screensharing Kit](https://github.com/opentok/screen-sharing-acc-pack), and the [OpenTok Annotations Kit] (https://github.com/opentok/annotation-acc-pack).

#### Using the repository

1. Clone the [OpenTok Screensharing Accelerator Pack repo](https://github.com/opentok/screen-sharing-acc-pack).
2. From the OpenTok Screensharing with Annotations Sample app project, right-click the app name and select **New > Module > Import Gradle Project**.
3. Navigate to the directory in which you cloned **OpenTok Screensharing Accelerator Pack**, select **screensharing-acc-pack-kit**, and click **Finish**.
4. Open the **build.gradle** file for the app and ensure the following lines have been added to the `dependencies` section:
```
compile project(':screensharing-acc-pack-kit')
```

#### Using Maven

1. Modify the `build.gradle` for your solution. Add the following code to the section labeled `repositories`:

    `maven { url  "http://tokbox.bintray.com/maven" }`

1. Modify the `build.gradle` for your activity. Add the following code to the section labeled `dependencies`:

    `compile 'com.opentok.android:opentok-screensharing-annotations:2.0.0'`

  _**NOTE**: Because dependencies are transitive with Maven, you do not need to explicitly reference the TokBox Common Accelerator Session Pack and the Annotations Kit with this option._


#### Downloading and Installing the AAR File

1.  Download the [Screensharing with Annotations Library AAR](https://s3.amazonaws.com/artifact.tokbox.com/solution/rel/screensharing-annotations-acc-pack/android/opentok-screensharing-annotations-2.0.0.zip).
1. Extract the **opentok-screensharing-annotations-2.0.0.aar** file.
1. Right-click the app name, select **Open Module Settings**, and click **+**.
1. Select **Import .JAR/.AAR Package** and click  **Next**.
1. Browse to the **Screensharing with Annotations library AAR** and click **Finish**.


### Configure and build the app

Configure the sample app code. Then, build and run the app.

1. Get values for **API Key**, **Session ID**, and **Token**. See the [Screensharing Annotation Sample home page](../README.md) for important information.

1. In Android Studio, open **OpenTokConfig.java** and replace the following empty strings with the corresponding **Session ID**, **Token**, and **API Key** values:

    ```java
    // Replace with a generated Session ID
    public static final String SESSION_ID = "";

    // Replace with a generated token
    public static final String TOKEN = "";

    // Replace with your OpenTok API key
    public static final String API_KEY = "";
    ```

1. Use Android Studio to build and run the app on an Android emulator or device.

1. By default, your app does not subscribe to its own stream. This feature is controlled by the following line of code open **OpenTokConfig.java**.

    ```java
    public static final boolean SUBSCRIBE_TO_SELF = false;
    ```

    To enable or disable the `SUBSCRIBE_TO_SELF` feature, you can invoke the `OneToOneCommunication.setSubscribeToSelf()` method:

    ```java
    OneToOneCommunication comm = new OneToOneCommunication(
      MainActivity.this,
      OpenTokConfig.SESSION_ID,
      OpenTokConfig.TOKEN,
      OpenTokConfig.API_KEY
    );

    comm.setSubscribeToSelf(OpenTokConfig.SUBSCRIBE_TO_SELF);

    ```

## Exploring the code

This section describes best practices the sample app code uses to deploy screen sharing with annotations.

The sample app design extends the [OpenTok One-to-One Communication Sample App](https://github.com/opentok/one-to-one-sample-apps/tree/master/one-to-one-sample-app/) and [OpenTok Common Accelerator Session Pack](https://github.com/opentok/acc-pack-common/) by adding logic using the `com.tokbox.android.accpack.screensharing` classes.

For details about developing with the SDK and the APIs this sample uses, see [OpenTok Android SDK Reference](https://tokbox.com/developer/sdks/android/reference/) and [Android API Reference](http://developer.android.com/reference/packages.html).


_**NOTE:** This sample app collects anonymous usage data for internal TokBox purposes only. Please do not modify or remove any logging code from this sample application._

### App design

This section focuses on screen sharing with annotations features. For information about one-to-one communication features, see the [OpenTok One-to-One Communication Sample App](https://github.com/opentok/one-to-one-sample-apps).

| Class        | Description  |
| ------------- | ------------- |
| `MainActivity`    | Implements the sample app UI and screen sharing with annotations callbacks. |
| `OpenTokConfig`   | Stores the information required to configure the session and connect to the cloud.   |


From the [ScreenSharingAccPack](https://github.com/opentok/screen-sharing-acc-pack).

| Class        | Description  |
| ------------- | ------------- |
| `ScreenSharingFragment`   | Provides the initializers and methods for the client screen sharing views. |
| `ScreenSharingCapturer`   | Provides TokBox custom support for sharing content displayed in the screen sharing area, overriding the mobile device OS default to share content captured by the camera. |
| `ScreenSharingBar`   | Initializes the screen sharing toolbar and its UI controls. |


From the [AnnotationsKit](https://github.com/opentok/annotation-acc-pack).

| Class        | Description  |
| ------------- | ------------- |
| `AnnotationsToolbar`   | Provides the initializers and methods for the annotation toolbar view, and initializes such functionality as text annotations, screen capture button, erase button that removes the last annotation that was added, a color selector for drawing stroke and text annotations, and scrolling features. You can customize this toolbar. |
| `AnnotationsView`   | Provides the rectangular area on the screen which is responsible for drawing annotations and event handling |


###  Screen sharing and annotation features

The `ScreenSharingFragment` class is the backbone of the screen sharing features for the app. It serves as a controller for the screen sharing UI widget, and initializes such functionality as stroke color and scrolling features. It uses the [`android.media.projection.MediaProjection`](http://developer.android.com/reference/android/media/projection/MediaProjection.html) class, supported on Android Lollipop, and provides the projection callbacks needed for screen sharing.

This class, which inherits from the [`android.support.v4.app.Fragment`](http://developer.android.com/intl/es/reference/android/support/v4/app/Fragment.html) class, sets up the screen sharing with annotations UI views and events, sets up session listeners, and defines a listener interface that is implemented in this example by the `MainActivity` class.

```java
public class ScreenSharingFragment
        extends    Fragment
        implements AccPackSession.SessionListener,
                   PublisherKit.PublisherListener,
                   AccPackSession.SignalListener,
                   ScreenSharingBar.ScreenSharingBarListener {

    . . .

}
```

Both the publisher (the participant sharing the screen) and the subscriber (the participant with whom the publisher’s screen is being shared) can annotate the shared screen. The `ScreenSharingListener` interface monitors state changes in the `ScreenSharingFragment`, and defines the following methods, which include callbacks for both the publisher and subscriber views:

```java
public interface ScreenSharingListener {

    void onScreenSharingStarted();
    void onScreenSharingStopped();
    void onScreenSharingError(String error);
    void onAnnotationsViewReady(AnnotationsView view); 

}
```


#### Initialization methods

The following `ScreenSharingFragment` methods are the main methods of the ScreensharingKit and provide basic information determining the behavior of the screen sharing with annotations functionality. They are used for such purposes as starting and stopping screensharing, as well as enabling and disabling publisher and subscriber annotations.

| Feature        | Methods  |
| ------------- | ------------- |
| Start screen capture.   | `start()`  |
| Stop screen capture.  | `stop()`  |
| Set the listener object to monitor state changes.   | `setListener()` |
| Sets whether annotations are enabled on the specified publisher toolbar.  | `enableAnnotations()`  |


#### Setting the Annotation Toolbar

To set up your annotation toolbar, instantiate a `ScreenSharingFragment` object and call the `setAnnotationsEnabled(boolean annotationsEnabled, AnnotationsToolbar toolbar)` method, setting the `annotationsEnabled` parameter to `true`.

For example, the following private method instantiates a `ScreenSharingFragment` object and enables the annotation toolbar in the Publisher:

```java
    private void initScreenSharingFragment(){

        mScreenSharingFragment = ScreenSharingFragment.newInstance(
          mComm.getSession(),
          OpenTokConfig.API_KEY
        );

        mScreenSharingFragment.enableAnnotations(true, mAnnotationsToolbar);

        mScreenSharingFragment.setListener(this);

        getSupportFragmentManager().beginTransaction()
                .add(
                      R.id.screensharing_fragment_container,
                      mScreenSharingFragment
                ).commit();
    }
```

To enable the annotation toolbar in the Subscriber:

```java
     try {
            AnnotationsView remoteAnnotationsView = new AnnotationsView(this, mComm.getSession(), OpenTokConfig.API_KEY, mComm.getRemote());

            AnnotationsVideoRenderer renderer = new AnnotationsVideoRenderer(this);
            mComm.getRemote().setRenderer(renderer);
            remoteAnnotationsView.setVideoRenderer(renderer);
            remoteAnnotationsView.attachToolbar(mAnnotationsToolbar);

            ((ViewGroup) mRemoteViewContainer).addView(remoteAnnotationsView);
            mPreviewFragment.enableAnnotations(true);
        } catch (Exception e) {
            Log.i(LOG_TAG, "Exception - enableRemoteAnnotations: " + e);
        }
```

#### Capturing and Saving a Screenshot

The annotation toolbar provides a camera icon that the user can click to capture a screenshot of the shared screen containing previously rendered annotations.

To take a screenshot of the screen sharing area, implement the `AnnotationsView.AnnotationsListener` interface and override the `onScreencaptureReady()` listener.

For example, the `MainActivity` class implements the interface and provides the following implementation of the listener, passing the bitmap to a private method that compresses and saves the image to an external storage location:


```java
public class MainActivity extends AppCompatActivity implements
    OneToOneCommunication.Listener,
    PreviewControlFragment.PreviewControlCallbacks,
    RemoteControlFragment.RemoteControlCallbacks,
    PreviewCameraFragment.PreviewCameraCallbacks,
    ScreenSharingFragment.ScreenSharingListener,
    AnnotationsView.AnnotationsListener {

    . . .

    @Override
    public void onScreencaptureReady(Bitmap bmp) {
        saveScreencapture(bmp);
    }


}
```


### User interface

As described in [Class design](#class-design), the `ScreenSharingFragment` class sets up and manages the UI views, events, and rendering for the screen sharing with annotations controls.

This class works with the following `MainActivity` methods, which manage the views as both clients participate in the session. These are callbacks that allow you to modify the app UI when there are changes in the Screensharing with Annotations library.

| Feature        | Methods  |
| ------------- | ------------- |
| Manage the UI containers. | `onCreate()`  |
| Reload the UI views whenever the device [configuration](http://developer.android.com/reference/android/content/res/Configuration.html), such as screen size or orientation, changes. | `onConfigurationChanged()`  |
| Opens and closes the screen sharing with annotations view. | `onScreenSharing()` |
| Manage the customizable views for the action bar, screen sharing, and annotation callbacks.   | `onScreenSharingStarted()`, `onScreenSharingStopped()`, `onAnnotationsViewReady()`, `onAnnotationsRemoteViewReady()`, `onScreenSharingError()` |


## Requirements

To develop a screen sharing with annotations app:

1. Install [Android Studio](http://developer.android.com/intl/es/sdk/index.html).
2. Review the [OpenTok Android SDK Requirements](https://tokbox.com/developer/sdks/android/#developerandclientrequirements).
3. The device must support Android Lollipop and later.

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

This section describes best practices the sample app code uses to deploy screen sharing with annotations.

The sample app design extends the [OpenTok One-to-One Communication Sample App](https://github.com/opentok/one-to-one-sample-apps/tree/master/one-to-one-sample-app/) and [OpenTok Accelerator Core for Android](https://github.com/opentok/accelerator-core-android/) by adding logic using the Screensharing feature in the Core library and the [OpenTok Accelerator Annotation](https://github.com/opentok/accelerator-annotation-android/) classes.




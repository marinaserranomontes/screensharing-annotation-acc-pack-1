![logo](../tokbox-logo.png)

# OpenTok Screensharing with Annotations Sample App for Android

This document describes how to use the Screensharing case included in the OpenTok Accelerator Core and the Accelerator Annotations for Android. Through the exploration of the One to One Screensharing with Annotations Sample Application, you will learn best practices for screensharing and adding annotations on an Android mobile device.  

You can configure and run this sample app within just a few minutes!

## Prerequisites

To be prepared to develop your text chat app:

1. Install [Android Studio](http://developer.android.com/intl/es/sdk/index.html).
2. Review the [OpenTok Android SDK Requirements](https://tokbox.com/developer/sdks/android/#developerandclientrequirements).
3. Your app will need a **Session ID**, **Token**, and **API Key**, which you can get at the [OpenTok Developer Dashboard](https://dashboard.tokbox.com/).

_**NOTE**: The OpenTok Developer Dashboard allows you to quickly run this sample program. For production deployment, you must generate the **Session ID** and **Token** values using one of the [OpenTok Server SDKs](https://tokbox.com/developer/sdks/server/)._


## Quick start

To get up and running quickly with your app, go through the following steps in the tutorial provided below:

1. [Importing the sample app](#importing-the-sample-app)
2. [Adding dependencies](#adding-dependencies)
3. [Configuring the app](#configuring-the-app)

To learn more about the best practices used to design this app, see [Exploring the code](#exploring-the-code).

### Importing the sample app

1. Clone [the OpenTok Screensharing with Annotations Sample App repository](https://github.com/opentok/screensharing-annotation-acc-pack/tree/master/android) from GitHub.
2. Start Android Studio.
3. In the **Quick Start** panel, click **Open an existing Android Studio Project**.
4. Navigate to the **android** folder, select the **OneToOneScreenSharingSample** folder, and click **Choose**.

### Adding dependencies

The Screensharing with Annotations sample app uses the [OpenTok Accelerator Annotation](https://github.com/opentok/accelerator-annotation-android) and [OpenTok Accelerator Core](https://github.com/opentok/accelerator-core-android).
The Accelerator Core is dependency of the Accelerator Annotations too, so following the transitive dependencies rules in maven, we don't need to add Accelerator Core in the sample app.

To add the Accelerator Annotation take a look [here](https://github.com/opentok/accelerator-annotation-android/blob/develop/README.md#add-the-annotations-library).

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

The sample app design extends the [OpenTok One-to-One Communication Sample App](https://github.com/opentok/one-to-one-sample-apps/tree/master/one-to-one-sample-app/) and [OpenTok Accelerator Core for Android](https://github.com/opentok/accelerator-core-android#screensharing) by adding logic using the Screensharing feature in the Core library and the [OpenTok Accelerator Annotation](https://github.com/opentok/accelerator-annotation-android/) classes.




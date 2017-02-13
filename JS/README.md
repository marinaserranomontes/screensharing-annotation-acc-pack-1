![logo](../tokbox-logo.png)
# OpenTok One-to-One Screensharing with Annotations Sample App for JavaScript

## Quick start

This section shows you how to prepare and run the sample application. The app is built by the [Accelerator Core JS](https://github.com/opentok/accelerator-core-js), the [Accelerator Screensharing JS](https://github.com/opentok/accelerator-screen-sharing-js) and [Accelerator Annotation JS](https://github.com/opentok/accelerator-annotation-js)

### Configuring the app

Configure the sample app code. Then, build and run the app.

1. Get values for **API Key**, **Session ID**, and **Token**.

2. In **app.js**, replace the following empty strings with the corresponding **API Key**, **Session ID**, and **Token** values:

   ```javascript
    apiKey: '',    // Replace with your OpenTok API Key
    sessionId: '', // Replace with a generated Session ID
    token: '',     // Replace with a generated token (from the dashboard or using an OpenTok server SDK)
    extensionID: '', // Replace with the generated ExtensionId for screensharing.
   ```

_**NOTE**: The OpenTok Developer Dashboard allows you to quickly run this sample program. For production deployment, you must generate the **Session ID** and **Token** values using one of the [OpenTok Server SDKs](https://tokbox.com/developer/sdks/server/). And to generate the extensionID for screenshing, please take a look [here](https://github.com/opentok/screensharing-extensions)_

### Deploying and running the app

```javascript
$ ./build.sh 
$ node server.js
```

The web page that loads the sample app for JavaScript must be served over HTTP/HTTPS. Browser security limitations prevent you from publishing video using a `file://` path, as discussed in the OpenTok.js [Release Notes](https://www.tokbox.com/developer/sdks/js/release-notes.html#knownIssues). To support clients running [Chrome 47 or later](https://groups.google.com/forum/#!topic/discuss-webrtc/sq5CVmY69sc), HTTPS is required. A [Node](https://nodejs.org/en/) server will work, as will [MAMP](https://www.mamp.info/) or [XAMPP](https://www.apachefriends.org/index.html).  You can also use a cloud service such as [Heroku](https://www.heroku.com/) to host the application.


## Exploring the code

For details about how to use the Accelerator Core in the sample app, see [here](https://github.com/opentok/accelerator-core-js#sample-applications).

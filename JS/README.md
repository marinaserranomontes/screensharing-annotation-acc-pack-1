![logo](../tokbox-logo.png)
# OpenTok Screensharing with Annotations Sample for JavaScript<br/>Version 1.1.0

This document describes how to develop a web-based application that uses the OpenTok Screensharing with Annotations Sample for JavaScript.

## Explore the Code

This section describes how the sample app code design uses recommended best practices to create a working implementation that uses the Screensharing with Annotations Sample.

To develop your application, follow this section to learn how to add the toolbar to your container and create an annotation canvas for both the publisher and subscriber.

The following steps will help you get started, and you can refer to the [complete code example](./sample-app/public/index.html):

1. [Web page design](#web-page-design)
2. [Specifying the options](#specifying-the-options)
3. [Initializing the session](#initializing-the-session)
4. [Initializing the components](#initializing-the-components)
5. [Best Practices for Resizing the Canvas](#best-practices-for-resizing-the-canvas)

To learn more about the annotation widget, visit [OpenTok Annotations Widget for JavaScript](https://github.com/opentok/annotation-widget/tree/js).


### Web Page Design

While TokBox hosts [OpenTok.js](https://tokbox.com/developer/sdks/js/), you must host the JavaScript Annotations widget yourself. You can specify toolbar items, colors, icons, and other options for the annotation widget via the common layer. For details about the one-to-one communication audio-video aspects of the design, see the [OpenTok One-to-One Communication Sample App](https://github.com/opentok/one-to-one-sample-apps/tree/master/one-to-one-sample-app/js) and [OpenTok Common Accelerator Session Pack](https://github.com/opentok/acc-pack-common/). The sample app has the following design:

* **[app.js](./sample-app/public/js/app.js)**: This is where you specify the **Session ID**, **Token**, and **API key**. This file contains functionality supporting the view layer and button click events, initializes the session, instantiates the one-to-one communication layer, and listens for stream and call events.

* **[accelerator-pack.js](./sample-app/public/js/components/accelerator-pack.js)**: The TokBox Common Accelerator Session Pack is a common layer that permits all accelerators to share the same OpenTok session, API Key, and other related information, and is required whenever you use any of the OpenTok accelerators. This layer handles communication between the client and any required components, such as screen sharing and text chat, which are instantiated in this layer. Each component can define its events, which are then registered in this common layer.

* **[opentok-annotation.js](./opentok.js-ss-annotation/src/acc-pack-annotation.js)**: This contains the constructor for the annotation component used over video or a shared screen. In the sample application build process, opentok-annotation.js (this file) and opentok-screen-sharing.js are combined into the screenshare-annotation-acc-pack.js.

* **[opentok-screensharing.js](./opentok.js-ss-annotation/src/opentok-screen-sharing.js)**: This contains the constructor for the screen sharing component. In the sample application build process, opentok-annotation.js and opentok-screensharing.js  (this file) are combined into the screenshare-annotation-acc-pack.js.
* **[screenshare-annotation-acc-pack.js](./sample-app/public/js/components/screenshare-annotation-acc-pack.js)**: _(Available only in the Screensharing with Annotations Sample)._ Defines the annotation component that can be used with screen sharing, the screensharing component, and the one-to-one communication layer used in the sample application. Both the publisher and subscriber can annotate the shared screen.

* **[CSS files](./sample-app/public/css)**: Defines the client UI style.  

* **[index.html](web/index.html)**: This web page provides you with a quick start if you don't already have a web page making calls against OpenTok.js (via accelerator-pack.js) and opentok-annotation.js. Its <head> element loads the OpenTok.js library, Annotation library, and other dependencies, and its <body> element implements the UI container for the controls on your page.


### Specifying the Options

In app.js, specify the **Session ID**, **Token**, and **API key** by editing the corresponding strings in `_options`:

```javascript
  var _options = {
    apiKey: '',
    sessionId: '',
    token: '',
    ...
  }
```


### Initializing the Session

The `AnnotationAccPack` constructor, located in `acc-pack-annotation.js`, sets the `accPack` property to register, trigger, and start events via the common layer API used for all samples:

```javascript
  // Trigger event via common layer API
  var _triggerEvent = function (event, data) {
    if (_accPack) {
      _accPack.triggerEvent(event, data);
    }
  };

  . . .

  var AnnotationAccPack = function (options) {
    _this = this;
    _this.options = _.omit(options, 'accPack');
    _accPack = _.property('accPack')(options);
    _session = _.property('session')(options);
    _registerEvents();
    _setupUI();
  };
```

The events that may be triggered include:

 - Resizing the canvas
 - Closing the external annotation window
 - Starting an annotation
 - Linking the canvas to the annotation
 - Ending an annotation


The `_init()` method in app.js initializes the session. It listens for the connection event and instantiates one-to-one communication:


```javascript
  var _init = function () {

    var accPackOptions = _.pick(_options, ['apiKey', 'sessionId', 'token', 'screensharing']);

    _accPack = new AcceleratorPack(accPackOptions);
    _session = _accPack.getSession();
    _.extend(_options, _accPack.getOptions());

    _session.on({
      connectionCreated: function () {

        if (_connected) {
          return;
        }

        _connected = true;

        var commOptions = _.extend({}, _options, {
          session: _session,
          accPack: _accPack
        }, _accPack.getOptions());

        _communication = new CommunicationAccPack(commOptions);
        _addEventListeners();
        _initialized = true;
        _startCall();
      }
    });
  };
```

For more information, see [Initialize, Connect, and Publish to a Session](https://tokbox.com/developer/concepts/connect-and-publish/).

### Initializing the Components

If you install the screen sharing with annotations component with [npm](https://www.npmjs.com/package/opentok-screen-sharing), you can instantiate the `ScreenSharingAccPack` instance with this approach:

  ```javascript
  const screenSharing = require('opentok-screen-sharing');
  const screensharingAccPack = new screenSharing(options);
  ```


The `_initAccPackComponents()` method in accelerator-pack.js initializes the components required by the application. In this example the screen sharing and annotation components are initialized:


```javascript
  var _initAccPackComponents = _.once(function () {

    if (!!ScreenSharingAccPack) {

      var screensharingProps = [
        'sessionId',
        'annotation',
        'extensionURL',
        'extensionID',
        'extensionPathFF',
        'screensharingContainer'
      ];

      var screensharingOptions = _.extend(_.pick(_this.options, screensharingProps),
        _this.options.screensharing, {
          session: _session,
          accPack: _this,
          localScreenProperties: _commonOptions.localScreenProperties
        });

      _screensharing = new ScreenSharingAccPack(screensharingOptions);
    }

    if (!!AnnotationAccPack) {

      _annotation = new AnnotationAccPack(_.extend({}, _this.options, {
        accPack: _this
      }));
    }

    _setupEventListeners();

  });
```

### Best Practices for Resizing the Canvas

The `linkCanvas()` method, located in `acc-pack-annotation.js`, refers to a parent DOM element called the `absoluteParent`. This is needed in order to maintain accurate information needed to properly resize both the canvas and its container element as the window is resized. This is a recommended best practice that mitigates potential issues with jQuery in which information may be lost upon multiple resize attempts.

## Prerequisites for Development

To prepare for developing a web-based application that uses the OpenTok Screensharing with Annotations Accelerator for JavaScript:

1. Review the basic requirements for [OpenTok](https://tokbox.com/developer/requirements/) and [OpenTok.js](https://tokbox.com/developer/sdks/js/#browsers).
2. Your project must include [jQuery](https://jquery.com/) and [Underscore](http://underscorejs.org/).<br/>_**NOTE**: This step is not necessary if you are using [browserify](http://browserify.org/) or [webpack](https://webpack.github.io/) in your project._
3. There are several ways to install the OpenTok Screensharing with Annotations Sample: <ul><li>(Preferred) Run the [build.sh script](./build.sh).</li><li>Download and extract the **screenshare-annotation-acc-pack.js** file from the [zip](https://s3.amazonaws.com/artifact.tokbox.com/solution/rel/screensharing-annotations-acc-pack/JS/opentok-js-screenshare-annotation-1.1.0.zip) file provided by TokBox.</li><li>Install the following packages with  [npm](https://www.npmjs.com/package/opentok-screen-sharing): opentok-annotations, opentok-screen-sharing,  opentok-solutions-css, and opentok-solutions-logging. Then move the files to the `public/js` folder.</li></ul>
4. Your web page must load [OpenTok.js](https://tokbox.com/developer/sdks/js/) first, and then load [opentok-annotations.js](./sample-app/public/js/components/opentok-annotation.js) and [screenshare-annotation-acc-pack.js](./sample-app/public/js/components/screenshare-annotation-acc-pack.js).
5. Your app will need a **Session ID**, **Token**, and **API key**. See the [Screensharing Annotation Sample home page](../README.md) for important information.


## Deploy

The web page that loads the OpenTok Screensharing with Annotations example for JavaScript must be served over HTTP/HTTPS. Browser security limitations prevent you from publishing video using a `file://` path, as discussed in the OpenTok.js [Release Notes](https://www.tokbox.com/developer/sdks/js/release-notes.html#knownIssues).

To support clients running [Chrome 47 or later](https://groups.google.com/forum/#!topic/discuss-webrtc/sq5CVmY69sc), HTTPS is required, though localhost Chrome clients are considered secure and HTTP is permitted in such cases.

A web server such as [MAMP](https://www.mamp.info/) or [Apache](https://httpd.apache.org/) will work, or you can use a cloud service such as [Heroku](https://www.heroku.com/) to host the widget.

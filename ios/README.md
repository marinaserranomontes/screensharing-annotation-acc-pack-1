![logo](../tokbox-logo.png)

# OpenTok Screensharing with Annotations Sample for iOS<br/>Version 1.1.0

## Quick start

This section shows you how to prepare, build, and run the sample application.

### Install the project files

Use CocoaPods to install the project files and dependencies.

1. Install CocoaPods as described in [CocoaPods Getting Started](https://guides.cocoapods.org/using/getting-started.html#getting-started).
1. Add the following line to your pod file: ` pod ‘OTScreenShareKit’  `
1. In Terminal, `cd` to your project directory and type `pod install`.
1. Reopen your project using the new `*.xcworkspace` file.

### Configure and build the app

Configure the sample app code. Then, build and run the app.

1. Get values for **API Key**, **Session ID**, and **Token**. See the [Screensharing Annotation Sample home page](../README.md) for important information.

1. In Xcode, open **AppDelegate.h** and replace the following empty strings with the corresponding **API Key**, **Session ID**, and **Token** values:

	```objc
	- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions {

		  // Override point for customization after application launch.    
	    [OTScreenSharer setOpenTokApiKey:@""
	                           sessionId:@""
	                               token:@""];
	  	return YES;
	}
	```

1. Use Xcode to build and run the app on an iOS simulator or device.


## Exploring the code

This section describes best practices the sample app code uses to deploy screen sharing with annotations.

The sample app design extends the [OpenTok One-to-One Communication Sample App](https://github.com/opentok/one-to-one-sample-apps/tree/master/one-to-one-sample-app/) and [OpenTok Common Accelerator Session Pack](https://github.com/opentok/acc-pack-common/) by adding logic using the classes in the `OTScreenShareKit` framework.

For details about developing with the SDK and the APIs this sample uses, see [Requirements](#requirements), the [OpenTok iOS SDK Requirements](https://tokbox.com/developer/sdks/ios/) and the [OpenTok iOS SDK Reference](https://tokbox.com/developer/sdks/ios/reference/).

_**NOTE:** This sample app collects anonymous usage data for internal TokBox purposes only. Please do not modify or remove any logging code from this sample application._

### App design

This section focuses on screen sharing with annotations features. For information about one-to-one communication features, see the [OpenTok One-to-One Communication Sample App](https://github.com/opentok/one-to-one-sample-apps).

| Class        | Description  |
| ------------- | ------------- |
| `MainViewController`   | In conjunction with **Main.storyboard**, this class uses the OpenTok API to initiate the client connection to the OpenTok session. It also implements the sample UI and screen sharing with annotations callbacks.   |


From the [ScreenShareAccPackKit](https://github.com/opentok/screen-sharing-acc-pack).

| Class        | Description  |
| ------------- | ------------- |
| `OTScreenSharer`   | This component enables the publisher to share either the entire screen or a specified portion of the screen. |


From the [AnnotationAccPackKit](https://github.com/opentok/annotation-acc-pack).

| Class        | Description  |
| ------------- | ------------- |
| `OTAnnotationScrollView` | Provides the initializers and methods for the client annotating views. See the [OpenTok Annotations Sample](https://github.com/opentok/annotation-acc-pack) for more information. |
| `OTAnnotationToolbarView`   | A convenient annotation toolbar that is optionally available for your development. As an alternative, you can create a toolbar using `OTAnnotationScrollView`. See the [OpenTok Annotations Sample](https://github.com/opentok/annotation-acc-pack) for more information. |
| `OTFullScreenAnnotationViewController`   | Combines both the scroll and annotation toolbar views. See the [OpenTok Annotations Sample](https://github.com/opentok/annotation-acc-pack) for more information. |


### Screen sharing and annotation features

The `OTScreenSharer` and `OTAnnotationScrollView` classes are the backbone of the screen sharing and annotation features for the app.

```objc
@interface OTScreenSharer : NSObject

@property (readonly, nonatomic) BOOL isScreenSharing;

+ (instancetype)screenSharer;
+ (void) setOpenTokApiKey:(NSString *)apiKey
               sessionId:(NSString *)sessionId
                   token:(NSString *)token;

- (void)connectWithView:(UIView *)view;
- (void)connectWithView:(UIView *)view
                handler:(ScreenShareBlock)handler;
- (void)disconnect;
- (void)updateView:(UIView *)view;
@property (weak, nonatomic) id<ScreenShareDelegate> delegate;

// SUBSCRIBER
@property (readonly, nonatomic) UIView *subscriberView;
@property (nonatomic, getter=isSubscribeToAudio) BOOL subscribeToAudio;
@property (nonatomic, getter=isSubscribeToVideo) BOOL subscribeToVideo;

// PUBLISHER
@property (readonly, nonatomic) UIView *publisherView;
@property (nonatomic, getter=isPublishAudio) BOOL publishAudio;
@property (nonatomic, getter=isPublishVideo) BOOL publishVideo;
```

```objc
@interface OTAnnotationScrollView : UIView

property (nonatomic, getter = isAnnotatable) BOOL annotatable;
@property (nonatomic, getter = isZoomEnabled) BOOL zoomEnabled;
@property (readonly, nonatomic) OTAnnotationView *annotationView;

- (instancetype)initWithFrame:(CGRect)frame;
- (void)addContentView:(UIView *)view;  // Enables scrolling if image is larger than device screen

#pragma mark - Tool bar
@property (nullable, readonly, nonatomic) OTAnnotationToolbarView *toolbarView;
- (void)initializeToolbarView;

#pragma mark - Annotation
- (void)addTextAnnotation:(OTAnnotationTextView *)annotationTextView;

@end
```



#### Initialization methods

The following `OTScreenSharer` and `OTAnnotationScrollView` methods initialize the screen sharing with annotations features so the publisher and subscriber can both annotate the shared screen.

| Feature        | Methods  |
| ------------- | ------------- |
| Initialize the annotation view  | `initWithFrame:` |
| Initialize the connection for screen sharing | `connectWithView:handler:` |


```objc
- (void)viewDidLoad {
    [super viewDidLoad];

    UIImage *image = [UIImage imageNamed:@"mvc"];
    UIImageView *imageView = [[UIImageView alloc] initWithImage:image];
    imageView.frame = CGRectMake(0, 0, image.size.width, image.size.height);

    self.screenShareView = [[OTAnnotationScrollView alloc] initWithFrame:CGRectMake(0, 0, CGRectGetWidth([UIScreen mainScreen].bounds), CGRectGetHeight([UIScreen mainScreen].bounds))];
    [self.screenShareView addContentView:imageView];
    [self.view addSubview:self.screenShareView];
}

- (void)shareTheWholeScreen {
	[self.screenSharer connectWithView:[UIApplication sharedApplication].keyWindow.rootViewController.view handler:^(ScreenShareSignal signal, NSError *error) {

        if (!error) {
            // begin sharing screen
        }
        else {
            // error with screen sharing
        }
    }];
}
```

## Requirements

To develop a screen sharing with annotations app:

1. Install Xcode version 5 or later.
1. Review the [OpenTok iOS SDK Requirements](https://tokbox.com/developer/sdks/ios/).

_You do not need the OpenTok iOS SDK to use this sample._

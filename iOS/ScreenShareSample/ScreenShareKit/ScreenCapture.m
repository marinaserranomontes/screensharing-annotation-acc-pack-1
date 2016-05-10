//
//  ScreenCapture.m
//  ScreenCapture
//
//  Created by Esteban Cordero on 3/7/16.
//  Copyright © 2016 Esteban Cordero. All rights reserved.
//

#include <mach/mach.h>
#include <mach/mach_time.h>
#import "ScreenCapture.h"
#import <OTAcceleratorPackUtil/OTAcceleratorPackUtil.h>

@interface ScreenCapture() <OTVideoCapture>

@property (nonatomic) OTPublisher *publisher;
@property (nonatomic) OTAcceleratorSession *session;
@property (nonatomic) ScreenCapture *videoCapture;

@end

@implementation ScreenCapture {
    CMTime _minFrameDuration;
    dispatch_queue_t _queue;
    dispatch_source_t _timer;

    CVPixelBufferRef _pixelBuffer;
    BOOL _capturing;
    OTVideoFrame* _videoFrame;
    UIView* _view;

}


@synthesize videoCaptureConsumer;

#pragma mark - Class Lifecycle.

+ (void)setOpenTokApiKey:(NSString *)apiKey
               sessionId:(NSString *)sessionId
                   token:(NSString *)token {
    
    [OTAcceleratorSession setOpenTokApiKey:apiKey sessionId:sessionId token:token];
    [ScreenCapture sharedInstance];
}

+ (instancetype)sharedInstance {
    
    static ScreenCapture *sharedInstance;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        sharedInstance = [[ScreenCapture alloc] init];
        sharedInstance.session = [OTAcceleratorSession getAcceleratorPackSession];
    });
    return sharedInstance;
}

- (instancetype)initWithView:(UIView *)view {
    self = [super init];
    if (self) {
        _view = view;
        // Recommend sending 5 frames per second: Allows for higher image
        // quality per frame
        _minFrameDuration = CMTimeMake(1, 5);
        _queue = dispatch_queue_create("SCREEN_CAPTURE", NULL);

        OTVideoFormat *format = [[OTVideoFormat alloc] init];
        [format setPixelFormat:OTPixelFormatARGB];

        _videoFrame = [[OTVideoFrame alloc] initWithFormat:format];

    }
    return self;
}

- (id)connectPublisher {
    
    [OTAcceleratorSession registerWithAccePack:self];
    
    // need to explcitly publish and subscribe if the communicator joins/rejoins a connected session
    if (self.session.sessionConnectionStatus == OTSessionConnectionStatusConnected &&
        self.session.streams[self.publisher.stream.streamId]) {
        
        [_publisher setVideoType:OTPublisherKitVideoTypeScreen];
        
        _videoCapture = [[ScreenCapture alloc] initWithView: _view];
        [_publisher setVideoCapture:_videoCapture];
        
        OTError *error = nil;
        [self.session publish:self.publisher error:&error];
        if (error) {
            NSLog(@"%@", error.localizedDescription);
        }
    }
    return self.publisher.view;
}

- (void)dealloc { //ARC should handle everythihg else
    [self releaseCapture];
    CVPixelBufferRelease(_pixelBuffer);
}


#pragma mark - Capture lifecycle

/**
 * Allocate capture resources; in this case we're just setting up a timer and
 * block to execute periodically to send video frames.
 */
- (void)startCapture {
    __unsafe_unretained ScreenCapture* _self = self;
    _timer = dispatch_source_create(DISPATCH_SOURCE_TYPE_TIMER, 0, 0, _queue);

    dispatch_source_set_timer(_timer, dispatch_walltime(NULL, 0),
                              100ull * NSEC_PER_MSEC, 100ull * NSEC_PER_MSEC);

    dispatch_source_set_event_handler(_timer, ^{
        @autoreleasepool {
            __block UIImage* screen = [_self screenshot];
            [_self consumeFrame:[screen CGImage]];
        }
    });
}

- (void)stopCapture{
    _timer = nil;
}

- (void)initCapture {
    _capturing = YES;

    if (_timer) {
        dispatch_resume(_timer);
    }

}

- (void)releaseCapture  {
    _capturing = NO;

    dispatch_sync(_queue, ^{
        if (_timer) {
            dispatch_source_cancel(_timer);
        }
    });
}

- (BOOL)isCaptureStarted {
    return _capturing;
}

#pragma mark - Screen capture implementation

- (CVPixelBufferRef)pixelBufferFromCGImage:(CGImageRef)image {
    CGFloat width = CGImageGetWidth(image);
    CGFloat height = CGImageGetHeight(image);
    CGSize frameSize = CGSizeMake(width, height);
    
    CVPixelBufferLockBaseAddress(_pixelBuffer, 0);
    void *pxdata = CVPixelBufferGetBaseAddress(_pixelBuffer);

    CGColorSpaceRef rgbColorSpace = CGColorSpaceCreateDeviceRGB();
    CGContextRef context =
    CGBitmapContextCreate(pxdata,
                          frameSize.width,
                          frameSize.height,
                          8,
                          CVPixelBufferGetBytesPerRow(_pixelBuffer),
                          rgbColorSpace,
                          kCGImageAlphaPremultipliedFirst |
                          kCGBitmapByteOrder32Little);


    CGContextDrawImage(context, CGRectMake(0, 0, width, height), image);
    CGColorSpaceRelease(rgbColorSpace);
    CGContextRelease(context);

    CVPixelBufferUnlockBaseAddress(_pixelBuffer, 0);

    return _pixelBuffer;
}

- (int32_t)captureSettings:(OTVideoFormat*)videoFormat {
    videoFormat.pixelFormat = OTPixelFormatARGB;
    return 0;
}

- (UIImage *)screenshot {
    CGSize imageSize = CGSizeZero;

    imageSize = [UIScreen mainScreen].bounds.size;

    UIGraphicsBeginImageContextWithOptions(imageSize, NO, 0);

    if ([_view respondsToSelector: @selector(drawViewHierarchyInRect:afterScreenUpdates:)]) {
        [_view drawViewHierarchyInRect:_view.bounds afterScreenUpdates:NO];
    } else {
        [_view.layer renderInContext:UIGraphicsGetCurrentContext()];
    }

    UIImage *image = UIGraphicsGetImageFromCurrentImageContext();
    UIGraphicsEndImageContext();
    return image;
}

- (void) consumeFrame:(CGImageRef)frame {

    [self checkImageSize:frame];

    static mach_timebase_info_data_t time_info;
    uint64_t time_stamp = 0;

    if (!(_capturing && self.videoCaptureConsumer)) return;

    if (time_info.denom == 0) (void) mach_timebase_info(&time_info);

    time_stamp = mach_absolute_time();
    time_stamp *= time_info.numer;
    time_stamp /= time_info.denom;

    CMTime time = CMTimeMake(time_stamp, 1000);
    CVImageBufferRef ref = [self pixelBufferFromCGImage:frame];

    CVPixelBufferLockBaseAddress(ref, 0);

    _videoFrame.timestamp = time;
    _videoFrame.format.estimatedFramesPerSecond = _minFrameDuration.timescale / _minFrameDuration.value;
    _videoFrame.format.estimatedCaptureDelay = 100;
    _videoFrame.orientation = OTVideoOrientationUp;
    
    [_videoFrame clearPlanes];
    [_videoFrame.planes addPointer:CVPixelBufferGetBaseAddress(ref)];
    [self.videoCaptureConsumer consumeFrame:_videoFrame];
    
    CVPixelBufferUnlockBaseAddress(ref, 0);
}

#pragma mark - Private Methods

/**
 * Make sure receiving video frame container is setup for this image.
 */
- (void)checkImageSize:(CGImageRef)image {
    CGFloat width = CGImageGetWidth(image);
    CGFloat height = CGImageGetHeight(image);
    
    if (_videoFrame.format.imageHeight == height && _videoFrame.format.imageWidth == width) return;
    
    [_videoFrame.format.bytesPerRow removeAllObjects];
    [_videoFrame.format.bytesPerRow addObject:@(width * 4)];
    [_videoFrame.format setImageHeight:height];
    [_videoFrame.format setImageWidth:width];
    
    CGSize frameSize = CGSizeMake(width, height);
    NSDictionary *options = [NSDictionary dictionaryWithObjectsAndKeys:
                             @NO,
                             kCVPixelBufferCGImageCompatibilityKey,
                             @NO,
                             kCVPixelBufferCGBitmapContextCompatibilityKey,
                             nil];
    
    if (NULL != _pixelBuffer) CVPixelBufferRelease(_pixelBuffer);
    
    CVReturn status = CVPixelBufferCreate(kCFAllocatorDefault,
                                          frameSize.width,
                                          frameSize.height,
                                          kCVPixelFormatType_32ARGB,
                                          (__bridge CFDictionaryRef)(options),
                                          &_pixelBuffer);
    
    NSParameterAssert(status == kCVReturnSuccess && _pixelBuffer != NULL);
}

@end
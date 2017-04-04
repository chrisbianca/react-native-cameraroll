#import "RNCameraRoll.h"

#import <Photos/Photos.h>

#import <CoreLocation/CoreLocation.h>
#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>

#import <React/RCTBridge.h>
#import <React/RCTConvert.h>
#import <React/RCTLog.h>
#import <React/RCTUtils.h>

#import "RCTAssetsLibraryRequestHandler.h"

@implementation RNCameraRoll

RCT_EXPORT_MODULE()

NSString *const RCTErrorInvalidAssetType = @"E_INVALID_ASSET_TYPE";
NSString *const RCTErrorPermissionDenied = @"ERROR_PERMISSION_DENIED";


RCT_EXPORT_METHOD(getAssets:(NSDictionary *)params
                  resolve:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject)
{
  checkPhotoLibraryConfig();

  PHAuthorizationStatus status = [PHPhotoLibrary authorizationStatus];
  if (status == PHAuthorizationStatusDenied || status == PHAuthorizationStatusRestricted) {
    reject(RCTErrorPermissionDenied, nil, nil);
    return;
  } else if (status == PHAuthorizationStatusNotDetermined) {
    //prompt for access
    [PHPhotoLibrary requestAuthorization:^(PHAuthorizationStatus status) {
      if (status == PHAuthorizationStatusAuthorized) {
        [self loadAssets:params resolve:resolve reject:reject];
      } else {
        reject(RCTErrorPermissionDenied, nil, nil);
        return;
      }
    }];
  } else {
    [self loadAssets:params resolve:resolve reject:reject];
  }
}

- (void) loadAssets:(NSDictionary *) params
            resolve:(RCTPromiseResolveBlock) resolve
             reject:(RCTPromiseRejectBlock)reject
{
  NSUInteger startParam = [RCTConvert NSInteger:params[@"start"]];
  NSUInteger limitParam = [RCTConvert NSInteger:params[@"limit"]];
  NSString *assetTypeParam = [RCTConvert NSString:params[@"assetType"]];

  PHAssetMediaType assetType;
  if ([assetTypeParam isEqualToString:@"image"]) {
    assetType = PHAssetMediaTypeImage;
  } else if ([assetTypeParam isEqualToString:@"video"]) {
    assetType = PHAssetMediaTypeVideo;
  } else {
    RCTLogError(@"Invalid assetType %@", assetTypeParam);
    reject(RCTErrorInvalidAssetType, nil, nil);
    return;
  }

  PHFetchOptions *allPhotosOptions = [PHFetchOptions new];
  allPhotosOptions.sortDescriptors = @[[NSSortDescriptor sortDescriptorWithKey:@"creationDate" ascending:NO]];

  PHFetchResult *allPhotosResult = [PHAsset fetchAssetsWithMediaType:assetType options:allPhotosOptions];

  NSInteger startIndex = startParam ? startParam : 0;
  NSInteger length = !limitParam || startIndex + limitParam > allPhotosResult.count ? allPhotosResult.count - startIndex : limitParam;
  NSMutableArray<NSDictionary<NSString *, id> *> *assets = [NSMutableArray new];

  [allPhotosResult enumerateObjectsAtIndexes:[NSIndexSet indexSetWithIndexesInRange:NSMakeRange(startIndex, length)]
                                     options:0
                                  usingBlock:^(PHAsset *asset, NSUInteger idx, BOOL *stop) {
                                    PHAssetMediaType assetType = [asset mediaType];
                                    [assets addObject:@{
                                                        @"type": assetType == PHAssetMediaTypeImage ? @"image" : @"video",
                                                        @"uri": [NSString stringWithFormat:@"ph://%@", asset.localIdentifier],
                                                        @"filename": [asset valueForKey:@"filename"],
                                                        @"height": @(asset.pixelHeight),
                                                        @"width": @(asset.pixelWidth),
                                                        @"timestamp": @(asset.creationDate.timeIntervalSince1970),
                                                        @"location": asset.location ? @{
                                                          @"latitude": @(asset.location.coordinate.latitude),
                                                          @"longitude": @(asset.location.coordinate.longitude),
                                                          @"altitude": @(asset.location.altitude),
                                                          @"heading": @(asset.location.course),
                                                          @"speed": @(asset.location.speed),
                                                        } : [NSNull null],
                                                      }];
                                  }];
  resolve(@{
    @"assets": assets,
    @"page_info": @{
      @"end_cursor": @(startIndex + length),
      @"has_next_page": @(startIndex + length < allPhotosResult.count),
      @"start_cursor": @(startIndex),
    }
  });
}

static void checkPhotoLibraryConfig()
{
#if RCT_DEV
  if (![[NSBundle mainBundle] objectForInfoDictionaryKey:@"NSPhotoLibraryUsageDescription"]) {
    RCTLogError(@"NSPhotoLibraryUsageDescription key must be present in Info.plist to use camera roll.");
  }
#endif
}

@end

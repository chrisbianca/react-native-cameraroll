package com.chrisbianca.cameraroll;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.text.TextUtils;

import com.facebook.react.bridge.GuardedAsyncTask;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.bridge.WritableNativeMap;
import com.facebook.react.module.annotations.ReactModule;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

@ReactModule(name = "RNCameraRoll")
public class RNCameraRollModule extends ReactContextBaseJavaModule {

    private static final String ERROR_INVALID_ASSET_TYPE = "E_INVALID_ASSET_TYPE";
    private static final String ERROR_PERMISSION_DENIED = "E_PERMISSION_DENIED";
    private static final String ERROR_UNABLE_TO_LOAD = "E_UNABLE_TO_LOAD";

    private static final String[] FIELDS = new String[] {
        Images.Media._ID,
        Images.Media.MIME_TYPE,
        Images.Media.BUCKET_DISPLAY_NAME,
        Images.Media.DATE_TAKEN,
        Images.Media.WIDTH,
        Images.Media.HEIGHT,
        Images.Media.LONGITUDE,
        Images.Media.LATITUDE,
        Images.Media.DISPLAY_NAME
    };

    private static final String QUERY_DATE_TAKEN = Images.Media.DATE_TAKEN + " < ?";

    public RNCameraRollModule(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @Override
    public String getName() {
        return "RNCameraRoll";
    }

    @ReactMethod
    public void getAssets(final ReadableMap params, final Promise promise) {
        String start = params.hasKey("start") ? params.getString("start") : null;
        int limit = params.getInt("limit");
        String assetType = params.getString("assetType");

        new GetAssetsTask(
                getReactApplicationContext(),
                start,
                limit,
                assetType,
                promise)
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private static class GetAssetsTask extends GuardedAsyncTask<Void, Void> {
        private final Context mContext;
        private final @Nullable String mStart;
        private final int mLimit;
        private final @Nullable String mAssetType;
        private final Promise mPromise;

        private GetAssetsTask(
                ReactContext context,
                @Nullable String start,
                int limit,
                @Nullable String assetType,
                Promise promise) {
            super(context);
            mContext = context;
            mStart = start;
            mLimit = limit;
            mAssetType = assetType;
            mPromise = promise;
        }

        @Override
        protected void doInBackgroundGuarded(Void... params) {
            StringBuilder query = new StringBuilder("1");
            List<String> queryArgs = new ArrayList<>();
            if (!TextUtils.isEmpty(mStart)) {
                query.append(" AND " + QUERY_DATE_TAKEN);
                queryArgs.add(mStart);
            }
            Uri uri;
            if (mAssetType == null || "all".equals(mAssetType)) {
                uri = MediaStore.Files.getContentUri("external");
                query.append(" AND (" + Images.Media.MIME_TYPE + " LIKE ? OR "
                        + Images.Media.MIME_TYPE + " LIKE ?) ");
                queryArgs.add("image/%");
                queryArgs.add("video/%");
            } else if ("image".equals(mAssetType)) {
                uri = Images.Media.EXTERNAL_CONTENT_URI;
                query.append(" AND " + Images.Media.MIME_TYPE + " LIKE ? ");
                queryArgs.add("image/%");
            } else if ("video".equals(mAssetType)) {
                uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                query.append(" AND " + Images.Media.MIME_TYPE + " LIKE ? ");
                queryArgs.add("video/%");
            } else {
                mPromise.reject(ERROR_INVALID_ASSET_TYPE, "Invalid assetType: " + mAssetType);
                return;
            }
            WritableMap response = new WritableNativeMap();
            ContentResolver resolver = mContext.getContentResolver();
            try {
                Cursor assetsCursor = resolver.query(
                        uri,
                        FIELDS,
                        query.toString(),
                        queryArgs.toArray(new String[queryArgs.size()]),
                        Images.Media.DATE_TAKEN + " DESC, " + Images.Media.DATE_MODIFIED +
                            " DESC LIMIT " + (mLimit + 1));
                if (assetsCursor == null) {
                    mPromise.reject(ERROR_UNABLE_TO_LOAD, "Could not get assets");
                } else {
                    try {
                        response.putArray("assets", buildAssets(assetsCursor, response, mLimit));
                        response.putMap("page_info", buildPageInfo(assetsCursor, response, mLimit));
                    } finally {
                        assetsCursor.close();
                        mPromise.resolve(response);
                    }
                }
            } catch (SecurityException e) {
                mPromise.reject(
                        ERROR_PERMISSION_DENIED,
                        "Could not get assets: need READ_EXTERNAL_STORAGE permission",
                        e);
            }
        }
    }

    private static WritableMap buildPageInfo(Cursor assetsCursor, WritableMap response, int limit) {
        WritableMap pageInfo = new WritableNativeMap();
        pageInfo.putBoolean("has_next_page", limit < assetsCursor.getCount());
        if (limit < assetsCursor.getCount()) {
            assetsCursor.moveToPosition(limit - 1);
            pageInfo.putString("end_cursor",
                    assetsCursor.getString(assetsCursor.getColumnIndex(Images.Media.DATE_TAKEN)));
        }
        return pageInfo;
    }

    private static WritableArray buildAssets(Cursor assetsCursor, WritableMap response,
        int limit) {
        assetsCursor.moveToFirst();
        int idIndex = assetsCursor.getColumnIndex(Images.Media._ID);
        int mimeTypeIndex = assetsCursor.getColumnIndex(Images.Media.MIME_TYPE);
        int dateTakenIndex = assetsCursor.getColumnIndex(Images.Media.DATE_TAKEN);
        int widthIndex = assetsCursor.getColumnIndex(Images.Media.WIDTH);
        int heightIndex = assetsCursor.getColumnIndex(Images.Media.HEIGHT);
        int longitudeIndex = assetsCursor.getColumnIndex(Images.Media.LONGITUDE);
        int latitudeIndex = assetsCursor.getColumnIndex(Images.Media.LATITUDE);
        int filenameIndex = assetsCursor.getColumnIndex(Images.Media.DISPLAY_NAME);

        WritableArray assets = new WritableNativeArray();
        for (int i = 0; i < limit && !assetsCursor.isAfterLast(); i++) {
            WritableMap asset = new WritableNativeMap();
            asset.putDouble("width", assetsCursor.getInt(widthIndex));
            asset.putDouble("height", assetsCursor.getInt(heightIndex));
            asset.putString("filename", assetsCursor.getString(filenameIndex));
            asset.putDouble("timestamp", assetsCursor.getLong(dateTakenIndex) / 1000d);

            String mimeType = assetsCursor.getString(mimeTypeIndex);
            if (mimeType.startsWith("image/")) {
                Uri photoUri = Uri.withAppendedPath(Images.Media.EXTERNAL_CONTENT_URI,
                        assetsCursor.getString(idIndex));
                asset.putString("uri", photoUri.toString());
                asset.putString("type", "image");
            } else {
                Uri videoUri = Uri.withAppendedPath(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        assetsCursor.getString(idIndex));
                asset.putString("uri", videoUri.toString());
                asset.putString("type", "video");
            }

            double longitude = assetsCursor.getDouble(longitudeIndex);
            double latitude = assetsCursor.getDouble(latitudeIndex);
            if (longitude > 0 || latitude > 0) {
                WritableMap location = new WritableNativeMap();
                location.putDouble("longitude", longitude);
                location.putDouble("latitude", latitude);
                asset.putMap("location", location);
            }

            assets.pushMap(asset);
            assetsCursor.moveToNext();
        }
        return assets;
    }
}

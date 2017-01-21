package com.example.android.sunshine.sync;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.io.ByteArrayOutputStream;

/**
 * Created by ramkrishna on 15/1/17.
 */

public class SunshineWearSyncUtils implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    private static int i = 0;
    private static final String TAG = SunshineWearSyncUtils.class.getName();
    private GoogleApiClient mGoogleApiClient;
    private Context mContext;
    private static final String WEARABLE_DATA_PATH = "/wearable_data";
    String tempHigh;
    String tempLow;
    Bitmap bitmap;

    public SunshineWearSyncUtils(Context context, String tempHigh, String tempLow, Bitmap bitmap) {
        this.mContext = context;
        this.tempHigh = tempHigh;
        this.tempLow = tempLow;
        this.bitmap = bitmap;
        mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "Connected to google services");
        sendDataToWearable();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "Disconnected to google services");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "Failed to connect to google services");
    }

    private static Asset createAssetFromBitmap(Bitmap bitmap) {
        final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteStream);
        return Asset.createFromBytes(byteStream.toByteArray());
    }

    private void sendDataToWearable() {
        PutDataMapRequest dataMapRequest = PutDataMapRequest.create(WEARABLE_DATA_PATH);
        dataMapRequest.getDataMap().putString("temphigh", tempHigh);
        dataMapRequest.getDataMap().putString("templow", tempLow);
        dataMapRequest.getDataMap().putLong("timestamp", System.currentTimeMillis());
        Asset asset = createAssetFromBitmap(bitmap);
        dataMapRequest.getDataMap().putAsset("bmpicon", asset);
        PutDataRequest putDataRequest = dataMapRequest.asPutDataRequest();
        putDataRequest.setUrgent();
        Wearable.DataApi.putDataItem(mGoogleApiClient, putDataRequest).setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
            @Override
            public void onResult(@NonNull DataApi.DataItemResult dataItemResult) {
                if (dataItemResult.getStatus().isSuccess()) {
                    Log.d(TAG, "onresult success data was sent!!!");
                } else {
                    Log.d(TAG, "onresult failed");
                }
            }
        });
    }
}

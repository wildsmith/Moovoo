package com.ooVoo.oovoosample.Main;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.data.FreezableUtils;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.ooVoo.oovoosample.R;


import java.util.List;
import java.util.concurrent.TimeUnit;

public class OngoingNotificationListenerService extends WearableListenerService {
    private static final String TAG = OngoingNotificationListenerService.class.getSimpleName();
    private static final int NOTIFICATION_ID = 100;

    private GoogleApiClient mGoogleApiClient;

    @Override
    public void onCreate() {
        super.onCreate();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
            .addApi(Wearable.API)
            .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        final List<DataEvent> events = FreezableUtils.freezeIterable(dataEvents);
        dataEvents.close();

        if (!mGoogleApiClient.isConnected()) {
            ConnectionResult connectionResult = mGoogleApiClient
                .blockingConnect(30, TimeUnit.SECONDS);
            if (!connectionResult.isSuccess()) {
                Log.e(TAG, "Service failed to connect to GoogleApiClient.");
                return;
            }
        }

        for (DataEvent event : events) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                String path = event.getDataItem().getUri().getPath();
                if (Constants.PATH_NOTIFICATION.equals(path)) {

                    // Get the data out of the event
                    DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
                    final boolean isChatting = dataMapItem.getDataMap().getBoolean(Constants.KEY_CHATTING);

                    // Build the intent to display our custom notification - is currently video chatting
                    Intent notificationIntent = new Intent(this, NotificationActivity.class);
                    notificationIntent.putExtra(NotificationActivity.EXTRA_CHATTING, isChatting);
                    PendingIntent notificationPendingIntent = PendingIntent.getActivity(
                        this,
                        0,
                        notificationIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);

                    //building a notification card
                    Notification.Builder notificationBuilder =
                        new Notification.Builder(this)
                            .setSmallIcon(R.drawable.ic_launcher)
                            .extend(new Notification.WearableExtender()
                                        .setDisplayIntent(notificationPendingIntent)
                            );

                    ((NotificationManager) getSystemService(NOTIFICATION_SERVICE))
                        .notify(NOTIFICATION_ID, notificationBuilder.build());
                } else {
                    Log.d(TAG, "Unrecognized path: " + path);
                }
            }
        }
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if (messageEvent.getPath().equals(Constants.PATH_DISMISS)) {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            notificationManager.cancel(NOTIFICATION_ID);
        }
    }

    public class Constants {
        public static final String PATH_NOTIFICATION = "/ongoingnotification";
        public static final String PATH_DISMISS = "/dismissnotification";
        public static final String KEY_CHATTING = "chatting";
    }
}

package com.ooVoo.oovoosample.Main;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.Wearable;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ooVoo.oovoosample.R;

import java.io.InputStream;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class NotificationActivity extends Activity {
    private static final String TAG = NotificationActivity.class.getSimpleName();

    public static final String EXTRA_CHATTING = "chatting";

    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        mTextView = (TextView) findViewById(R.id.text_view);

        Intent intent = getIntent();
        if (intent != null) {
            //update notification to reflect current video chatting status
            mTextView.setText("Video chat in session : " + intent.getStringExtra(EXTRA_CHATTING).toString());
        }
    }
}
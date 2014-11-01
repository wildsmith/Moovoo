package com.moverio.control;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.ooVoo.oovoosample.R;

import jp.epson.moverio.bt200.AudioControl;
import jp.epson.moverio.bt200.DisplayControl;
import jp.epson.moverio.bt200.SensorControl;

public class BT200CtrlDemoActivity extends Activity implements SensorEventListener {
    private String TAG = "Bt2CtrlDemoActivity";
    private TextView locationText;

    private DisplayControl mDisplayControl = null;
    private AudioControl mAudioControl = null;
    private SensorControl mSensorControl = null;
    private LocationManager mgr;

    private float[] mLastAccelerometer = new float[3];
    private float[] mLastMagnetometer = new float[3];
    private boolean mLastAccelerometerSet = false;
    private boolean mLastMagnetometerSet = false;

    private float[] mR = new float[9];
    private float[] mOrientation = new float[3];
    private float mCurrentDegree = 0f;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mMagnetometer;
    private ImageView mPointer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bt2_ctrl_demo);

        mgr = (LocationManager) getSystemService(LOCATION_SERVICE);

        mDisplayControl = new DisplayControl(this);
        mAudioControl = new AudioControl(this);
        mSensorControl = new SensorControl(this);

        locationText = (TextView) findViewById(R.id.locationLabel);

        Location location = new Location("dummy");
        location.setLongitude(49.866221);
        location.setLatitude(8.683780);
        updatePosition(location);

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        mPointer = (ImageView) findViewById(R.id.pointer);
    }

    @Override
    public void onResume() {
        super.onResume();

        mgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3600000, 1000, onLocationChange);

        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    public void onPause() {
        super.onPause();

        mgr.removeUpdates(onLocationChange);
    }

    LocationListener onLocationChange = new LocationListener() {
        public void onLocationChanged(Location location) {
            updatePosition(location);
        }

        public void onProviderDisabled(String provider) {
            // required for interface, not used
        }

        public void onProviderEnabled(String provider) {
            // required for interface, not used
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
            // required for interface, not used
        }
    };

    protected void updatePosition(Location location) {
        locationText.setText(Double.toString(location.getLatitude()) + ":"
                             + Double.toString(location.getLongitude()));
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor == mAccelerometer) {
            System.arraycopy(event.values, 0, mLastAccelerometer, 0,
                             event.values.length);
            mLastAccelerometerSet = true;
        } else if (event.sensor == mMagnetometer) {
            System.arraycopy(event.values, 0, mLastMagnetometer, 0,
                             event.values.length);
            mLastMagnetometerSet = true;
        }
        if (mLastAccelerometerSet && mLastMagnetometerSet) {
            SensorManager.getRotationMatrix(mR, null, mLastAccelerometer,
                                            mLastMagnetometer);
            SensorManager.getOrientation(mR, mOrientation);
            float azimuthInRadians = mOrientation[0];
            float azimuthInDegress = (float) (Math.toDegrees(azimuthInRadians) + 360) % 360;
            RotateAnimation ra = new RotateAnimation(mCurrentDegree, -azimuthInDegress, Animation.RELATIVE_TO_SELF, 0.5f,
                                                     Animation.RELATIVE_TO_SELF, 0.5f);

            ra.setDuration(250);

            ra.setFillAfter(true);

            mPointer.startAnimation(ra);
            mCurrentDegree = -azimuthInDegress;
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO Auto-generated method stub

    }
}
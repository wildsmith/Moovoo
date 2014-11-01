//
// MainActivity.java
// 
// Created by ooVoo on July 22, 2013
//
// © 2013 ooVoo, LLC.  Used under license. 
//
package com.ooVoo.oovoosample.Main;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.ooVoo.oovoosample.ConferenceManager;
import com.ooVoo.oovoosample.ConferenceManager.SessionListener;
import com.ooVoo.oovoosample.R;
import com.ooVoo.oovoosample.SessionUIPresenter;
import com.ooVoo.oovoosample.Common.AlertsManager;
import com.ooVoo.oovoosample.Common.ParticipantHolder.RenderViewData;
import com.ooVoo.oovoosample.Common.ParticipantVideoSurface;
import com.ooVoo.oovoosample.Common.ParticipantsManager;
import com.ooVoo.oovoosample.Common.Utils;
import com.ooVoo.oovoosample.Settings.SettingsActivity;
import com.ooVoo.oovoosample.Settings.UserSettings;
import com.ooVoo.oovoosample.VideoCall.VideoCallActivity;
import com.oovoo.core.IConferenceCore.ConferenceCoreError;

// Main presenter entity
public class MainActivity extends Activity implements OnClickListener,
		SessionListener, SessionUIPresenter {

	private static final String TAG = MainActivity.class.getName();
	private ConferenceManager mConferenceManager = null;
	private EditText mSessionIdView = null;
	private EditText mDisplayNameView = null;
	private Button mJoinButton = null;
	private ProgressDialog mWaitingDialog = null;
	private ParticipantVideoSurface mPreviewSurface;
	private Boolean isInitialized = false;
	private RenderViewData mRenderViewData = null;
	private boolean isJoining = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		initView();
		initConferenceManager();
	}

	protected void initView() {
		Log.i(TAG, "Setup views ->");
		// Set layout
		setContentView(R.layout.main);
		// Register for button press
		Object obj = findViewById(R.id.joinButton1);
		mJoinButton = (Button) obj;
		mJoinButton.setOnClickListener(this);
		mJoinButton.setEnabled(false);
		
		// Retrieve and display SDK version
		mSessionIdView = (EditText) findViewById(R.id.sessionIdText);
		mDisplayNameView = (EditText) findViewById(R.id.displayNameText);
		
		mPreviewSurface = (ParticipantVideoSurface)findViewById(R.id.preview_layout_id);
		mPreviewSurface.avatar = ((ImageView) findViewById(R.id.myAvatar));
		mPreviewSurface.mVideoView = ((android.opengl.GLSurfaceView) findViewById(R.id.myVideoSurface));
		
		showAvatar();
			
		ActionBar ab = getActionBar();
		if(ab != null){
			ab.setIcon(R.drawable.ic_main);
		}		
		Log.i(TAG, "<- Setup views");
	}
	
	private void showAvatar() {
		mPreviewSurface.avatar.setVisibility(View.VISIBLE);
	}
	
	private void hideAvatar() {
		mPreviewSurface.avatar.setVisibility(View.INVISIBLE);
		mPreviewSurface.mVideoView.setVisibility(View.VISIBLE);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.main_menu, menu);
	    
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item == null)
			return false;

		switch (item.getItemId()) {			
			case R.id.menu_settings:
				if (!isInitialized) {
					Toast.makeText(getApplicationContext(), R.string.initialization_wait, Toast.LENGTH_SHORT).show();
				}

				startActivity(SettingsActivity.class);
		
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void initConferenceManager() {
		if (!isInitialized) {
			if( mConferenceManager == null) {
				Log.i(TAG, "Init ConferenceManager");
				mConferenceManager = ConferenceManager.getInstance(getApplicationContext());
			}
			
			mConferenceManager.removeSessionListener(this);
			mConferenceManager.addSessionListener(this);
			mConferenceManager.initConference();
		}
	}

	public String getAppVersion() {
		String versionName = new String();
		try {
			versionName = this.getPackageManager().getPackageInfo(
					this.getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			Log.e(TAG, "", e);
		}
		return versionName;
	}

	public boolean isOnline() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		try {
			NetworkInfo netInfo = cm.getActiveNetworkInfo();
			if (netInfo != null && netInfo.isConnectedOrConnecting()) {
				return true;
			}
		} catch (Exception e) {
			Log.d(Utils.getOoVooTag(),
					"An exception while trying to find internet connectivity: "
							+ e.getMessage());
			// probably connectivity problem so we will return false
		}
		return false;
	}

	@Override
	public void onClick(View v) {	
		if(v == null)
			return;
		
		if (!isOnline()) {
			Utils.ShowMessageBox(this, "Network Error",
					"No Internet Connection. Please check your internet connection or try again later.");
			return;
		}
		
		switch(v.getId()){
			case R.id.joinButton1:

				if (mConferenceManager.isSdkInitialized()) {
					onJoinSession();
				} else {
					Toast.makeText(getApplicationContext(), R.string.initialization_wait, Toast.LENGTH_SHORT).show();			    
				    initConferenceManager();
				}
				break;
		}
	}
	
	private synchronized void onJoinSession() {
		
		if (isJoining) {
			return;
		}
		
		isJoining = true;

		saveSettings();	

		// Join session
		mJoinButton.setEnabled(false);
		showWaitingMessage();
		mConferenceManager.joinSession();		
	}

//	@Override
//	protected Class<?> getLeftActivity() {
//		return SettingsActivity.class;
//	}

	@Override
	public synchronized void onResume() {
		super.onResume();
		Log.i(TAG, "onResume ->");
		
		// Read settings
		UserSettings settings = mConferenceManager.retrieveSettings();
			
		try {
			// Fill views
			mSessionIdView.setText(settings.SessionID);
			mDisplayNameView.setText(settings.DisplayName);

			// reseting the resolution config
			//settings.Resolution = CameraResolutionLevel.ResolutionHigh;
			Log.i(TAG, "persistSettings ->");
			mConferenceManager.persistSettings(settings);

			Log.i(TAG, "<- persistSettings");

			Log.i(TAG, "loadDataFromSettings ->");
			mConferenceManager.loadDataFromSettings();
			Log.i(TAG, "<- loadDataFromSettings");
			
			mConferenceManager.removeSessionListener(this);
			mConferenceManager.addSessionListener(this);
			
			if (isInitialized) {
				
	    		initPreview(mPreviewSurface);
			
				mConferenceManager.resumePreviewSession();
				
				mJoinButton.setEnabled(true);
			}
			
		} catch (Exception e) {
			AlertsManager.getInstance().addAlert(
					"An Error occured while trying to select Devices");
		}
	}

	@Override
	public void onBackPressed() {
		if (mConferenceManager != null)
			mConferenceManager.leaveSession();
		super.onBackPressed();
	}

	@Override
	public void onPause() {
		super.onPause();

		// mModel.unregisterFromEvents();
		if (isInitialized)
			mConferenceManager.pauseSession();

		mConferenceManager.removeSessionListener(this);

		isInitialized = true;
		saveSettings();
	}
	
	public synchronized void initPreview(ParticipantVideoSurface participantsVideoSurface) {

		try {
			mConferenceManager.setSessionUIPresenter(this);

			ParticipantsManager mParticipantsManager = mConferenceManager.getParticipantsManager();
		
			if (mRenderViewData == null)
				mRenderViewData = mParticipantsManager.getHolder().addGLView(participantsVideoSurface.mVideoView.getId());
			else
				 mParticipantsManager.getHolder().updateGLPreview(participantsVideoSurface.mVideoView.getId(), mRenderViewData);
		
		} catch (Exception e) {
			Log.e(Utils.getOoVooTag(), "", e);
		}
	}
	
	private void saveSettings() {
		UserSettings settingsToPersist = mConferenceManager.retrieveSettings();
		settingsToPersist.SessionID = mSessionIdView.getText().toString();
		settingsToPersist.UserID = android.os.Build.SERIAL;
		settingsToPersist.DisplayName = mDisplayNameView.getText().toString();

		// Save changes
		mConferenceManager.persistSettings(settingsToPersist);
	};

	private void switchToVideoCall() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {				
				hideWaitingMessage(); 
				startActivity(VideoCallActivity.class);
			}
		});
	}
	
	private void showWaitingMessage() {
		 mWaitingDialog = new ProgressDialog(this);
			mWaitingDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			mWaitingDialog.setMessage(getResources().getText(R.string.please_wait));
			mWaitingDialog.setIndeterminate(true);
			mWaitingDialog.setCancelable(false);
			mWaitingDialog.setCanceledOnTouchOutside(false);
			mWaitingDialog.show();
	}
	
	public void hideWaitingMessage() {
		try {
			if (mWaitingDialog != null) {
				mWaitingDialog.dismiss();
			}
			mWaitingDialog = null;
		} catch (Exception ex) {
		}
	}
	
	// Start a new activity using the requested effects
	private void startActivity(Class<?> activityToStart) {
		// Maybe should use this flag just for Video Call activity?
		Intent myIntent = new Intent(this, activityToStart);
		myIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		startActivity(myIntent);
	}

	public void showErrorMessage(final String titleToShow,
			final String msgToShow) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mJoinButton.setEnabled(true);
				hideWaitingMessage(); 
				Utils.ShowMessageBox(MainActivity.this, titleToShow, msgToShow);
			}
		});
	}

	@Override
	public void onSessionError(ConferenceCoreError error) {
		String errorMsg = "An Error occured";
		showErrorMessage("Error", errorMsg);
		isJoining = false;
	}

	@Override
	public void onSessionIDGenerated(String sSessionId) {
		Log.d(Utils.getOoVooTag(), "OnSessionIdGenerated called with: "
				+ sSessionId);
	}

	@Override
	public void onJoinSessionSucceeded() {
		switchToVideoCall();
		isJoining = false;
	}

	@Override
	public void onJoinSessionError(final ConferenceCoreError error) {
		Log.e(TAG, "onJoinSessionError: " + error);
		isJoining = false;
		showErrorMessage("Join Session",
				"Error while trying to join session. " + mConferenceManager.getErrorMessageForConferenceCoreError( error));
	}

	@Override
	public void onJoinSessionWrongDataError() {
		showErrorMessage("Join Session", "Display Name should not be empty");
	}

	@Override
	public void onLeftSession(ConferenceCoreError error) {
	}

	@Override
	public synchronized void onSessionInited() {

		runOnUiThread(new Runnable() {
	        @Override
	        public void run() {
			
	        	try {
	        		Log.i(TAG, "loadDataFromSettings ->");
	    			mConferenceManager.loadDataFromSettings();
	    			Log.i(TAG, "<- loadDataFromSettings");

	        		initPreview(mPreviewSurface);

	        		mConferenceManager.resumePreviewSession();
	        		
					isInitialized = true;
					mJoinButton.setEnabled(true);
					
				} catch (Exception e) {
					Log.e(TAG, "", e);
				}
	        }
		});
	}

	@Override
	public void updateParticipantSurface(final int participantViewId,
			String displayName, final boolean isVideoOn) {

		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				hideAvatar();
			}
		});
	}

	@Override
	public void initSurfaces() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onFullModeChanged(int id) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMultiModeChanged() {
		// TODO Auto-generated method stub
		
	}
}

//
// VideoCallActivity.java
// 
// Created by ooVoo on July 22, 2013
//
// © 2013 ooVoo, LLC.  Used under license. 
//
package com.ooVoo.oovoosample.VideoCall;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.ooVoo.oovoosample.ConferenceManager;
import com.ooVoo.oovoosample.ConferenceManager.ParticipantSwitchListener;
import com.ooVoo.oovoosample.ConferenceManager.SessionControlsListener;
import com.ooVoo.oovoosample.ConferenceManager.SessionListener;
import com.ooVoo.oovoosample.ConferenceManager.SessionParticipantsListener;
import com.ooVoo.oovoosample.R;
import com.ooVoo.oovoosample.SessionUIPresenter;
import com.ooVoo.oovoosample.Alerts.AlertsActivity;
import com.ooVoo.oovoosample.Common.AlertsManager;
import com.ooVoo.oovoosample.Common.Participant;
import com.ooVoo.oovoosample.Common.ParticipantHolder;
import com.ooVoo.oovoosample.Common.ParticipantHolder.VideoParticipant;
import com.ooVoo.oovoosample.Common.ParticipantVideoSurface;
import com.ooVoo.oovoosample.Common.ParticipantVideoSurface.States;
import com.ooVoo.oovoosample.Common.ParticipantsManager;
import com.ooVoo.oovoosample.Common.Utils;
import com.ooVoo.oovoosample.Information.InformationActivity;
import com.ooVoo.oovoosample.Messenger.MessengerActivity;
import com.ooVoo.oovoosample.Messenger.MessengerController;
import com.ooVoo.oovoosample.Settings.UserSettings;
import com.oovoo.core.ConferenceCore;
import com.oovoo.core.ConferenceCore.FrameSize;
import com.oovoo.core.IConferenceCore.CameraResolutionLevel;
import com.oovoo.core.IConferenceCore.ConferenceCoreError;
import com.oovoo.core.device.deviceconfig.VideoFilterData;

// Video presenter entity
public class VideoCallActivity extends Activity implements OnClickListener,
		SessionControlsListener, SessionListener, SessionParticipantsListener,
		SessionUIPresenter, View.OnTouchListener, ParticipantSwitchListener {
	
	private Boolean _initialized = false;
	private ConferenceManager mConferenceManager = null;
	private HashMap<Integer, ParticipantVideoSurface> _surfaces = new HashMap<Integer, ParticipantVideoSurface>();
	private ParticipantVideoSurface mParticipantsVideoSurfaces[];
	private Spinner mResSpinner;
	private VCParticipantsController mVCParticipantsController = null;
	private Spinner mFilterSpinner; 
	private Button mBubbleButton;
	private String mActiveFilterId;
	private boolean isCameraMuted = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		mConferenceManager = ConferenceManager.getInstance(getApplicationContext());
		Log.d(Utils.getOoVooTag(), "savedInstanceState is null: " + (savedInstanceState == null));
		mActiveFilterId = mConferenceManager.getActiveFilter();
		mConferenceManager.setParticipantSwitchListener(this);
		initView();
	}

	protected void initView() {
		// Set layout
		setContentView(R.layout.video_call);
		
		Log.d(Utils.getOoVooTag(), "setting cameraButton");
		Button cameraButton = (Button) findViewById(R.id.cameraButton);
		cameraButton.setOnClickListener(this);

		Log.d(Utils.getOoVooTag(), "setting microphoneButton");
		Button microphoneButton = (Button) findViewById(R.id.microphoneButton);
		microphoneButton.setOnClickListener(this);

		Log.d(Utils.getOoVooTag(), "setting speakersButton");
		Button speakersButton = (Button) findViewById(R.id.speakersButton);
		speakersButton.setOnClickListener(this);

		Log.d(Utils.getOoVooTag(), "setting endOfCallButton");
		Button endOfCallButton = (Button) findViewById(R.id.endOfCallButton);
		endOfCallButton.setOnClickListener(this);
		
		Log.d(Utils.getOoVooTag(), "setting bubbleButton");
		mBubbleButton = (Button) findViewById(R.id.bubbleButton);
		mBubbleButton.setOnClickListener(this);

		Log.d(Utils.getOoVooTag(), "setting resolutionSpinner");
		mResSpinner = (Spinner) findViewById(R.id.resolutionSpinner);
		ArrayList<ResolutionWrapper> values = new ArrayList<ResolutionWrapper>();
		
		values.add(new ResolutionWrapper(CameraResolutionLevel.ResolutionLow, "Low"));
		values.add(new ResolutionWrapper(CameraResolutionLevel.ResolutionMedium, "Med"));
		values.add(new ResolutionWrapper(CameraResolutionLevel.ResolutionHigh, "Hi"));
		values.add(new ResolutionWrapper(CameraResolutionLevel.ResolutionHD, "HD"));
		
		Utils.setSpinnerValues(this, mResSpinner, values);

		mResSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				ResolutionWrapper selectedRes = Utils
						.getSelectedSpinnerValue(mResSpinner);
				UserSettings settings = mConferenceManager.retrieveSettings();
				settings.Resolution = selectedRes.Level;
				mConferenceManager.setVideoResolution(settings.Resolution);
				mConferenceManager.persistSettings(settings);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});

		Log.d(Utils.getOoVooTag(), "setting ParticipantVideoSurfaces");
		mVCParticipantsController = (VCParticipantsController)findViewById(R.id.participants_controller);
		
		mParticipantsVideoSurfaces = new ParticipantVideoSurface[4];
		mParticipantsVideoSurfaces[0] = (ParticipantVideoSurface)findViewById(R.id.preview_layout_id);
		mParticipantsVideoSurfaces[0].avatar = ((ImageView) findViewById(R.id.myAvatar));
		mParticipantsVideoSurfaces[0].nameBox = ((TextView) findViewById(R.id.previewName));
		mParticipantsVideoSurfaces[0].mVideoView = ((android.opengl.GLSurfaceView) findViewById(R.id.myVideoSurface));
		
		mParticipantsVideoSurfaces[1] = (ParticipantVideoSurface)findViewById(R.id.user1_layout_id);
		mParticipantsVideoSurfaces[1].avatar = ((ImageView) findViewById(R.id.user1Avatar));
		mParticipantsVideoSurfaces[1].nameBox = ((TextView) findViewById(R.id.user1Name));
		mParticipantsVideoSurfaces[1].mVideoView = ((android.opengl.GLSurfaceView) findViewById(R.id.user1VideoSurface));

		mParticipantsVideoSurfaces[2] = (ParticipantVideoSurface)findViewById(R.id.user2_layout_id);
		mParticipantsVideoSurfaces[2].avatar = ((ImageView) findViewById(R.id.user2Avatar));
		mParticipantsVideoSurfaces[2].nameBox = ((TextView) findViewById(R.id.user2Name));
		mParticipantsVideoSurfaces[2].mVideoView = ((android.opengl.GLSurfaceView) findViewById(R.id.user2VideoSurface));

		mParticipantsVideoSurfaces[3] = (ParticipantVideoSurface)findViewById(R.id.user3_layout_id);
		mParticipantsVideoSurfaces[3].avatar = ((ImageView) findViewById(R.id.user3Avatar));
		mParticipantsVideoSurfaces[3].nameBox = ((TextView) findViewById(R.id.user3Name));
		mParticipantsVideoSurfaces[3].mVideoView = ((android.opengl.GLSurfaceView) findViewById(R.id.user3VideoSurface));
		
		mParticipantsVideoSurfaces[0].setOnTouchListener(this);
		mParticipantsVideoSurfaces[1].setOnTouchListener(this);
		mParticipantsVideoSurfaces[2].setOnTouchListener(this);
		mParticipantsVideoSurfaces[3].setOnTouchListener(this);
		
		mVCParticipantsController.onResize();
		
		ActionBar ab = getActionBar();
		if(ab != null){
			ab.setHomeButtonEnabled(false);
			ab.setDisplayShowTitleEnabled(true);
			ab.setDisplayShowHomeEnabled(true);
			ab.setDisplayHomeAsUpEnabled(false);
			ab.setDisplayUseLogoEnabled(false);
			ab.setIcon(R.drawable.ic_main);
		}
		
		Log.d(Utils.getOoVooTag(), "setting filterSpinner");
		mFilterSpinner = (Spinner) findViewById(R.id.filterSpinner);
		ArrayList<VideoFilterDataWrapper> f_values = new ArrayList<VideoFilterDataWrapper>();
		VideoFilterData[] arr = mConferenceManager.getAvailableFilters();
		VideoFilterDataWrapper none = null;
		for( VideoFilterData d : arr) {
			VideoFilterDataWrapper w = new VideoFilterDataWrapper(d);
			if( d.id().equals(Camera.Parameters.EFFECT_NONE)) {
				none = w;
			}

			f_values.add(w);
		}		
		Utils.setSpinnerValues(this, mFilterSpinner, f_values);
		Utils.setSelectedSpinnerValue(mFilterSpinner, none);

		mFilterSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				VideoFilterDataWrapper selectedRes = Utils
						.getSelectedSpinnerValue(mFilterSpinner);
//				UserSettings settings = mConferenceManager.retrieveSettings();
//				settings.Resolution = selectedRes.Level;
				mActiveFilterId = selectedRes.id();
				mConferenceManager.setActiveFilter(mActiveFilterId);
//				mConferenceManager.persistSettings(settings);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});

		showMessagesButton();
	}
	
	private void showMessagesButton() {
		if (mConferenceManager.inCallMessagesPermitted()) {
			mFilterSpinner.setVisibility(View.GONE);
			mBubbleButton.setVisibility(View.VISIBLE);
		} else {
			showFiltersButton();
		}
	}
	
	private void showFiltersButton() {
		mFilterSpinner.setVisibility(View.VISIBLE);
		mBubbleButton.setVisibility(View.GONE);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.vc_menu, menu);
	    return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item == null)
			return false;

		switch (item.getItemId()) {
			case android.R.id.home:
				mConferenceManager.endOfCall();
				finish();
				return true;
			case R.id.menu_information:
				openInfrormationView();
				return true;
			case R.id.menu_alerts:
				openAlertsView();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	// Start a new activity using the requested effects
	private void startActivity(Class<?> activityToStart) {
		
		// Maybe should use this flag just for Video Call activity?
		Intent myIntent = new Intent(this, activityToStart);
		myIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		startActivity(myIntent);
	}
	
	private void openMessengerView() {
		startActivity(MessengerActivity.class);
	}

	@Override
	public void onClick(View v) {
		// Check which button was pressed
		switch (v.getId()) {
		case R.id.endOfCallButton: {
			mConferenceManager.endOfCall();
			break;
		}
		case R.id.cameraButton: {
			fireCameraEnabled(false);
			mConferenceManager.toggleCameraMute();
			break;
		}
		case R.id.microphoneButton: {
			fireMicrophoneEnabled(false);
			mConferenceManager.toggleMicrophoneMute();
			break;
		}
		case R.id.speakersButton: {
			fireSpeakersEnabled(false);
			mConferenceManager.toggleSpeakersMute();
			break;
		}
		case R.id.bubbleButton: {
			openMessengerView();
			break;
		}
		default: {
			break;
		}
		}
	}

	private void openInfrormationView() {
		startActivity(InformationActivity.class);
	}

	@Override
	protected void onDestroy() {
		Log.d(Utils.getOoVooTag(), "VideoCallActivity onDestroy");
		super.onDestroy();
	}

	private void openAlertsView() {
		startActivity(AlertsActivity.class);
	}

	public void onCameraOn() {
		Log.d(Utils.getOoVooTag(), "OnCameraOn");
		mConferenceManager.setCameraMuted(isCameraMuted);
	}
	
	
	// Called from model upon camera mute change
	@Override
	public void onSetCameraMuted(final boolean isMuted) {
		Log.d(Utils.getOoVooTag(), "onSetCameraMuted to " + isMuted);
		
		// Just GUI. SDK calls are in model
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				fireCameraMuted(isMuted);
			}
		});
		AlertsManager.getInstance().addAlert( "Camera mute set to: " + isMuted);
	}

	private void fireCameraMuted(boolean isMuted) {
		
		if (!isMuted) {
			mConferenceManager.setActiveFilter(mActiveFilterId);
		}

		final Button btn = (Button) (findViewById(R.id.cameraButton));
		final SurfaceView myVideoSurface = ((SurfaceView) findViewById(R.id.myVideoSurface));
		
		int new_v = isMuted ? SurfaceView.INVISIBLE : SurfaceView.VISIBLE;
		btn.setSelected(isMuted ? true : false);
		myVideoSurface.setVisibility(new_v);
		ParticipantVideoSurface surface = _surfaces.get(myVideoSurface.getId());
		if (surface != null) {
			States state = isMuted ? States.STATE_AVATAR : States.STATE_VIDEO;
			surface.setState(state);
			
			ParticipantsManager participantsManager = mConferenceManager.getParticipantsManager();
			if (participantsManager.getHolder().isFullMode() && 
					!participantsManager.getHolder().isFullMode(ConferenceManager.LOCAL_PARTICIPANT_ID_DEFAULT)) {
				surface.hideSurface();
				surface.setVisibility(View.INVISIBLE);
			}
			
			participantsManager.getHolder().setVideoOn(ConferenceManager.LOCAL_PARTICIPANT_ID_DEFAULT, isCameraMuted ? false : true);
		}

		Log.d(Utils.getOoVooTag(), "set visibility to " + isMuted + " new: " + new_v);
	}

	// Called from model upon microphone mute change
	@Override
	public void onSetMicrophoneMuted(final boolean isMuted) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				fireMicrophoneMuted(isMuted);
			}
		});
		AlertsManager.getInstance().addAlert("Microphone mute set to: " + isMuted);
	}

	private void fireMicrophoneMuted(boolean isMuted) {
		Button btn = (Button) (findViewById(R.id.microphoneButton));
		btn.setSelected(isMuted ? true : false);
	}

	// Called from model upon speakers mute change
	@Override
	public void onSetSpeakersMuted(final boolean isMuted) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				fireSpeakersMuted(isMuted);
			}
		});
		AlertsManager.getInstance().addAlert("Speakers mute set to: " + isMuted);
	}

	private void fireSpeakersMuted(boolean isMuted) {
		// Just GUI. SDK calls are in model
		final Button btn = ((Button) (findViewById(R.id.speakersButton)));
		btn.setSelected(isMuted ? true : false);
	}

	public void onSetCameraEnabled(final boolean isEnabled) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				fireCameraEnabled(isEnabled);
			}
		});
	}

	private void fireCameraEnabled(boolean isEnabled) {
		final Button btn = (Button) (findViewById(R.id.cameraButton));
		btn.setEnabled(isEnabled);
	}

	public void onSetMicrophoneEnabled(final boolean isEnabled) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				fireMicrophoneEnabled(isEnabled);
			}
		});
	}

	private void fireMicrophoneEnabled(boolean isEnabled) {
		Button btn = (Button) (findViewById(R.id.microphoneButton));
		btn.setEnabled(isEnabled);
	}

	public void onSetSpeakersEnabled(final boolean isEnabled) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				fireSpeakersEnabled(isEnabled);
			}
		});
	}

	private void fireSpeakersEnabled(boolean isEnabled) {
		Button btn = ((Button) (findViewById(R.id.speakersButton)));
		btn.setEnabled(isEnabled);
	}

	@Override
	protected void onPause() {
		super.onPause();
		isCameraMuted = mConferenceManager.isCameraMuted();
		mConferenceManager.setCameraMuted(true);
		mConferenceManager.pauseSession();
		mConferenceManager.removeSessionControlsListener(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		UserSettings settings = mConferenceManager.retrieveSettings();

		CameraResolutionLevel level = settings.Resolution; //mConferenceManager.getCameraResolutionLevel();
		Log.d(Utils.getOoVooTag(), "Camera resolution is: " + level);
		Utils.setSelectedSpinnerValue(mResSpinner, new ResolutionWrapper( level, "Doesnt Matter"));
		
		mConferenceManager.addSessionControlsListener(this);
		
		if (!_initialized) {
			_initialized = true;
			initSession(mParticipantsVideoSurfaces);
		}
		
		mConferenceManager.resumeSession();
		
		ParticipantsManager mParticipantsManager = mConferenceManager.getParticipantsManager();
		if( mParticipantsManager.getNoOfVideosOn() > 0) {
			ParticipantHolder holder = mParticipantsManager.getHolder();
			Collection<VideoParticipant> users = holder.getParticipants();
			for( VideoParticipant vp : users) {
				if( holder.isVideoOn( vp.getParticipantId())) {
					int participantViewId = holder.getViewIdByParticipant(vp.getParticipantId());
					ParticipantVideoSurface surface = _surfaces.get(participantViewId);
					if (surface != null) {
						surface.showAvatar();
					}
				}
			}
		}
		
		fireMicrophoneMuted( mConferenceManager.isMicMuted());
		fireSpeakersMuted( mConferenceManager.isSpeakerMuted());
	}

	public synchronized void initSession(ParticipantVideoSurface[] mParticipantsVideoSurfaces) {
		// Select devices
		try {
			mConferenceManager.addSessionListener(this);
			mConferenceManager.addSessionParticipantsListener(this);
			mConferenceManager.setSessionUIPresenter(this);

			ParticipantsManager mParticipantsManager = mConferenceManager.getParticipantsManager();
			_surfaces.clear();
			Log.i(Utils.getOoVooTag(), "VideoCallActivity :: initSession -> mParticipantsVideoSurfaces length = " + mParticipantsVideoSurfaces.length );
			for (int i = 0; i < mParticipantsVideoSurfaces.length; i++) {
				mParticipantsManager.getHolder().addGLView(mParticipantsVideoSurfaces[i].mVideoView.getId());
				_surfaces.put(mParticipantsVideoSurfaces[i].mVideoView.getId(), mParticipantsVideoSurfaces[i]);
			}
			mConferenceManager.initSession(mParticipantsVideoSurfaces);
			mConferenceManager.setUIReadyState(true);
		} catch (Exception e) {
			Log.e(Utils.getOoVooTag(), "", e);
		}
	}

	@Override
	public void onParticipantJoinedSession(final String sParticipantId, final int participantViewId, final String sOpaqueString) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Log.i(Utils.getOoVooTag(), "VideoCallActivity :: " + sParticipantId +" joined to conference;  {participantViewId = " + participantViewId +" }");
				if (participantViewId != -1 && _surfaces != null) {
					ParticipantVideoSurface surface = _surfaces.get(participantViewId);
					if (surface != null) {
						surface.setVisibility(View.VISIBLE);
						surface.showAvatar();
						surface.setName(sOpaqueString);
						surface.setState(States.STATE_AVATAR);
						
						if (mConferenceManager.getParticipantsManager().getHolder().isFullMode()) {
							surface.hideSurface();
							surface.setVisibility(View.INVISIBLE);
							surface.setState(States.STATE_VIDEO);
							ConferenceCore.instance().receiveParticipantVideoOn(sParticipantId);
						}
					}

					mVCParticipantsController.onResize();
				}
			}
		});
	}

	@Override
	public void onJoinSessionError(ConferenceCoreError error) {
	}

	@Override
	public void onJoinSessionWrongDataError() {
	}

	@Override
	public void onJoinSessionSucceeded() {
	}

	@Override
	public void onSessionIDGenerated(String sSessionId) {
	}

	@Override
	public void onSessionError(ConferenceCoreError error) {
	}

	@Override
	public void onLeftSession(ConferenceCoreError eErrorCode) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Log.d(Utils.getOoVooTag(), "onSessionLeft (JAVA MF)");
				// // Just GUI. SDK calls are in model
//				startActivity(MainActivity.class);
				// Kill the activity so it will not remain in the stack
				finish();
			}
		});
	}
	
	

	@Override
	public void finish() {
		MessengerController.getInstance().clear();
		
		if(mConferenceManager != null)
			mConferenceManager.setUIReadyState(false);
		super.finish();
	}

	@Override
	public void initSurfaces() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				for (ParticipantVideoSurface surf : _surfaces.values()) {
					surf.showEmptyCell();
				}
			}
		});
	}

	@Override
	public void updateParticipantSurface(final int participantViewId, final String displayName, final boolean isVideoOn) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				ParticipantsManager participantsManager = mConferenceManager.getParticipantsManager();
				ParticipantVideoSurface surface = _surfaces.get(participantViewId);
				
				if (surface != null) {
					surface.setName(displayName);
					surface.showAvatar();
					surface.setState(States.STATE_AVATAR);
					
					if (participantsManager.getHolder().isFullMode() && 
							participantsManager.getHolder().getViewIdForFullMode() != participantViewId) {
						surface.hideSurface();
						surface.setVisibility(View.INVISIBLE);
						surface.setState(isVideoOn ? States.STATE_PAUSED : States.STATE_AVATAR);
					} else {
						surface.setVisibility(View.VISIBLE);
						if (isVideoOn) {
							surface.showVideo();
							surface.hideUserStatusInfo();
							surface.setState(States.STATE_VIDEO);
						}
					}
				}
			}
		});
	}

	@Override
	public void onBackPressed() {
		mConferenceManager.endOfCall();
		finish();
		super.onBackPressed();
	}

	public void onParticipantVideoPaused(final int participantViewId) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (participantViewId != -1) {
					ParticipantVideoSurface surface = _surfaces.get(participantViewId);
					if (surface != null) {
						surface.showAvatar();
						surface.showUserStatusInfo();
						surface.setState(States.STATE_PAUSED);
					}
				}
			}
		});
	}

	public void onParticipantVideoTurnedOff(final ConferenceCoreError eErrorCode,
			final int participantViewId, final String sParticipantId) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (participantViewId != -1) {
					ParticipantVideoSurface surface = _surfaces.get(participantViewId);
					if (surface != null) {
						surface.showAvatar();
						surface.hideUserStatusInfo();
						surface.setState(States.STATE_AVATAR);
					}
				}
			}
		});
	}

	public void onParticipantVideoResumed(final int participantViewId,
			final String sParticipantId) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (participantViewId != -1) {
					ParticipantVideoSurface surface = _surfaces
							.get(participantViewId);
					if (surface != null) {
						surface.showVideo();
						surface.setName(sParticipantId);
						surface.setState(States.STATE_VIDEO);
					}
				}
			}
		});
	}

	public void onParticipantLeftSession(final int participantViewId, final String sParticipantId) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (participantViewId != -1) {
					ParticipantVideoSurface surface = _surfaces.get(participantViewId);
					if (surface != null) {
						surface.setVisibility(View.INVISIBLE);
						surface.showEmptyCell();
						surface.setState(States.STATE_EMPTY);
					}
					mVCParticipantsController.onResize();
				}
			}
		});
	}

	public void onParticipantVideoTurnedOn(ConferenceCoreError eErrorCode,
			final String sParticipantId, FrameSize frameSize,
			final int participantViewId, final String displayName) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (participantViewId != -1) {
					ParticipantVideoSurface surface = _surfaces.get(participantViewId);
					if (surface != null) {
						ParticipantsManager participantsManager = mConferenceManager.getParticipantsManager();

						surface.setName(displayName);
						
						if (participantsManager.getHolder().isFullMode() && 
								!participantsManager.getHolder().isFullMode(sParticipantId)) {
							surface.hideSurface();
							surface.setVisibility(View.INVISIBLE);
						} else {
							surface.showVideo();
						}
						surface.setState(States.STATE_VIDEO);
					}
				}
			}
		});
	}
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (v != null) {
			if (v.getVisibility() == View.INVISIBLE)
				return false;
			
			if (_surfaces != null) {
				for (ParticipantVideoSurface surfaceHolder : _surfaces.values()) {
					if (surfaceHolder.getId() == v.getId()) {
						ParticipantsManager participantsManager = mConferenceManager.getParticipantsManager();
						String participantId = participantsManager.getHolder().getParticipantByViewId(surfaceHolder.mVideoView.getId());
						if (participantsManager.getHolder().isFullMode(participantId) || surfaceHolder.getState() != States.STATE_AVATAR) {
							mConferenceManager.switchUIFullMode(surfaceHolder.mVideoView.getId());
						}
						
						break;
					}						
				}
			}
		}
		return false;
	}
	
	public void onFullModeChanged(final int participantViewId){
		runOnUiThread(new Runnable() {
			@Override
			public void run() {				
				if (participantViewId != -1 && _surfaces != null) {
					for(ParticipantVideoSurface surfaceHolder : _surfaces.values()) {
						
						if (surfaceHolder.mVideoView.getId() == participantViewId) {
							surfaceHolder.setVisibility(View.VISIBLE);
						} else {
							surfaceHolder.hideSurface();
							surfaceHolder.setVisibility(View.INVISIBLE);
						}	
						if (surfaceHolder.getVisibility() == View.VISIBLE && 
							surfaceHolder.mVideoView.getId() == R.id.myVideoSurface) {
							showFiltersButton();
						}
					}
				}

				mVCParticipantsController.onModeUpdated(VCParticipantsController.FULL_MODE);
			}
		});
	}
	
	public void onMultiModeChanged(){
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (_surfaces != null) {
					boolean needResume = false;
					for(ParticipantVideoSurface surfaceHolder : _surfaces.values()){
						
						if (mConferenceManager.isVideoRenderActive(surfaceHolder.mVideoView.getId())) {
							surfaceHolder.setVisibility(View.VISIBLE);		
							surfaceHolder.showSurface();
						} else {
							surfaceHolder.setVisibility(View.INVISIBLE);	
						}
						if (surfaceHolder.getState() == States.STATE_AVATAR) {
							surfaceHolder.setVisibility(View.VISIBLE);
							surfaceHolder.update();
						}
						else if(surfaceHolder.getState() == States.STATE_PAUSED){
							needResume = true;
							surfaceHolder.setVisibility(View.VISIBLE);
							surfaceHolder.setState(States.STATE_VIDEO);
							surfaceHolder.update();
						}
					}
					if(needResume)
					{
						ParticipantHolder holder = getParticipantHolder();
						if(holder != null)
						{
							holder.Resume();
						}
					}
				}
				showMessagesButton();
				mVCParticipantsController.onModeUpdated(VCParticipantsController.MULTI_MODE);
			}
		});
	}
	
	public ParticipantHolder getParticipantHolder() {
		return ((mConferenceManager == null || mConferenceManager.getParticipantsManager() == null)? null : mConferenceManager.getParticipantsManager().getHolder());
	}

	@Override
	public void onSessionInited() {
		mConferenceManager.setCameraMuted(false);
	}

	@Override
	public void onParticipantVideoTurned(String participantId, boolean isOn) {
		
		if (participantId.isEmpty()) {
			isCameraMuted = isOn ? false : true;
		}
	}
}

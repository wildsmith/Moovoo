//
// ConferenceManager.java
// 
// Created by ooVoo on July 22, 2013
//
// © 2013 ooVoo, LLC.  Used under license. 
//
package com.ooVoo.oovoosample;

import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.os.Build;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceView;
import android.view.WindowManager;

import com.ooVoo.oovoosample.Common.AlertsManager;
import com.ooVoo.oovoosample.Common.Participant;
import com.ooVoo.oovoosample.Common.ParticipantHolder;
import com.ooVoo.oovoosample.Common.ParticipantsManager;
import com.ooVoo.oovoosample.Common.Utils;
import com.ooVoo.oovoosample.Messenger.MessengerController;
import com.ooVoo.oovoosample.Settings.MediaDeviceWrapper;
import com.ooVoo.oovoosample.Settings.UserSettings;
import com.ooVoo.oovoosample.Settings.UserSettingsManager;
import com.ooVoo.oovoosample.util.CommandQueued;
import com.oovoo.core.ClientCore.VideoChannelPtr;
import com.oovoo.core.ConferenceCore;
import com.oovoo.core.ConferenceCore.FrameSize;
import com.oovoo.core.ConferenceCore.IMediaDeviceInformation;
import com.oovoo.core.ConferenceCore.MediaDevice;
import com.oovoo.core.ConferenceCore.MediaDeviceInfo.DeviceState;
import com.oovoo.core.Exceptions.DeviceNotSelectedException;
import com.oovoo.core.IConferenceCore;
import com.oovoo.core.IConferenceCore.CameraResolutionLevel;
import com.oovoo.core.IConferenceCore.ConferenceCoreError;
import com.oovoo.core.IConferenceCore.ConnectionStatistics;
import com.oovoo.core.IConferenceCore.DeviceType;
import com.oovoo.core.IConferenceCore.LogLevel;
import com.oovoo.core.IConferenceCoreListener;
import com.oovoo.core.ILoggerListener;
import com.oovoo.core.Utils.LogSdk;
import com.oovoo.core.device.deviceconfig.VideoFilterData;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class ConferenceManager implements IConferenceCoreListener, ILoggerListener
{

	private static final int JOIN_SESSION = 0;
	private static final int LEAVE_SESSION = 1;
	private static final int MUTE_CAMERA = 2;
	private static final int MUTE_SPEAKERS = 3;
	private static final int MUTE_MIC = 4;
	private static final int END_SESSION = 5;
	private static final int VIDEO_RESOLUTION = 6;
	private static final int TURN_ON_PARTICIPANT_VIDEO = 7;
	private static final int TURN_OFF_PARTICIPANT_VIDEO = 8;
	private static final int INIT_CONFERENCE_CORE = 9;
	private static final int UI_SWITCH_CONFERENCE_MODE = 10;
	private static final int UI_PREPARE_HOLDER_USER = 11;
	private static final int UI_READY = 12;
	public static final String LOCAL_PARTICIPANT_ID_DEFAULT = ""; 

	private UserSettingsManager mSettingsManager;
	private static final String TAG = ConferenceManager.class.getSimpleName();
	private CommandQueued mConferenceQueue = null;
	private static ConferenceManager instance;
	private Context mApp = null;
	private List<SessionListener> mSessionListenerList = null;
	private List<SessionParticipantsListener> mSessionParticipantsListenerList = null;
	private List<SessionControlsListener> mSessionControlsListenerList = null;
	private SessionUIPresenter mSessionUIPresenter = null;
	private ParticipantsManager mParticipantsManager = null;
	private IConferenceCore mConferenceCore = null;
	private boolean mIsCameraMute = false;
	private boolean mIsMicrophoneMute = false;
	private boolean mAreSpeakersMute = false;
	private ArrayList<Message> mDelayedMessages;
	private boolean isUIReadyState = false;
	private boolean isSdkInitialize = false;
	private FileLogger mFileLogger = null;
	private SurfaceView mSurfaceView = null;
	private ParticipantSwitchListener mParticipantSwitchListener = null;

	public boolean isCameraMuted() { return mIsCameraMute; }
	
	public static ConferenceManager getInstance(Context app)
	{
		if (instance == null)
		{
			instance = new ConferenceManager(app);
		}
		return instance;
	}

	private ConferenceManager(Context app)
	{
		mApp = app;
		
//		mConferenceCore = ConferenceCore.instance(mApp);
		
		Log.i(TAG, "Init SettingsManager ");
		mSettingsManager = new UserSettingsManager(mApp);

		Log.i(TAG, "Init ParticipantsManager ");
		mParticipantsManager = new ParticipantsManager();

		Log.i(TAG, "Init AlertsManager ");
		AlertsManager.getInstance();

		Log.i(TAG, "Init ConferenceQueue ");
		if (mConferenceQueue == null)
		{
			try
			{
				mConferenceQueue = new CommandQueued("ConferenceManager")
				{
					protected void onHandleCommandMessage(Message msg)
					{
						onConferenceEvent(msg);
					}
				};
			}
			catch (Exception e)
			{
				Log.e(TAG, "", e);
			}
		}
		
		mFileLogger = new FileLogger();
		
		Log.i(TAG, "Init done. ConferenceManager created.");
	}
	
	public void resetFlagSdkInited() {
		isSdkInitialize = false;
		Log.i(TAG, "Reset InitSdk done.");
	}

	public void initConference()
	{
		if (mConferenceQueue != null)
			mConferenceQueue.sendMessage(INIT_CONFERENCE_CORE, 1000);
	}

	private void initConferenceCore()
	{
		if( mConferenceCore == null) {
			Log.i(TAG, "================================== Init ConferenceCore started ====================================");

			Log.i(TAG, " Register for internal events in ConferenceCore");
			// Register for internal events
			mConferenceCore = ConferenceCore.instance(mApp, this);
		}
		
//		try
//		{
//			Log.i(TAG, "Set context in ConferenceCore started");
//			mConferenceCore.setContext(mApp);
//			Log.i(TAG, "Set context in ConferenceCore finished");
//		}
//		catch (CoreException e)
//		{
//			Log.e(TAG, "", e);
//		}
//		catch (Exception ex)
//		{
//			Log.e(TAG, "", ex);
//		}
		Log.i(TAG, " Register for internal events in ConferenceCore");
		// Register for internal events
		UserSettings settings = retrieveSettings();

		ConferenceCoreError err = mConferenceCore.initSdk(settings.AppId, settings.AppToken, settings.BaseURL); 
		Log.d(TAG, "appId = " + settings.AppId + " token = " + settings.AppToken + " err = " + err);
		String errStr = mConferenceCore.getErrorMessageForConferenceCoreError(err);
		isSdkInitialize = (err == ConferenceCoreError.OK);
		if( isSdkInitialize)
			Log.i(TAG, "Init	ConferenceCore finished");
		else
			Log.e(TAG, "Init ConferenceCore failed: " + errStr);
		
		if (mSessionListenerList != null)
		{
			for (SessionListener listener : mSessionListenerList)
			{
				listener.onSessionInited();
			}
		}
	}

	public void addSessionListener(SessionListener l)
	{
		if (mSessionListenerList == null)
			mSessionListenerList = new ArrayList<SessionListener>();
		mSessionListenerList.add(l);
	}

	public void removeSessionListener(SessionListener l)
	{
		if (mSessionListenerList != null)
			mSessionListenerList.remove(l);
	}

	public void addSessionParticipantsListener(SessionParticipantsListener l)
	{
		if (mSessionParticipantsListenerList == null)
		{
			mSessionParticipantsListenerList = new ArrayList<SessionParticipantsListener>();
		}
		mSessionParticipantsListenerList.add(l);
	}

	public void removeSessionParticipantsListener(SessionParticipantsListener l)
	{
		if (mSessionParticipantsListenerList != null)
			mSessionParticipantsListenerList.remove(l);
	}

	public void addSessionControlsListener(SessionControlsListener listener)
	{
		if (mSessionControlsListenerList == null)
			mSessionControlsListenerList = new ArrayList<SessionControlsListener>();
		mSessionControlsListenerList.add(listener);
	}

	public void removeSessionControlsListener(SessionControlsListener listener)
	{
			mSessionControlsListenerList.remove(listener);
	}

	public void setSessionUIPresenter(SessionUIPresenter presenter)
	{
		mSessionUIPresenter = presenter;
	}

	private void onConferenceEvent(Message msg)
	{
		try
		{
			int command = msg.what;
			Log.i(Utils.getOoVooTag(), "onConferenceEvent :: " + command);
			switch (command)
			{
			case JOIN_SESSION:
				doJoinSession();
				break;
			case LEAVE_SESSION:
				doLeaveSession();
				break;
			case END_SESSION:
				doEndOfCall();
				break;
			case MUTE_CAMERA:
				setCameraMuted(!mIsCameraMute);
				break;
			case MUTE_MIC:
				setMicrophoneMuted(!mIsMicrophoneMute);
				break;
			case MUTE_SPEAKERS:
				setSpeakersMuted(!mAreSpeakersMute);
				break;
			case VIDEO_RESOLUTION:
				if (msg.obj != null && msg.obj instanceof CameraResolutionLevel)
				{
					doSetVideoResolution((CameraResolutionLevel) msg.obj);
				}
				break;
			case TURN_ON_PARTICIPANT_VIDEO:
				if (msg.obj != null && msg.obj instanceof String)
				{
					doTurnParticipantVideoOn((String) msg.obj);
				}
				break;
			case TURN_OFF_PARTICIPANT_VIDEO:
				if (msg.obj != null && msg.obj instanceof String[])
				{
					String[] data = (String[]) msg.obj;
					doTurnParticipantVideoOff(data[0], data[1]);
				}
				break;
			case INIT_CONFERENCE_CORE:
				initConferenceCore();
				break;
			case UI_SWITCH_CONFERENCE_MODE:
				if (msg.obj != null && msg.obj instanceof Integer)
				{
					Integer data = (Integer) (msg.obj);
					doSwitchUIFullMode(data.intValue());
				}
				break;
			case UI_READY:
				Boolean ready = (Boolean) msg.obj;
				isUIReadyState = ready.booleanValue();
				if (isUIReadyState)
				{

					if (mDelayedMessages != null && !mDelayedMessages.isEmpty())
					{
						Log.i(Utils.getOoVooTag(), "UI is ready :: continue with delayed messages "
								+ mDelayedMessages.size());
						for (int i = 0; i < mDelayedMessages.size(); i++)
						{
							Message message = mDelayedMessages.get(i);
							Log.i(Utils.getOoVooTag(), "\t\tDelayed message = " + message.what);
							if (message.what == UI_PREPARE_HOLDER_USER)
							{
								if (message.obj != null && message.obj instanceof String[])
								{
									String[] data = (String[]) (message.obj);
									prepareParticipantActiveRender(data[0], data[1]);
								}
							}
						}
						mDelayedMessages.clear();
						mDelayedMessages = null;
					}
				}
				break;
			case UI_PREPARE_HOLDER_USER:
				Log.i(Utils.getOoVooTag(), "UI_PREPARE_HOLDER_USER event :: isUIReadyState = " + isUIReadyState);

				if (!isUIReadyState)
					handleWithDelay(msg);
				else
				{
					if (msg.obj != null && msg.obj instanceof String[])
					{
						String[] data = (String[]) (msg.obj);
						prepareParticipantActiveRender(data[0], data[1]);
					}
				}
				break;

			}
		}
		catch (Exception e)
		{
			Log.e(TAG, "", e);
		}
	}

	private void handleWithDelay(Message message)
	{
		if (mDelayedMessages == null)
		{
			mDelayedMessages = new ArrayList<Message>();
		}
		Log.i(Utils.getOoVooTag(), "HANDLE WITH DELAY " + message.what);
		Message msg = new Message();
		msg.what = (int) message.what;
		msg.obj = message.obj;
		mDelayedMessages.add(mDelayedMessages.size(), msg);
	}

	public void setUIReadyState(boolean ready)
	{
		if (mConferenceQueue != null)
			mConferenceQueue.sendMessage(UI_READY, Boolean.valueOf(ready));
	}

	public void switchUIFullMode(int id)
	{
		if (mConferenceQueue != null)
			mConferenceQueue.sendMessage(UI_SWITCH_CONFERENCE_MODE, Integer.valueOf(id));
	}

	public void joinSession()
	{
		Log.i(Utils.getOoVooTag(), "SENDING TO QUEUE JOIN_SESSION");

		if (mConferenceQueue != null)
			mConferenceQueue.sendMessage(JOIN_SESSION);
	}

	public void leaveSession()
	{
		if (mConferenceQueue != null)
			mConferenceQueue.sendMessage(LEAVE_SESSION);
	}

	// Start End of Call logic
	public void endOfCall()
	{
		if (mConferenceQueue != null)
			mConferenceQueue.sendMessage(END_SESSION);
	}

	// Start toggle camera mute logic
	public void toggleCameraMute()
	{
		if (mConferenceQueue != null)
			mConferenceQueue.sendMessage(MUTE_CAMERA);
	}

	// Start toggle microphone mute logic
	public void toggleMicrophoneMute()
	{
		if (mConferenceQueue != null)
			mConferenceQueue.sendMessage(MUTE_MIC);
	}

	// Start toggle speakers mute logic
	public void toggleSpeakersMute()
	{
		if (mConferenceQueue != null)
			mConferenceQueue.sendMessage(MUTE_SPEAKERS);
	}

	public void setVideoResolution(CameraResolutionLevel resolution)
	{
		if (mConferenceQueue != null)
			mConferenceQueue.sendMessage(VIDEO_RESOLUTION, resolution);
	}

	public void turnParticipantVideoOn(String id)
	{
		if (mConferenceQueue != null)
			mConferenceQueue.sendMessage(TURN_ON_PARTICIPANT_VIDEO, id);
	}

	public void turnParticipantVideoOff(String id, String displayName)
	{
		if (mConferenceQueue != null)
			mConferenceQueue.sendMessage(TURN_OFF_PARTICIPANT_VIDEO, new String[] { id, displayName });
	}

	private void doTurnParticipantVideoOn(String id)
	{
		Log.d( TAG, "turning participant video ON for " + id);
		if (ConferenceCoreError.OK == mConferenceCore.receiveParticipantVideoOn(id) && mParticipantsManager.getHolder() != null) {
			mParticipantsManager.getHolder().setVideoOn(id, true);
		
			if (mParticipantSwitchListener != null)
			{
				mParticipantSwitchListener.onParticipantVideoTurned(id, true);
			}
		}
	}

	private void doTurnParticipantVideoOff(String id, String displayName)
	{
		if( mParticipantsManager.getHolder() != null)
		{
			mParticipantsManager.getHolder().setVideoOn(id, false);
			if (ConferenceCoreError.OK == mConferenceCore.receiveParticipantVideoOff(id))
			{
				mParticipantsManager.getHolder().turnVideoOff(id);
				addAlert(displayName + " turned video Off", ConferenceCoreError.OK);
				
				if (mParticipantSwitchListener != null)
				{
					mParticipantSwitchListener.onParticipantVideoTurned(id, false);
				}
			}
		}
	}

	private void doSetVideoResolution(CameraResolutionLevel resolution)
	{
		try
		{
			Log.d(TAG, "trying to set resolution " + resolution);
			mConferenceCore.setCameraResolutionLevel(resolution);
		}
		catch (Exception ex)
		{
			Log.e(TAG, "", ex);
			addAlert("Error while changing resolution", ConferenceCoreError.OK);
		}
	}
	
	
	public void turnCameraOn(boolean on) {
		try
		{
			Log.d(Utils.getOoVooTag(), "Turn camera to: " + on);
			if (on)
			{
				mConferenceCore.turnCameraOn();
			}
			else
			{
				mConferenceCore.turnCameraOff();
			}
		}
		catch (Exception e)
		{
			Log.e(Utils.getOoVooTag(), "An Exception thrown while calling turnCameraOn " + on, e);
			addAlert("An error occured while calling turnCameraOn", ConferenceCoreError.Error);
		}
	}

	public void setCameraMuted(final boolean muted)
	{
		try
		{
			mIsCameraMute = muted;
			
			Log.d(Utils.getOoVooTag(), "Setting camera mute to: " + muted);
			if (!muted)
			{
				mConferenceCore.turnPreviewOn();
				mConferenceCore.turnVideoTransmitOn();
			}
			else
			{
				mConferenceCore.turnPreviewOff();
				mConferenceCore.turnVideoTransmitOff();
			}
		}
		catch (Exception e)
		{
			Log.e(Utils.getOoVooTag(), "An Exception thrown while calling setCameraMuted " + muted, e);
			addAlert("An error occured while calling turnMyVideoOn", ConferenceCoreError.Error);
		}
		finally
		{
			if (mSessionControlsListenerList != null)
			{
				for (SessionControlsListener listener : mSessionControlsListenerList)
				{
					listener.onSetCameraEnabled(true);
				}
			}
		}
	}

	private void setMicrophoneMuted(boolean shouldMute)
	{
		try
		{
			Log.d(Utils.getOoVooTag(), "Setting microphone mute to: " + shouldMute);
			ConferenceCoreError error = (shouldMute) ? mConferenceCore.turnMicrophoneOff() : mConferenceCore
					.turnMicrophoneOn();
			Log.d(Utils.getOoVooTag(), "Setting microphone mute to: " + shouldMute + " rc = " + error);
		}
		catch (Exception e)
		{
			addAlert("An error occured while calling setMicrophoneMuted " + shouldMute, ConferenceCoreError.Error);
		}
		finally
		{
			if (mSessionControlsListenerList != null)
			{
				for (SessionControlsListener listener : mSessionControlsListenerList)
				{
					listener.onSetMicrophoneEnabled(true);
				}
			}
		}
	}

	private void setSpeakersMuted(boolean shouldMute)
	{
		try
		{
			Log.d(Utils.getOoVooTag(), "Setting speaker mute to: " + shouldMute);
			ConferenceCoreError error = (shouldMute) ? mConferenceCore.turnSpeakerOff() : mConferenceCore
					.turnSpeakerOn();
			Log.d(Utils.getOoVooTag(), "Setting speaker mute to: " + shouldMute + " rc = " + error);
		}
		catch (Exception e)
		{
			addAlert("An error occured while calling setSpeakersMuted " + shouldMute, ConferenceCoreError.Error);
		}
		finally
		{
			if (mSessionControlsListenerList != null)
			{
				for (SessionControlsListener listener : mSessionControlsListenerList)
				{
					listener.onSetSpeakersEnabled(true);
				}
			}
		}
	}

	public void doEndOfCall()
	{
		isUIReadyState = false;
		mConferenceCore.leaveConference(ConferenceCoreError.OK);
		if (mSessionListenerList != null)
		{
			for (SessionListener listener : mSessionListenerList)
			{
				listener.onLeftSession(ConferenceCoreError.OK);
			}
		}
	}

	private void doJoinSession()
	{
		UserSettings settings = retrieveSettings();
		
		settings.Resolution = CameraResolutionLevel.ResolutionMedium;
		persistSettings(settings);
		
		Log.i(TAG, "Reset Camera Resolution Level to " + settings.Resolution);
		
		String conferenceID = settings.SessionID;
		String displayName = settings.DisplayName;
		if (displayName.trim().equals(""))
		{
			if (mSessionListenerList != null)
			{
				for (SessionListener listener : mSessionListenerList)
				{
					listener.onJoinSessionWrongDataError();
				}
			}
			return;
		}
		Log.d(Utils.getOoVooTag(), "trying to join session " + conferenceID);

//		if (!isSdkInitialize)
//		{
//			isSdkInitialize = 
//					(mConferenceCore.initSdk(settings.AppId, settings.AppToken, settings.BaseURL) == ConferenceCoreError.OK);
//		}
		
		ConferenceCoreError error = mConferenceCore.joinConference(conferenceID, LOCAL_PARTICIPANT_ID_DEFAULT, displayName);
		Log.d(Utils.getOoVooTag(), "JoinSession rc = " + error);
		AlertsManager.getInstance().clearAlerts();
		switch (error)
		{
		case OK:
			break;
		case AlreadyInSession:
			if (mSessionListenerList != null)
			{
				for (SessionListener listener : mSessionListenerList)
				{
					listener.onJoinSessionError(error);
				}
			}
			mConferenceCore.leaveConference(ConferenceCoreError.OK);
			break;
			
		case ConferenceIdNotValid:
		case ClientIdNotValid:
		case ServerAddressNotValid:
		default:
			if (mSessionListenerList != null)
			{
				for (SessionListener listener : mSessionListenerList)
				{
					listener.onJoinSessionError(error);
				}
			}
			break;
		}
	}

	private void doLeaveSession()
	{
		ConferenceCoreError rc = mConferenceCore.leaveConference(ConferenceCoreError.OK);
		Log.d(Utils.getOoVooTag(), "Leave session rc = " + rc);
	}

	@Override
	public void OnVideoTransmitTurnedOff(ConferenceCoreError errorCode) {

		addAlert("My video transmit turned off", errorCode);
	}

	@Override
	public void OnVideoTransmitTurnedOn(ConferenceCoreError errorCode) {

		addAlert("My video transmit turned on", errorCode);	
	}
	
	@Override
	public void OnPreviewTurnedOff(ConferenceCoreError errorCode)
	{	
		Log.d(Utils.getOoVooTag(), "received OnCameraUnmuted (JAVA) error = " + errorCode + " isCameraMuted = " + mIsCameraMute);
		
		if (mSessionControlsListenerList != null)
		{
			for (SessionControlsListener listener : mSessionControlsListenerList)
			{
				listener.onSetCameraMuted(mIsCameraMute);
			}
		}
		
		addAlert("My preview turned off", errorCode);	
	}

	@Override
	public void OnCameraTurnedOn(ConferenceCoreError errorCode, FrameSize frameSize, int fps)
	{
		Log.d(Utils.getOoVooTag(), "received OnCameraUnmuted (JAVA) error = " + errorCode);
		
		mIsCameraMute = false;
		
	    mConferenceCore.turnPreviewOn();
		
		if (mSessionControlsListenerList != null)
		{
			for (SessionControlsListener listener : mSessionControlsListenerList)
			{
				listener.onCameraOn();
			}
		}
		
		addAlert("Camera turned on. Video format: width=" + frameSize.Width + " height=" + frameSize.Height +
				" fps=" + fps, errorCode);
	}

	@Override
	public void OnMicrophoneTurnedOff(ConferenceCoreError errorCode, String sMediaDeviceId)
	{
		mIsMicrophoneMute = true;
		if (mSessionControlsListenerList != null)
		{
			for (SessionControlsListener listener : mSessionControlsListenerList)
			{
				listener.onSetMicrophoneMuted(mIsMicrophoneMute);
			}
		}
		addAlert("Microphone Turned Off", errorCode);
	}

	@Override
	public void OnMicrophoneTurnedOn(ConferenceCoreError errorCode, String sMediaDeviceId)
	{
		mIsMicrophoneMute = false;
		if (mSessionControlsListenerList != null)
		{
			for (SessionControlsListener listener : mSessionControlsListenerList)
			{
				listener.onSetMicrophoneMuted(mIsMicrophoneMute);
			}
		}
		addAlert("Microphone Turned On", errorCode);
	}

	@Override
	public void OnSpeakerTurnedOff(ConferenceCoreError errorCode, String sMediaDeviceId)
	{
		mAreSpeakersMute = true;
		if (mSessionControlsListenerList != null)
		{
			for (SessionControlsListener listener : mSessionControlsListenerList)
			{
				listener.onSetSpeakersMuted(mAreSpeakersMute);
			}
		}
		addAlert("Speaker Turned Off", errorCode);
	}

	@Override
	public void OnSpeakerTurnedOn(ConferenceCoreError errorCode, String sMediaDeviceId)
	{
		mAreSpeakersMute = false;
		if (mSessionControlsListenerList != null)
		{
			for (SessionControlsListener listener : mSessionControlsListenerList)
			{
				listener.onSetSpeakersMuted(mAreSpeakersMute);
			}
		}
		addAlert("Speaker Turned On", errorCode);
	}

	@Override
	public void OnLeftConference(ConferenceCoreError errorCode)
	{
		AudioManager mgr = (AudioManager) mApp.getSystemService(Context.AUDIO_SERVICE);

		if( mgr != null)
			mgr.setMode(AudioManager.MODE_NORMAL);

		mParticipantsManager.onLeftSession(errorCode);
		if (mSessionListenerList != null)
		{
			for (SessionListener listener : mSessionListenerList)
			{
				listener.onLeftSession(errorCode);
			}
		}
		addAlert("Left Session", errorCode);
	}

	private void doSwitchUIFullMode(int viewId)
	{
		if( mParticipantsManager.getHolder() != null)
		{
			if (!mParticipantsManager.getHolder().isFullMode())
			{
				mParticipantsManager.getHolder().moveToFullMode(viewId);
				mSessionUIPresenter.onFullModeChanged(viewId);
			}
			else
			{
				mParticipantsManager.getHolder().moveToMultiMode(viewId);
				mSessionUIPresenter.onMultiModeChanged();
			}
		}
	}

	@Override
	public synchronized void OnParticipantJoinedConference(String sParticipantId, String sOpaqueString)
	{
		int participantViewId = -1;
		try
		{
			Log.i(Utils.getOoVooTag(),
					"ConferenceManager.OnParticipantJoinedSession - adding participant to holder " + sOpaqueString
							+ " -> started");
			mParticipantsManager.onParticipantJoinedSession(sParticipantId, sOpaqueString);

			// if (mParticipantsManager.getHolder().getNumOfVideosOn() <
			// ParticipantsManager.MAX_ACTIVE_PARTICIPANTS_IN_CALL) {
			// Log.d(Utils.getOoVooTag(),
			// "ConferenceManager.OnParticipantJoinedSession - turning video on for "
			// + sParticipantId);
			// if (ConferenceCoreError.OK == mConferenceCore
			// .turnParticipantVideoOn(sParticipantId)) {
			// mParticipantsManager.getHolder().setVideoStateOn(
			// sParticipantId, true);
			// participantViewId = mParticipantsManager.getHolder()
			// .getViewIdByParticipant(sParticipantId);
			// Log.d(Utils.getOoVooTag(),
			// "ConferenceManager.OnParticipantJoinedSession - participantViewId = "
			// + participantViewId);
			// }
			// }
		}
		catch (Exception ex)
		{
			Log.e(Utils.getOoVooTag(), "ConferenceManager.OnParticipantJoinedSession - An Exception:", ex);
		}
		finally
		{
			if (mConferenceQueue != null)
				mConferenceQueue.sendMessage(UI_PREPARE_HOLDER_USER, new String[] { sParticipantId, sOpaqueString });

			if (mSessionParticipantsListenerList != null)
			{
				for (SessionParticipantsListener listener : mSessionParticipantsListenerList)
				{
					listener.onParticipantJoinedSession(sParticipantId, participantViewId, sOpaqueString);
				}
			}
			addAlert("joinSession for user: " + sParticipantId + " With Display Name: " + sOpaqueString,
					ConferenceCoreError.OK);
		}
	}

	private void prepareParticipantActiveRender(String sParticipantId, String sOpaqueString)
	{
		int participantViewId = -1;
		try
		{
			Log.i(Utils.getOoVooTag(),
					"ConferenceManager.prepareParticipantActiveRender - adding participant to holder " + sParticipantId
							+ " -> started");
			participantViewId = mParticipantsManager.prepareParticipantAsActiveRender(sParticipantId);

			Log.i(Utils.getOoVooTag(),
					"ConferenceManager.prepareParticipantActiveRender - adding participant to holder "
							+ participantViewId + " <- finished");
			if (participantViewId != -1)
			{
				Log.i(Utils.getOoVooTag(),
						"ConferenceManager.prepareParticipantActiveRender - turning video on for " + sParticipantId);
				if (!mParticipantsManager.getHolder().isFullMode()
						&& ConferenceCoreError.OK == mConferenceCore.receiveParticipantVideoOn(sParticipantId))
				{
					mParticipantsManager.getHolder().setVideoOn(sParticipantId, true);
					Log.i(Utils.getOoVooTag(), "ConferenceManager.OnParticipantJoinedSession - participantViewId = "
							+ participantViewId);
				}
			}
		}
		catch (Exception ex)
		{
			Log.e(Utils.getOoVooTag(), "ConferenceManager.OnParticipantJoinedSession - An Exception:", ex);
		}
		finally
		{
			if (mSessionParticipantsListenerList != null)
			{
				for (SessionParticipantsListener listener : mSessionParticipantsListenerList)
				{
					listener.onParticipantJoinedSession(sParticipantId, participantViewId, sOpaqueString);
				}
			}
		}
	}
	
	private SurfaceView createSurfaceView() {
		if (mSurfaceView == null) {
			mSurfaceView = new SurfaceView(mApp);
			WindowManager.LayoutParams params = new WindowManager.LayoutParams();
			params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
			params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
			params.gravity = Gravity.TOP | Gravity.LEFT;
			params.width = 1;
			params.height = 1;
			WindowManager wmgr = (WindowManager) mApp.getSystemService(Context.WINDOW_SERVICE);
			wmgr.addView(mSurfaceView, params);
		}
		
		return mSurfaceView;
	}

	public void resumePreviewSession() {
		try
		{
			Log.d(Utils.getOoVooTag(), "setting preview video surface");
			SurfaceView view = createSurfaceView();
			if (view == null)
				Log.e(Utils.getOoVooTag(), "Surface is null");
			else
				Log.i(Utils.getOoVooTag(), "Surface is not null");
			mConferenceCore.setPreviewSurface(view);
			mConferenceCore.resume();
		}
		catch (Exception e)
		{
			Log.e(TAG, "resumePreviewSession Error:", e);
		}
	}
	
	public void resumeSession()
	{
		Log.d(Utils.getOoVooTag(), "Resume event from UI - resumeSession");
		try
		{
			Log.d(Utils.getOoVooTag(), "setting preview video surface");
			SurfaceView view = createSurfaceView();
			if (view == null)
				Log.e(Utils.getOoVooTag(), "Surface is null");
			else
				Log.i(Utils.getOoVooTag(), "Surface is not null");
			mConferenceCore.setPreviewSurface(view);
			mConferenceCore.resume();

			MediaDevice dev = mConferenceCore.getMediaDevice(
					DeviceType.Microphone, -1);
			if (dev != null) {
				Log.d(Utils.getOoVooTag(), "Microphone info: " + dev.toString());
				mIsMicrophoneMute = (dev.State != DeviceState.ON);
			}

			dev = mConferenceCore.getMediaDevice(DeviceType.Speaker, -1);
			if (dev != null) {
				Log.d(Utils.getOoVooTag(), "Speaker info: " + dev.toString());
				mAreSpeakersMute = (dev.State != DeviceState.ON);
			}

			dev = mConferenceCore.getMediaDevice(DeviceType.Camera, -1);
			if (dev != null) {
				Log.d(Utils.getOoVooTag(), "Camera info: " + dev.toString());
				// mIsCameraMute = (dev.State != DeviceState.ON);
			}
		}
		catch (Exception e)
		{
			Log.e(TAG, "resumeSession Error:", e);
		}
	}

	public void pauseSession()
	{
		Log.d(Utils.getOoVooTag(), "Pause event from UI - pauseSession");
		try
		{
			mConferenceCore.pause();
		}
		catch (Exception e)
		{
			Log.e(TAG, "", e);
		}
	}

	public synchronized void onResume()
	{
		addAlert("onResume", ConferenceCoreError.OK);
		if( mParticipantsManager.getHolder() != null)
		{
			mParticipantsManager.getHolder().updateGLViews(mSessionUIPresenter);
			mParticipantsManager.getHolder().Resume();

			if (mSessionUIPresenter != null)
				mSessionUIPresenter.initSurfaces();

			Log.d(Utils.getOoVooTag(), "ParticipantsManager.getInstance().getParticipants().size() = "
					+ mParticipantsManager.getParticipants().size());
			for (Participant participant : mParticipantsManager.getParticipants())
			{
				int participantViewId = mParticipantsManager.getHolder().getViewIdByParticipant(participant.getId());
				Log.d(Utils.getOoVooTag(), "onResume - participantViewId =" + participantViewId + " participantId " + participant.getId());
				if (participantViewId != -1)
				{
					if (mSessionUIPresenter != null)
						mSessionUIPresenter.updateParticipantSurface(participantViewId, participant.getDisplayName(),
								participant.getIsVideoOn());
				}
			}
		}
		
		turnCameraOn(true);
	}

	public void onPause()
	{
		Log.d(Utils.getOoVooTag(), "ConferenceManager - onPause");
		mParticipantsManager.getHolder().Pause();
		turnCameraOn(false);
		addAlert("onPause", ConferenceCoreError.OK);
	}

	public void initSession()
	{
		// Select devices
		try
		{
			int numOfVidOn = 0;
			for (Participant participant : mParticipantsManager.getParticipants())
			{
				if (numOfVidOn < ParticipantsManager.MAX_ACTIVE_PARTICIPANTS_IN_CALL)
				{
					Log.d(Utils.getOoVooTag(), "turning ParticipantVideoOn for " + participant.getDisplayName() + "(" + participant.getId() + ")");
					if (participant.getId().isEmpty())
						continue;
					
					if (ConferenceCoreError.OK == mConferenceCore.receiveParticipantVideoOn(participant.getId()))
					{
						Log.d(Utils.getOoVooTag(), "setting VideoStateOn for " + participant.getDisplayName());
						mParticipantsManager.getHolder().setVideoOn(participant.getId(), true);
					}
				}
			}

			setMicrophoneMuted(false);
			setSpeakersMuted(false);
		}
		catch (Exception e)
		{
			Log.e(TAG, "", e);
		}
	}

	public void loadDataFromSettings()
	{
		try
		{
			UserSettings settings = retrieveSettings();
			((ConferenceCore) mConferenceCore).setLogListener(this, settings.CurrentLogLevel);
			
			if(mConferenceCore == null){
				addAlert("set configuration before initialization", ConferenceCoreError.InvalidOperation);
				return;
			}
			
			mConferenceCore.selectCamera(settings.CameraType);
			mConferenceCore.setCameraResolutionLevel( CameraResolutionLevel.ResolutionMedium); //settings.Resolution);
			ConferenceCoreError err = mConferenceCore.selectMicrophone(settings.MicrophoneType);
			if( err == ConferenceCoreError.DeviceNotFound) {
				MediaDevice mic = mConferenceCore.getMediaDevice(DeviceType.Microphone, -1); // search default one
				if( mic != null) {
					err = mConferenceCore.selectMicrophone(mic.getId());
					if( err == ConferenceCoreError.OK) {
						settings.MicrophoneType = mic.getId();
						persistSettings(settings);
					}
				}
			}
			mConferenceCore.selectSpeaker(settings.SpeakersType);
			
		}
		catch (Exception e)
		{
			addAlert("An Error occured while trying to select Devices", ConferenceCoreError.DeviceNotFound);
		}
	}

	public Participant[] getActiveUsers()
	{
		List<Participant> users = mParticipantsManager == null ? null : mParticipantsManager.getParticipants();
		return users == null ? null : users.toArray(new Participant[users.size()]);
	}

	@Override
	public void OnJoinConference(ConferenceCoreError errorCode, String mySessionId)
	{
		if (errorCode == ConferenceCoreError.OK)
		{			
			AudioManager mgr = (AudioManager) mApp.getSystemService(Context.AUDIO_SERVICE);

			if( mgr != null)
			{
				mgr.setMode(AudioManager.MODE_IN_COMMUNICATION);

				Log.d(TAG, "Audio for API level is " + Build.VERSION.SDK_INT);
				
				String frames_prop = "1";
				String sample_rate_prop = "1";
				if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
				{	
					frames_prop = mgr.getProperty( AudioManager.PROPERTY_OUTPUT_FRAMES_PER_BUFFER);
					sample_rate_prop = mgr.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE);
				}
				
				boolean low_prop = mApp.getPackageManager().hasSystemFeature( PackageManager.FEATURE_AUDIO_LOW_LATENCY);
				int nat_rate = AudioTrack.getNativeOutputSampleRate(AudioManager.STREAM_SYSTEM);
				int channelMode = AudioFormat.CHANNEL_IN_MONO;
				int encodingMode = AudioFormat.ENCODING_PCM_16BIT;
				int bufferSize = AudioTrack.getMinBufferSize(nat_rate, channelMode, encodingMode);

				int rnat_rate = 44100;
				int rchannelMode = AudioFormat.CHANNEL_IN_MONO;
				int rencodingMode = AudioFormat.ENCODING_PCM_16BIT;
				int rbufferSize = AudioRecord.getMinBufferSize(rnat_rate, rchannelMode, rencodingMode);

				Log.d(TAG, "AudioPlayer Frames per buffer: " + frames_prop + ", sample rate: " + sample_rate_prop + 
						", low latency: " + low_prop);
				Log.d(TAG, "Audio Player   (rate, channel, encoding, bufSize): " + nat_rate + ", " + channelMode + ", " + encodingMode + ", " + bufferSize);
				Log.d(TAG, "Audio Recorder (rate, channel, encoding, bufSize): " + rnat_rate + ", " + rchannelMode + ", " + rencodingMode + ", " + rbufferSize);
				Log.d(TAG, "Audio duration: " + Integer.parseInt(frames_prop) * 1000 / Integer.parseInt(sample_rate_prop) );
				
			}
			addAlert("Joined Session myId = " + mySessionId, errorCode);
		}
		
		if (mSessionListenerList != null)
		{
			for (SessionListener listener : mSessionListenerList)
			{
				if (errorCode == ConferenceCoreError.OK)
					listener.onJoinSessionSucceeded();
				else
					listener.onJoinSessionError(errorCode);
			}
			addAlert("Failed to join: ", errorCode);
		}
		
		addAlert("Joined Session myId = " + mySessionId, errorCode);
	}

	@Override
	public void OnConferenceError(ConferenceCoreError errorCode)
	{
		Log.d(Utils.getOoVooTag(), "OnSessionError - recieved error:" + errorCode);
		if (mSessionListenerList != null)
		{
			for (SessionListener listener : mSessionListenerList)
			{
				listener.onSessionError(errorCode);
			}
		}
		addAlert("Session Error: ", errorCode);
	}

	@Override
	public void OnParticipantVideoReceiveOn(ConferenceCoreError errorCode, String sParticipantId, FrameSize frameSize)
	{
		int participantViewId = -1;
		String displayName = "";
		
		try
		{
			Log.i(Utils.getOoVooTag(), "ConferenceManager.OnParticipantVideoReceiveOn : " + sParticipantId + "; "
					+ frameSize.toString() + " {" + errorCode + "}");

			VideoChannelPtr in = mConferenceCore.getVideoChannelForUser(sParticipantId);
			if (in.isValid())
			{
				ParticipantHolder pholder = mParticipantsManager.getHolder();
				if (pholder != null && pholder.turnVideoOn(sParticipantId, in))
				{
					addAlert(sParticipantId + " turned video On", errorCode);
					participantViewId = mParticipantsManager.getHolder().getViewIdByParticipant(sParticipantId);
					Participant participant = mParticipantsManager.getParticipant(sParticipantId);
					
					if (participant != null)
						displayName = participant.getDisplayName();			
				}
				else {
					addAlert(sParticipantId + " turned video Off", errorCode);
					errorCode = ConferenceCoreError.InvalidPointer;
				}
			}
			else {
				Log.e(TAG, "No video channel found for user: " + sParticipantId);
				errorCode = ConferenceCoreError.ClientIdNotValid;
			}
		}
		catch (Exception ex)
		{
			Log.e(Utils.getOoVooTag(),
					"ConferenceManager.OnParticipantVideoReceiveOn - an Exception occured:", ex);
			errorCode = ConferenceCoreError.Error;
		}

		if (mSessionParticipantsListenerList != null)
		{
			for (SessionParticipantsListener listener : mSessionParticipantsListenerList)
			{
				listener.onParticipantVideoTurnedOn(errorCode, sParticipantId, frameSize,
						participantViewId, displayName);
			}
		}

		addAlert("Participant (" + displayName + ") Video Turned On. Video format: width="
				+ frameSize.Width + " height=" + frameSize.Height, errorCode);
	}
	
	@Override
	public void OnParticipantVideoReceiveOff(ConferenceCoreError errorCode, String sParticipantId)
	{
		addAlert(sParticipantId + " turned video Off", errorCode);
		int participantViewId = mParticipantsManager.getHolder().getViewIdByParticipant(sParticipantId);
		if (mSessionParticipantsListenerList != null)
		{
			for (SessionParticipantsListener listener : mSessionParticipantsListenerList)
			{
				listener.onParticipantVideoTurnedOff(errorCode, participantViewId, sParticipantId);
			}
		}
		mParticipantsManager.getHolder().turnVideoOff(sParticipantId);

		String displayName = "";
		Participant participant = mParticipantsManager.getParticipant(sParticipantId);
		if (participant != null)
			displayName = participant.getDisplayName();
		addAlert("Participant: (" + displayName + ") Video Turned Off", errorCode);
	}

	@Override
	public void OnParticipantVideoPaused(String sParticipantId)
	{
		addAlert(sParticipantId + " video Paused", ConferenceCoreError.OK);
		int participantViewId = mParticipantsManager.getHolder().getViewIdByParticipant(sParticipantId);
		if (mSessionParticipantsListenerList != null)
		{
			for (SessionParticipantsListener listener : mSessionParticipantsListenerList)
			{
				listener.onParticipantVideoPaused(participantViewId);
			}
		}
	}

	@Override
	public void OnParticipantVideoResumed(String sParticipantId)
	{
		addAlert(sParticipantId + " video Resumed", ConferenceCoreError.OK);
		int participantViewId = mParticipantsManager.getHolder().getViewIdByParticipant(sParticipantId);

		if (mSessionParticipantsListenerList != null)
		{
			for (SessionParticipantsListener listener : mSessionParticipantsListenerList)
			{
				listener.onParticipantVideoResumed(participantViewId, sParticipantId);
			}
		}
	}

	@Override
	public void OnGetMediaDeviceList(MediaDevice[] aDeviceArray)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void OnCameraSelected(ConferenceCoreError errorCode, String sPreviousDeviceId, String sNewDeviceId)
	{
		addAlert("Camera switched from: " + sPreviousDeviceId + " to: " + sNewDeviceId, errorCode);
	}

	@Override
	public void OnMicrophoneSelected(ConferenceCoreError errorCode, String sPreviousDeviceId, String sNewDeviceId)
	{
		addAlert("Micropone switched from:" + sPreviousDeviceId + " to:" + sNewDeviceId, errorCode);
	}

	@Override
	public void OnSpeakerSelected(ConferenceCoreError errorCode, String sPreviousDeviceId, String sNewDeviceId)
	{
		addAlert("Speaker switched from:" + sPreviousDeviceId + " to:" + sNewDeviceId, errorCode);
	}

	@Override
	public void OnParticipantLeftConference(String sParticipantId)
	{
		int participantViewId = mParticipantsManager.getHolder().getViewIdByParticipant(sParticipantId);

		String displayName = "";
		Participant participant = mParticipantsManager.getParticipant(sParticipantId);
		if (participant != null)
			displayName = participant.getDisplayName();
		addAlert("Participant: (" + displayName + ") Left the session", ConferenceCoreError.OK);

		boolean isUpdateFullMode = mParticipantsManager.onParticipantLeftSession(sParticipantId);
		if (mSessionParticipantsListenerList != null)
		{
			for (SessionParticipantsListener listener : mSessionParticipantsListenerList)
			{
				listener.onParticipantLeftSession(participantViewId, sParticipantId);
			}
		}
		if (isUpdateFullMode)
		{
			// mParticipantsManager.getHolder().turnVideoOnOnMoveToMultiMode(participantViewId);
			mSessionUIPresenter.onMultiModeChanged();
		}
	}

	@Override
	public void OnConnectionStatisticsUpdate(ConnectionStatistics connectionStatistics)
	{
		DecimalFormat df = new DecimalFormat("#.##");
		Log.d(Utils.getOoVooTag(),
				"Connection Statistics Update. InboundBandwidth=" + connectionStatistics.InboundBandwidth * 8 / 1024
						+ "Kbps InboundPacketLoss=" + df.format(connectionStatistics.InboundPacketLoss)
						+ "% OutboundPacketLoss=" + connectionStatistics.OutboundPacketLoss + "%");
		addAlert("Connection Statistics Update. InboundBandwidth=" + connectionStatistics.InboundBandwidth * 8 / 1024
				+ "Kbps InboundPacketLoss=" + connectionStatistics.InboundPacketLoss + "% OutboundPacketLoss="
				+ connectionStatistics.OutboundPacketLoss + "%", ConferenceCoreError.OK);
	}

	public String getSDKVersion()
	{
		return (mConferenceCore == null) ? "" : mConferenceCore.getSDKVersion();
	}

	// Commit the settings to the shared preferences file
	public void persistSettings(UserSettings toPersist)
	{
		mSettingsManager.persistSettings(toPersist);
	}

	// Retrieve the settings from the shared preferences file
	public UserSettings retrieveSettings()
	{
		return mSettingsManager.retrieveSettings();
	}

	// Gets the available cameras
	public List<MediaDeviceWrapper> getCameras()
	{
		return GetDeviceList(DeviceType.Camera);
	}

	// Gets the available microphones
	public List<MediaDeviceWrapper> getMicrohones()
	{
		return GetDeviceList(DeviceType.Microphone);
	}

	// Gets the available speakers
	public List<MediaDeviceWrapper> getSpeakers()
	{
		return GetDeviceList(DeviceType.Speaker);
	}

	private List<MediaDeviceWrapper> GetDeviceList(DeviceType type)
	{
		List<MediaDeviceWrapper> devices = new ArrayList<MediaDeviceWrapper>();
		Vector<MediaDevice> mediaDevices = (mConferenceCore == null ? null : mConferenceCore.getMediaDeviceList(type));
		if (mediaDevices != null)
		{
			for (MediaDevice mediaDevice : mediaDevices)
			{
				devices.add(new MediaDeviceWrapper(mediaDevice.getId(), mediaDevice.getDisplayName()));
			}
			Log.d(Utils.getOoVooTag(), "GetDeviceList: " + devices.toString());
		}
		return devices;
	}

	public boolean isVideoRenderActive(int viewId)
	{
		return mParticipantsManager != null && mParticipantsManager.getHolder() != null
				&& mParticipantsManager.getHolder().isRenderActive(viewId);
	}

	public void destroy()
	{
		try
		{
			mConferenceCore.removeListener();
			if (mConferenceQueue != null)
			{
				mConferenceQueue.end();
				mConferenceQueue.destroy();
				mConferenceQueue = null;
			}
			if (mSettingsManager != null)
			{
				mSettingsManager.destroy();
				mSettingsManager = null;
			}
			if (mParticipantsManager != null)
			{
				mParticipantsManager.destroy();
				mParticipantsManager = null;
			}
			if (mSessionListenerList != null)
			{
				mSessionListenerList.clear();
			}
			mSessionListenerList = null;

			if (mSessionParticipantsListenerList != null)
			{
				mSessionParticipantsListenerList.clear();
			}
			mSessionParticipantsListenerList = null;

			if (mSessionControlsListenerList != null)
			{
				mSessionControlsListenerList.clear();
			}
			mSessionControlsListenerList = null;

			if (mFileLogger != null) {
				mFileLogger.Stop();
			}
			mFileLogger = null;
			
			mSessionUIPresenter = null;
			mConferenceCore = null;
			instance = null;
			mApp = null;			
		}
		catch (Exception ex)
		{
			Log.e(TAG, "", ex);
		}
	}

	public static interface SessionListener
	{

		public void onJoinSessionError(ConferenceCoreError error);

		public void onJoinSessionWrongDataError();

		public void onJoinSessionSucceeded();

		public void onSessionIDGenerated(String sSessionId);

		public void onSessionError(ConferenceCoreError error);

		public void onLeftSession(ConferenceCoreError error);
		
		public void onSessionInited();
	}

	public static interface SessionParticipantsListener
	{

		public void onParticipantVideoTurnedOn(ConferenceCoreError errorCode, String sParticipantId,
                                               FrameSize frameSize, int participantViewId, String displayName);

		public void onParticipantVideoPaused(int participantViewId);

		public void onParticipantVideoResumed(int participantViewId, String sParticipantId);

		public void onParticipantVideoTurnedOff(ConferenceCoreError errorCode, int participantViewId,
                                                String sParticipantId);

		public void onParticipantJoinedSession(String sParticipantId, int participantViewId, String sOpaqueString);

		public void onParticipantLeftSession(int participantViewId, String sParticipantId);
	}

	public static interface SessionControlsListener
	{
		public void onCameraOn();
		
		public void onSetCameraMuted(boolean isMuted);

		public void onSetMicrophoneMuted(boolean isMuted);

		public void onSetSpeakersMuted(boolean isMuted);

		public void onSetCameraEnabled(boolean isEnabled);

		public void onSetMicrophoneEnabled(boolean isEnabled);

		public void onSetSpeakersEnabled(boolean isEnabled);
	}
	
	public static interface ParticipantSwitchListener
	{
		public void onParticipantVideoTurned(String participantId, boolean isOn);
	}
	
	public void setParticipantSwitchListener(ParticipantSwitchListener listener) {
		mParticipantSwitchListener = listener;
	}

	public ParticipantsManager getParticipantsManager()
	{
		return mParticipantsManager;
	}

	void addAlert(final String alert, final ConferenceCoreError errorCode)
	{
		String a = new String();
		// prints the alert or the error code if it is not OK
		if (errorCode != ConferenceCoreError.OK)
		{
			a = Utils.getCurrentMethodName(1) + 
					" recieved error: " + errorCode + " on ";
		}
	
		a += alert;
		AlertsManager.getInstance().addAlert(alert);
	}

	@Override
	public void OnIncallMessage(byte[] buffer, String participantId) {
		addAlert("Message from: " + participantId + " - " + buffer, ConferenceCoreError.OK);
		
		String participantName = mParticipantsManager.getParticipant(participantId).getDisplayName();
		MessengerController.getInstance().receiveText(buffer, participantName);
	}

	public VideoFilterData[] getAvailableFilters() {
		return mConferenceCore.getAvailableFilters();
	}

	public void setActiveFilter(String id) {
		addAlert("Set Active Filter to " + id, ConferenceCoreError.OK);
		if (isUIReadyState) {
			mConferenceCore.setActiveFilter(id);
		}
	}
	
	public String getActiveFilter() {
		if( mConferenceCore == null)
			mConferenceCore = ConferenceCore.instance(mApp);
		
		String id = mConferenceCore.getActiveFilter();
		addAlert("Get Active Filter to " + id, ConferenceCoreError.OK);
		return id;
	}

	public boolean inCallMessagesPermitted() {
		return mConferenceCore.inCallMessagesPermitted();
	}

	public CameraResolutionLevel getCameraResolutionLevel() {
		CameraResolutionLevel level = CameraResolutionLevel.ResolutionMedium;
		
		try {
			IMediaDeviceInformation devInfo = mConferenceCore.getMediaDeviceInfo(DeviceType.Camera, mConferenceCore.getActiveVideoDevice());
			if (devInfo != null)
			{
				String res_level = devInfo.getProperty(ConferenceCore.RESOLUTION_LEVEL);
				if (!res_level.isEmpty())
					level = CameraResolutionLevel.valueOf(res_level);
			}
		} catch (DeviceNotSelectedException e) {
		}

		return level;
	}

	public String getErrorMessageForConferenceCoreError( ConferenceCoreError error) {
		if( mConferenceCore == null)
			mConferenceCore = ConferenceCore.instance(mApp);
		
		return mConferenceCore.getErrorMessageForConferenceCoreError(error);
	}

	/**
	 * Fired on Incoming phone Call ON/OFF
	 * 
	 * @param reason
	 */
	public void OnHold(String reason)
	{
		addAlert( "On hold by reason: " + reason, ConferenceCoreError.OK);
	}

	public void OnUnHold(String reason)
	{
		addAlert( "On hold done. reason: " + reason, ConferenceCoreError.OK);
	}
	
	public void OnLog(LogLevel level, String tag, String message)
	{
		mFileLogger.OnLog(level, tag, message);
	}

	public boolean isMicMuted() {
		return mIsMicrophoneMute;
	}

	public boolean isSpeakerMuted() {
		return mAreSpeakersMute;
	}

	@Override
	public void OnCameraTurnedOff(ConferenceCoreError errorCode) {
		LogSdk.d(TAG, "OnCameraTurneedOff rc = " + errorCode);
		if (mSessionControlsListenerList != null)
		{
			for (SessionControlsListener listener : mSessionControlsListenerList)
			{
				listener.onSetCameraMuted(mIsCameraMute);
			}
		}
		addAlert("Camera turned off.", errorCode);
	}

	@Override
	public void OnPreviewTurnedOn(ConferenceCoreError errorCode) {
		LogSdk.d(TAG, "OnPreviewTurnedOn rc = " + errorCode);
		
		String sParticipantId = "";
		
		if( errorCode == ConferenceCoreError.OK) 
		{
			VideoChannelPtr in = ConferenceCore.instance().getVideoChannelForUser( sParticipantId);
			if( in == null) {
				Log.w(TAG, "The incoming channel for participant " + sParticipantId + " is NULL!!!");
				return;
			}
			
			if ( mParticipantsManager.getHolder() != null && mParticipantsManager.getHolder().turnVideoOn( sParticipantId, in))
				Log.d(TAG, "OnMyVideoTurnedOn: video is ON for " + sParticipantId );
		} 
		
		Log.d(Utils.getOoVooTag(), "received OnCameraUnmuted (JAVA) error = " + errorCode + " isCameraMuted = " + mIsCameraMute);
		
		if (mSessionControlsListenerList != null)
		{
			for (SessionControlsListener listener : mSessionControlsListenerList)
			{
				listener.onSetCameraMuted(mIsCameraMute);
			}
		}
		
		addAlert("My preview turned on. ", errorCode);
	}

	public boolean isSdkInitialized() {
		return isSdkInitialize;
	}
}

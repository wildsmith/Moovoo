//
// UserSettingsManager.java
// 
// Created by ooVoo on July 22, 2013
//
// © 2013 ooVoo, LLC.  Used under license. 
//
package com.ooVoo.oovoosample.Settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.util.Log;

import com.ooVoo.oovoosample.Common.Utils;
import com.ooVoo.oovoosample.R;
import com.ooVoo.oovoosample.UserEmailFetcher;
import com.oovoo.core.IConferenceCore;
import com.oovoo.core.IConferenceCore.CameraResolutionLevel;
import com.oovoo.core.IConferenceCore.LogLevel;

// Manages saving & loading of the UserSettings class
public class UserSettingsManager 
{
	private static final String BASE_BE_URL_DEFAULT = "https://api-sdk.oovoo.com";
	private static UserSettings mSettings;
	public Context mContext;
	
	public UserSettingsManager(Context context)
	{
		mContext = context;
	}

	// Retrieves user settings
	public UserSettings retrieveSettings()
	{ 
		if (mSettings == null)
		{
			Log.d(Utils.getOoVooTag(), "Reading user settings from repository...");
			String AppId = null;
			String AppToken = null;
			try 
			{
				ApplicationInfo ai;
				ai = mContext.getPackageManager().getApplicationInfo(mContext.getPackageName(), PackageManager.GET_META_DATA);
				Bundle bundle = ai.metaData;
				AppId = bundle.getString(IConferenceCore.AppIdProp);
				AppToken = bundle.getString( IConferenceCore.AppTokenProp);
				Log.d(Utils.getOoVooTag(), "Retrieved App meta-data settings: AppId = " + AppId + " AppToken = " + AppToken);
			} catch (NameNotFoundException e) 
			{
				Log.e(Utils.getOoVooTag(), "Error retrieving configuration!");
				e.printStackTrace();
			}
		    mSettings = new UserSettings();
			SharedPreferences sharedPref = mContext.getSharedPreferences(mContext.getResources().getString(R.string.ooVooUserSettings), Context.MODE_PRIVATE);
			mSettings.BaseURL = sharedPref.getString(mContext.getResources().getString(R.string.base_url_settings_field), "");
			mSettings.SessionID = sharedPref.getString(mContext.getResources().getString(R.string.session_id_settings_field), "");
            mSettings.AppId = "12349983352266";
            mSettings.AppToken =
                    "MDAxMDAxAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAADy1MGwu0PJ9EYNLx7sguQgjwE6rUVvvOk%2B2arxNQZjXsIh6dlZXQLR75idHeGCLKqWzQBXaaE8BuxxLzzUtGarXmkUgn0aRhUwTgGBoDc3fl3ZCFDT3yAqpu05%2F%2Fiplx4%3D";
            mSettings.CameraType = sharedPref.getInt(mContext.getResources().getString(R.string.camera_type_settings_field), 1);
			mSettings.MicrophoneType = sharedPref.getInt(mContext.getResources().getString(R.string.microphone_type_settings_field), 1);
			mSettings.SpeakersType = sharedPref.getInt(mContext.getResources().getString(R.string.speakers_type_settings_field), 2);
			mSettings.UserID=UserEmailFetcher.getEmail(mContext);
			mSettings.DisplayName=UserEmailFetcher.getEmail(mContext).split("@")[0];
			String res=sharedPref.getString(mContext.getResources().getString(R.string.resolution), CameraResolutionLevel.ResolutionMedium.toString());
			mSettings.Resolution=CameraResolutionLevel.valueOf(res);
			mSettings.CurrentLogLevel = LogLevel.fromString(sharedPref.getString(mContext.getResources().getString(R.string.log_level), LogLevel.Debug.toString()));

			if(mSettings.BaseURL.equals(""))
			{
				mSettings.BaseURL = BASE_BE_URL_DEFAULT;
			}
			if(mSettings.SessionID.equals(""))
			{
				mSettings.SessionID = "TEST_SESSION_123";
			}
			if(mSettings.CurrentLogLevel == LogLevel.None )
			{
				mSettings.CurrentLogLevel = LogLevel.Debug;
			}
		}		
		Log.d(Utils.getOoVooTag(), "Retrieved user settings: " + mSettings);
		return mSettings.Clone();
	}

	// Commits user settings
	public void persistSettings(UserSettings toPersist) 
	{	
		// Check if need to update
		if (!toPersist.equals(mSettings))
		{
            mSettings = toPersist;
            Log.d(Utils.getOoVooTag(), "Persisting user settings: " + toPersist);
            SharedPreferences sharedPref = mContext.getSharedPreferences(mContext.getResources().getString(R.string.ooVooUserSettings), Context.MODE_PRIVATE);
            SharedPreferences.Editor prefEditor = sharedPref.edit();
            prefEditor.putString(mContext.getResources().getString(R.string.base_url_settings_field), toPersist.BaseURL);
            prefEditor.putString(mContext.getResources().getString(R.string.session_id_settings_field), toPersist.SessionID);
            prefEditor.putString(mContext.getResources().getString(R.string.appIdPersisted), toPersist.AppId);
            prefEditor.putString(mContext.getResources().getString(R.string.appTokenPersisted), toPersist.AppToken);
            prefEditor.putString(mContext.getResources().getString(R.string.usrID), toPersist.UserID);
            prefEditor.putString(mContext.getResources().getString(R.string.displayName), toPersist.DisplayName);
            prefEditor.putInt(mContext.getResources().getString(R.string.camera_type_settings_field), toPersist.CameraType);
            prefEditor.putInt(mContext.getResources().getString(R.string.microphone_type_settings_field), toPersist.MicrophoneType);
            prefEditor.putInt(mContext.getResources().getString(R.string.speakers_type_settings_field), toPersist.SpeakersType);
            //prefEditor.putString(mContext.getResources().getString(R.string.resolution), toPersist.Resolution.toString());
            prefEditor.putString(mContext.getResources().getString(R.string.log_level), toPersist.CurrentLogLevel.toString());
            prefEditor.commit();
        }
    }

	public void destroy() {
		mSettings = null;
		mContext = null;
	}	
}
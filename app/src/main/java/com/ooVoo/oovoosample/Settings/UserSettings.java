package com.ooVoo.oovoosample.Settings;

import com.oovoo.core.IConferenceCore.CameraResolutionLevel;
import com.oovoo.core.IConferenceCore.LogLevel;


// Represents a user settings instance, filled by the Settings module
public class UserSettings
{
	public String BaseURL;
	public String SessionID;
	public String AppId;
	public String AppToken;
	public String UserID;
	public String DisplayName;
	public int CameraType;
	public int MicrophoneType;
	public int SpeakersType;
	public CameraResolutionLevel Resolution;
	public LogLevel CurrentLogLevel = LogLevel.Debug;
	@Override
	public boolean equals(Object o) 
	{
		// Check is same instance	
		if (this == o) 
		{
		  return true;		
		}
		
		// Return false if the other object has the wrong type.
		if (!(o instanceof UserSettings)) 
		{
		  return false;
		}

		// Check equality 
		UserSettings toCompare = (UserSettings) o;	

		return (BaseURL.equals(toCompare.BaseURL)) &&	
				(SessionID.equals(toCompare.SessionID)) &&
				(UserID.equals(toCompare.UserID)) &&
				(DisplayName.equals(toCompare.DisplayName))&&
			   (AppId.equals(toCompare.AppId)) &&
			   (AppToken.equals(toCompare.AppToken)) &&
			   (CameraType == toCompare.CameraType) &&
			   (MicrophoneType == toCompare.MicrophoneType) &&
			   (SpeakersType == toCompare.SpeakersType)&&
			   (Resolution==toCompare.Resolution)&&
			   (CurrentLogLevel==toCompare.CurrentLogLevel);
	}
	
	@Override
	public String toString() 
	{
		String description = "Base URL = " + BaseURL + System.getProperty("line.separator");
		description += "Session ID = " + SessionID + System.getProperty("line.separator");
		description += "User ID = " + UserID + System.getProperty("line.separator");
		description += "Display Name = " + DisplayName + System.getProperty("line.separator");
		description += "App Id = " + AppId + System.getProperty("line.separator");
		description += "App Token = " + AppToken + System.getProperty("line.separator");
		description += "Camera Type = " + CameraType + System.getProperty("line.separator");
		description += "Microphone Type = " + MicrophoneType + System.getProperty("line.separator");
		description += "Speakers Type = " + SpeakersType + System.getProperty("line.separator");
		description += "Resolution = " + Resolution + System.getProperty("line.separator");
		description += "Log Level = " + CurrentLogLevel + System.getProperty("line.separator");
		return description;
	}
	
	@Override
	public int hashCode() 
	{
		throw new UnsupportedOperationException();
	}
	
	public UserSettings Clone()
	{
		UserSettings clone= new UserSettings();
		clone.BaseURL=this.BaseURL;
		clone.AppId=this.AppId;
		clone.AppToken=this.AppToken;
		clone.CameraType=this.CameraType;
		clone.MicrophoneType=this.MicrophoneType;
		clone.SessionID=this.SessionID;
		clone.SpeakersType=this.SpeakersType;
		clone.UserID=this.UserID;
		clone.Resolution=this.Resolution;
		clone.DisplayName=this.DisplayName;
		clone.CurrentLogLevel=this.CurrentLogLevel;
		return clone;
		
	}
	
}	

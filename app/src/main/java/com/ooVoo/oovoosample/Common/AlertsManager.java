//
// AlertsManager.java
// 
// Created by ooVoo on July 22, 2013
//
// © 2013 ooVoo, LLC.  Used under license. 
//
package com.ooVoo.oovoosample.Common;

import com.oovoo.core.Utils.LogSdk;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.util.Log;
public class AlertsManager {
	private static final String TAG = AlertsManager.class.getSimpleName();
	
	List<String> mAlerts = new ArrayList<String>();
	private Set<IAlertsListener> mListeners = new HashSet<IAlertsListener>();
	static AlertsManager mInstance;
	private SimpleDateFormat mFormat = new SimpleDateFormat("HH:mm:ss");
	
	public static AlertsManager getInstance()
	{
		if (mInstance==null) 
		{ 
			mInstance = new AlertsManager();			
		}
		return mInstance;
	}
	
	protected AlertsManager(){}

	public synchronized void addAlert(String text)
	{
		String timeStamp = mFormat.format(Calendar.getInstance().getTime());
		String alert = timeStamp + " " + text; 
		
		Log.d(TAG,"addAlert - adding message: " + alert);
		
		mAlerts.add( alert);
		
		for (IAlertsListener listener : mListeners) 
		{
			Log.d(TAG,"addAlert - notifying listener:"+listener.getClass().getName()+"| message:"+alert);
			listener.OnAlert(text);
		}
	}	
	
	public synchronized List<String> GetAlerts()
	{
		Log.d(TAG,"GetAlerts");
		
		return mAlerts;
	}
	
	public void addListener(IAlertsListener listener) {
		mListeners.add(listener);
		Log.d(TAG,"addListener - listener:"+listener.getClass().getName());
	}
	
	public void removeListener(IAlertsListener listener) {
		mListeners.remove(listener); 
		Log.d(TAG,"IAlertsListener - listener:"+listener.getClass().getName());
	}
	
	public synchronized void clearAlerts()
	{
		mAlerts.clear();
	}

	public synchronized int getCount() {
		return (mAlerts == null ? 0 : mAlerts.size());
	}

	public synchronized String getItem(int i) {
		return (mAlerts == null ? null : mAlerts.get(i));
	}
}

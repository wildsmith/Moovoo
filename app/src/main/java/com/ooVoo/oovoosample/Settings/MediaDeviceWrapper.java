//
// MediaDeviceWrapper.java
// 
// Created by ooVoo on July 22, 2013
//
// © 2013 ooVoo, LLC.  Used under license. 
//
package com.ooVoo.oovoosample.Settings;

// Wrapper for a media device ID & description
public class MediaDeviceWrapper
{
	private int mId;
	private String mDescription;
	
	public MediaDeviceWrapper(int id, String description)
	{
		mId = id;
		mDescription = description;
	}
	
	public MediaDeviceWrapper(int id)
	{
		this(id, "");
	}
	
	// Returns description, used for displaying in Spinner
	@Override
	public String toString() 
	{			
		return mDescription;
	}
	
	// Retrieve device id
	public int getDeviceId()
	{
		return mId;
	}
	
	// Check for equality of IDs
	@Override
	public boolean equals(Object o) 
	{
		// Check is same instance	
		if (this == o) 
		{
		  return true;
		}
		
		// Return false if the other object has the wrong type.
		if (!(o instanceof MediaDeviceWrapper)) 
		{
		  return false;
		}
		
		// Check equality
		MediaDeviceWrapper toCompare = (MediaDeviceWrapper) o;		
		return (mId == toCompare.getDeviceId());
	}
	
	@Override
	public int hashCode() 
	{
		throw new UnsupportedOperationException();
	}
}
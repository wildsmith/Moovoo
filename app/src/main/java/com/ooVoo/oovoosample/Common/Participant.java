//
// Participant.java
// 
// Created by ooVoo on July 22, 2013
//
// © 2013 ooVoo, LLC.  Used under license. 
//
package com.ooVoo.oovoosample.Common;

public class Participant {

	String _id;
	String _displayName;
	boolean _isVideoOn;
	
	Participant(String id,String displayName,boolean isVideoOn) {
			_id=id;
			_isVideoOn=isVideoOn;
			_displayName=displayName;
	}
	public String getId()
	{ 
		return _id;
	}
	
	public String getDisplayName()
	{
		return _displayName;
	}
	public boolean getIsVideoOn()
	{
		return _isVideoOn;				
	}
	public void setVideoState(boolean isOn)
	{
		_isVideoOn=isOn;
	}
	
	@Override
	public boolean equals(Object o) {
		if (super.equals(o)) return true;
		
		if (!(o instanceof Participant)) {
			return false;
		}
		
		Participant other= (Participant)o;
		return other._id==_id;
	}
}

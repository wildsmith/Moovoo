//
// ResolutionWrapper.java
// 
// Created by ooVoo on July 22, 2013
//
// © 2013 ooVoo, LLC.  Used under license. 
//
package com.ooVoo.oovoosample.VideoCall;

import com.oovoo.core.IConferenceCore.CameraResolutionLevel;

public class ResolutionWrapper {
	public CameraResolutionLevel Level;
	public String FriendlyName;
	
	public ResolutionWrapper(CameraResolutionLevel level, String friendlyName)
	{
		Level=level;
		FriendlyName=friendlyName;
	}
	
	@Override
	public String toString() {
		return FriendlyName;
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
			if (!(o instanceof ResolutionWrapper)) 
			{
			  return false;
			}
			
			// Check equality
			ResolutionWrapper toCompare = (ResolutionWrapper) o;		
			return (Level == toCompare.Level);
		}
}

//
// ParticipantsManager.java
// 
// Created by ooVoo on July 22, 2013
//
// © 2013 ooVoo, LLC.  Used under license. 
//
package com.ooVoo.oovoosample.Common;


import android.util.Log;

import com.ooVoo.oovoosample.Common.ParticipantHolder.VideoParticipant;
import com.oovoo.core.ConferenceCore.FrameSize;
import com.oovoo.core.IConferenceCore.ConferenceCoreError;
import com.oovoo.core.Utils.LogSdk;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class ParticipantsManager{

	private ParticipantHolder _pholder = null;
	public static final int MAX_ACTIVE_PARTICIPANTS_IN_CALL = 3;
	
	public ParticipantsManager( ) {
		_pholder = new ParticipantHolder();
	}


	public void onParticipantJoinedSession(String sParticipantId, String sOpaqueString) {
		try
		{
			_pholder.addParticipant(sParticipantId, sOpaqueString);
		}
		catch(Exception ex)
		{			
			Log.e(Utils.getOoVooTag(),"OnParticipantJoinedSession - error while adding participant",ex);
		}
	}
	
	public int prepareParticipantAsActiveRender(String sParticipantId){
		return _pholder.prepareParticipantAsActiveRender(sParticipantId);
	}

	public void OnParticipantVideoTurnedOn(ConferenceCoreError eErrorCode, String sParticipantId, FrameSize frameSize) {

		if (eErrorCode==ConferenceCoreError.OK) {
		
		}
	}


	public void OnParticipantVideoTurnedOff(ConferenceCoreError eErrorCode, String sParticipantId) {

		if (eErrorCode==ConferenceCoreError.OK) {
			
		}
	}

	public boolean onParticipantLeftSession(String sParticipantId) {
		return _pholder.removeParticipant(sParticipantId);
	}


	public Participant getParticipant(String sParticiapntId)
	{
		Participant retParticipant=null;
		VideoParticipant participant=_pholder.getParticipant(sParticiapntId);
		if (participant!=null) {
			retParticipant= new Participant(participant.getParticipantId(), participant.getOpaqueString(), _pholder.isVideoOn(participant.getParticipantId()));
		}
		return retParticipant;
	}

	public List<Participant> getParticipants()
	{
		Collection<VideoParticipant> participants = _pholder.getParticipants();
		List<Participant> plist = new ArrayList<Participant>();
		
		for( VideoParticipant participant: participants) {
			plist.add( new Participant(participant.getParticipantId(), participant.getOpaqueString(), _pholder.isVideoOn(participant.getParticipantId())));
		}
		Collections.sort(plist, mParticipantComparator);
		return plist;
	}

	public int getNoOfVideosOn()
	{		
		return _pholder.getNumOfVideosOn();
	}
	
	public String findRenderIdByViewId(int view_id){
		return _pholder.findRenderIdByViewId(view_id);
	}


	public void onLeftSession(ConferenceCoreError eErrorCode) {
		_pholder.clear();
	}

	public ParticipantHolder getHolder() {
		return _pholder;
	}


	public void destroy() {
		if(_pholder != null)
			_pholder.clear();
		_pholder = null;
	}
	
	private Comparator<Participant> mParticipantComparator = new DefaultParticipantComparator();

	private static class DefaultParticipantComparator implements Comparator<Participant> {

		@Override
		public int compare(Participant lhs, Participant rhs) {						
			return (lhs.getDisplayName().compareTo(rhs.getDisplayName()));
		}
		
	}


}

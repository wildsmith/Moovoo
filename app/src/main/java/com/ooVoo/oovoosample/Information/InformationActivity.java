//
// InformationActivity.java
// 
// Created by ooVoo on July 22, 2013
//
// © 2013 ooVoo, LLC.  Used under license. 
//
package com.ooVoo.oovoosample.Information;

import android.app.ActionBar;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import com.ooVoo.oovoosample.Common.Participant;
import com.ooVoo.oovoosample.Common.ParticipantsManager;
import com.ooVoo.oovoosample.Common.Utils;
import com.ooVoo.oovoosample.ConferenceManager;
import com.ooVoo.oovoosample.ConferenceManager.SessionParticipantsListener;
import com.ooVoo.oovoosample.R;
import com.oovoo.core.ConferenceCore.FrameSize;
import com.oovoo.core.IConferenceCore.ConferenceCoreError;
import com.oovoo.core.Utils.LogSdk;

// Information presenter entity
public class InformationActivity extends Activity implements SessionParticipantsListener
{
	private CustomParticipantAdapter mCustomAdapter;
	private ListView mActiveUsersList;
	private TextView mSessionIdView = null;	 
	private ConferenceManager mConferenceManager = null;
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{		
		super.onCreate(savedInstanceState);
		setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		initView();
	}
	
	private void initView(){
		// Set layout
		setContentView(R.layout.information);
		mConferenceManager = ConferenceManager.getInstance(getApplicationContext());	
		mSessionIdView = (TextView) findViewById(R.id.sessionIdInformationValueLabel);		
		mActiveUsersList = (ListView) findViewById(R.id.activeUsersList);
		mActiveUsersList.setDivider(null);
		mActiveUsersList.setDividerHeight(0);
		mCustomAdapter = new CustomParticipantAdapter(mConferenceManager.getActiveUsers());
		
		ActionBar ab = getActionBar();
		if(ab != null){
			ab.setHomeButtonEnabled(true);
			ab.setTitle(R.string.information_screen_name);
			ab.setHomeButtonEnabled(true);
			ab.setDisplayShowTitleEnabled(true);
			ab.setDisplayShowHomeEnabled(true);
			ab.setDisplayHomeAsUpEnabled(true);
			ab.setDisplayUseLogoEnabled(false);
			ab.setIcon(R.drawable.menu_ic_info);
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item == null)
			return false;

		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onResume() 
	{
		super.onResume();
		mConferenceManager.addSessionParticipantsListener(this);
		mSessionIdView.setText(mConferenceManager.retrieveSettings().SessionID);
		
		// Get active users list
		mCustomAdapter.updateParticipants(mConferenceManager.getActiveUsers());		
		mActiveUsersList.setAdapter(mCustomAdapter);
	}

	private void refershList() {	
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if(mCustomAdapter != null){
					mCustomAdapter.updateParticipants(mConferenceManager.getActiveUsers());
					mCustomAdapter.notifyDataSetChanged();
				}
			}
		});		
	}
	
	private void updateDataInList() {	
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if(mCustomAdapter != null){
					mCustomAdapter.updateParticipants(mConferenceManager.getActiveUsers());
					mCustomAdapter.notifyDataSetChanged();
				}
			}
		});		
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		mActiveUsersList.setAdapter(null);
		mConferenceManager.removeSessionParticipantsListener(this);
	}
	
	@Override
	public void onParticipantVideoTurnedOn(ConferenceCoreError eErrorCode, String sParticipantId, FrameSize frameSize, int participantViewId, String displayName) {
		refershList();
	}
	
	@Override
	public void onParticipantVideoTurnedOff(ConferenceCoreError eErrorCode, int participantViewId, String sParticipantId) {
		refershList();
	}
	
	@Override
	public void onParticipantJoinedSession(String sParticipantId, int participantViewId, String sOpaqueString) {
		updateDataInList();
	}
	
	@Override
	public void onParticipantLeftSession(int participantViewId, String sParticipantId){
		updateDataInList();
	}

	@Override
	public void onParticipantVideoPaused(int participantViewId) {		
	}

	@Override
	public void onParticipantVideoResumed(int participantViewId, String sParticipantId) {		
	}
	
	private void switchParticipantVideoOn(Participant participant){
		Log.d(Utils.getOoVooTag(), "turning video on for " + participant.getDisplayName());
		mConferenceManager.turnParticipantVideoOn(participant.getId());		
	}

	private void switchParticipantVideoOff(Participant participant){
		Log.d(Utils.getOoVooTag(), "turning video off for " + participant.getId());
		mConferenceManager.turnParticipantVideoOff(participant.getId(), participant.getDisplayName());		
	}
	
	private class CustomParticipantAdapter extends BaseAdapter implements OnCheckedChangeListener{

		Participant[] listArray;

		public CustomParticipantAdapter(Participant[] participants) {
			listArray = participants;
		}
		
		public void updateParticipants(Participant[] participants){
			listArray = participants;
		}

		@Override
		public int getCount() {
			return (listArray == null)? 0 : listArray.length; // total number of elements in the list
		}

		@Override
		public Object getItem(int i) {
			return (listArray == null)? null : listArray[i]; // single item in the list
		}

		@Override
		public long getItemId(int i) {
			return i; // index number
		}

		@Override
		public View getView(int index, View view, final ViewGroup parent) {

			ParticipantViewHolder viewHolder = null;
			if (view == null) {
				LayoutInflater inflater = LayoutInflater.from(parent.getContext());
				view = inflater.inflate(R.layout.participantinfo, parent, false);
				viewHolder = new ParticipantViewHolder();
				viewHolder.textView = (TextView) view.findViewById(R.id.participantId);
				viewHolder.button = (Switch) view.findViewById(R.id.switch1);
				view.setTag(viewHolder);			
			}
			else{
				viewHolder = (ParticipantViewHolder)view.getTag();
			}

			final Participant participant = listArray[index];			
			viewHolder.textView.setText(participant.getDisplayName());		
			viewHolder.button.setTag(participant);
			viewHolder.button.setOnCheckedChangeListener(null);//detaching the handler so it wont raise when we set the inital value
			viewHolder.button.setChecked(participant.getIsVideoOn());
			viewHolder.button.setOnCheckedChangeListener(this);
			viewHolder.button.setEnabled(true);
			
			return view;
		}
		
		private class ParticipantViewHolder{
			Switch button;
			TextView textView;
		}

		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			Switch the_switch = (Switch) buttonView;
			Participant participant = (Participant)buttonView.getTag();
			if (the_switch.isChecked()) {
				ParticipantsManager manager = ConferenceManager.getInstance(InformationActivity.this.getApplicationContext()).getParticipantsManager();
				if (manager.getNoOfVideosOn() < ParticipantsManager.MAX_ACTIVE_PARTICIPANTS_IN_CALL) {
					switchParticipantVideoOn(participant); 
				} 
				else {
					the_switch.setChecked(false);						
				}
			} else {				
				switchParticipantVideoOff(participant);				
			}
		}
	}
	
}

//
// AlertsActivity.java
// 
// Created by ooVoo on July 22, 2013
//
// © 2013 ooVoo, LLC.  Used under license. 
//
package com.ooVoo.oovoosample.Alerts;

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
import android.widget.ListView;
import android.widget.TextView;

import com.ooVoo.oovoosample.Common.AlertsManager;
import com.ooVoo.oovoosample.Common.IAlertsListener;
import com.ooVoo.oovoosample.R;
import com.oovoo.core.Utils.LogSdk;

public class AlertsActivity extends Activity implements IAlertsListener {

	private static final String TAG = AlertsActivity.class.getSimpleName();
	private AlertsAdapter mAlertsAdapter = new AlertsAdapter();;
	private ListView mAlertsListView = null;

	@Override
	public void onDestroy() {
		super.onDestroy();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate started");	
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		mAlertsAdapter.notifyDataSetChanged();
		Log.d(TAG, "onCreate done");	
	}
	
	private void initView(){
		setContentView(R.layout.alerts);
		mAlertsListView = (ListView) findViewById(R.id.alertsList);
		mAlertsListView.setDivider(null);
		mAlertsListView.setDividerHeight(0);
		mAlertsListView.setItemsCanFocus(false);
		if( mAlertsListView.getAdapter() == null)
			mAlertsListView.setAdapter(mAlertsAdapter);
		mAlertsListView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
		mAlertsListView.setStackFromBottom(true);

		ActionBar ab = getActionBar();
		if(ab != null){
			ab.setHomeButtonEnabled(true);
			ab.setTitle(R.string.alerts);
			ab.setHomeButtonEnabled(true);
			ab.setDisplayShowTitleEnabled(true);
			ab.setDisplayShowHomeEnabled(true);
			ab.setDisplayHomeAsUpEnabled(true);
			ab.setDisplayUseLogoEnabled(false);
			ab.setIcon(R.drawable.menu_ic_alerts);
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
	protected void onResume() {
		super.onResume();
		
		mAlertsAdapter.notifyDataSetChanged();
		initView();
		
		Log.d(TAG, "onResume - registering to AlertsManager events");
		AlertsManager.getInstance().addListener(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.d(TAG, "onPause - de-registering to AlertsManager events");
		mAlertsAdapter.notifyDataSetInvalidated();
		AlertsManager.getInstance().removeListener(this);
	}

	@Override
	public void OnAlert(final String alert) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Log.d(TAG, "OnAlert - recieved alert:"+alert);
				mAlertsAdapter.notifyDataSetChanged();
			}
		});		
	}

	private class AlertsAdapter extends BaseAdapter{

		public AlertsAdapter() {
			super();
		}

		@Override
		public  int getCount() {
			return AlertsManager.getInstance().getCount();
		}

		@Override
		public  String getItem(int i) {
			return AlertsManager.getInstance().getItem(i);
		}
		
		@Override
		public  long getItemId(int i) {
			return i; // index number
		}

		@Override
		public  View getView(int index, View view, final ViewGroup parent) {
			AlertViewHolder viewHolder = null;
			try {
				if (view == null) {
					LayoutInflater inflater = LayoutInflater.from(parent.getContext());
					view = inflater.inflate(R.layout.alert_row_view, parent, false);
					viewHolder = new AlertViewHolder();
					viewHolder.textView = (TextView) view.findViewById(R.id.alert_row);
					view.setTag(viewHolder);			
				}
				else{
					viewHolder = (AlertViewHolder)view.getTag();
				}
				String alert = getItem(index);			
				viewHolder.textView.setText(alert);
			} catch( IllegalStateException ex) {
				Log.e(TAG, "getView", ex);
				//ex.printStackTrace();
			}
			return view;
		}
		
		private class AlertViewHolder{
			TextView textView;
		}
	}
}

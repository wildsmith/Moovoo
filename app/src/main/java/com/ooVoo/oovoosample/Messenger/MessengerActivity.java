package com.ooVoo.oovoosample.Messenger;

import android.app.ActionBar;
import android.app.ListActivity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.ooVoo.oovoosample.Common.Utils;
import com.ooVoo.oovoosample.ConferenceManager;
import com.ooVoo.oovoosample.R;
import com.oovoo.core.Utils.LogSdk;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class MessengerActivity extends ListActivity implements IMessengerListener {
	
	private ArrayList<Message> mMessages;
	private MessengerAdapter mAdapter;
	private EditText mTextMessage;
	private ConferenceManager mConferenceManager;
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);	
		setContentView(R.layout.messenger);
		
		mConferenceManager = ConferenceManager.getInstance(getApplicationContext());
		
		MessengerController.getInstance().setListener(this);
		
		mTextMessage = (EditText) this.findViewById(R.id.text);
		
		mMessages = MessengerController.getInstance().getMessages();

		mAdapter = new MessengerAdapter(this, mMessages);
		setListAdapter(mAdapter);
		
		setSelection(mAdapter.getCount()-1);
		
		ActionBar ab = getActionBar();
		if (ab != null) {
			ab.setHomeButtonEnabled(true);
			ab.setTitle(R.string.messenger_screen_name);
			ab.setHomeButtonEnabled(true);
			ab.setDisplayShowTitleEnabled(true);
			ab.setDisplayShowHomeEnabled(true);
			ab.setDisplayHomeAsUpEnabled(true);
			ab.setDisplayUseLogoEnabled(false);
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
	
	public void sendMessage(View v)
	{
		String newMessage = mTextMessage.getText().toString().trim(); 
		if (newMessage.length() > 0) {
			mTextMessage.setText("");
			addNewMessage(new Message(newMessage, true));
			
			try {
				MessengerController.getInstance().sendText(newMessage.getBytes("UTF-8"), new String());
			} catch (UnsupportedEncodingException e) {
				Log.d(Utils.getOoVooTag(), "Unsupported encoding.");
			}
		}
	}
	
	void addNewMessage(Message m)
	{
		mMessages.add(m);
		mAdapter.notifyDataSetChanged();
		getListView().setSelection(mMessages.size()-1);
	}

	@Override
	public void onTextReceived(final byte[] buffer, final String participantName) {
		
		runOnUiThread(new Runnable() {
		    @Override
		    public void run() {
		    	addNewMessage(new Message(new String(buffer), participantName, false));
		    }
		});
	}
}

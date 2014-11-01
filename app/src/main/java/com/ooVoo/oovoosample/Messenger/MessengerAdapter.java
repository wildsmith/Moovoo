package com.ooVoo.oovoosample.Messenger;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.ooVoo.oovoosample.R;

import java.util.ArrayList;

public class MessengerAdapter extends BaseAdapter {

	private Context mContext;
	private ArrayList<Message> mMessages;
	
	public MessengerAdapter(Context context, ArrayList<Message> messages) {
		super();
		mContext = context;
		mMessages = messages;
	}
	
	@Override
	public int getCount() {
		return mMessages.size();
	}

	@Override
	public Object getItem(int position) {
		return mMessages.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Message message = (Message) this.getItem(position);

		ViewHolder holder; 
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = LayoutInflater.from(mContext).inflate(R.layout.sms_row, parent, false);
			holder.messageTimestamp = (TextView) convertView.findViewById(R.id.message_timestamp);
			holder.messageText = (TextView) convertView.findViewById(R.id.message_text);
			holder.messageLabel = (TextView) convertView.findViewById(R.id.message_label);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		holder.messageTimestamp.setText(message.getTimestamp());
		holder.messageText.setText(message.getMessage());
		holder.messageLabel.setText(message.getOwner());
		
		LayoutParams lp = (LayoutParams) holder.messageText.getLayoutParams();

		if (message.isMine()) {
			holder.messageText.setBackgroundResource(R.drawable.speech_bubble_green);
			lp.gravity = Gravity.RIGHT;
		} else {
			holder.messageText.setBackgroundResource(R.drawable.speech_bubble_orange);
			lp.gravity = Gravity.LEFT;
		}
		holder.messageText.setLayoutParams(lp);
		holder.messageText.setTextColor(mContext.getResources().getColor(R.color.text_color));	
		holder.messageLabel.setLayoutParams(lp);

		return convertView;
	}
	
	private static class ViewHolder
	{
		TextView messageTimestamp;
		TextView messageText;
		TextView messageLabel;
	}
}

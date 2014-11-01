//
// ParticipantVideoSurface.java
// 
// Created by ooVoo on July 22, 2013
//
// © 2013 ooVoo, LLC.  Used under license. 
//
package com.ooVoo.oovoosample.Common;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.ooVoo.oovoosample.R;
import com.ooVoo.oovoosample.VideoCall.DynamicAbsoluteLayout;

public class ParticipantVideoSurface extends FrameLayout {
	
	public enum States {
		STATE_NONE,
		STATE_EMPTY,
		STATE_PAUSED,
	    STATE_AVATAR,
	    STATE_VIDEO
	}
	
	public ImageView avatar;
	public GLSurfaceView mVideoView;
	public TextView nameBox;
	public TextView mVideoInfo;
	private States mState = States.STATE_NONE;
	
	public ParticipantVideoSurface(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
	}

	public ParticipantVideoSurface(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ParticipantVideoSurface(Context context) {
		super(context);
	}

	
	
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		mVideoInfo = (TextView)findViewById(R.id.video_info);
		
	}

	public void hideSurface() {
		mVideoView.setVisibility(GLSurfaceView.INVISIBLE);
	}
	
	public void showSurface() {
		mVideoView.setVisibility(GLSurfaceView.VISIBLE);
	}
	
	public void showAvatar() {
		avatar.setVisibility(ImageView.VISIBLE);
		mVideoView.setVisibility(GLSurfaceView.INVISIBLE);
		nameBox.setVisibility(TextView.VISIBLE);
	}
	
	public void showUserStatusInfo(){
		mVideoInfo.setVisibility(View.VISIBLE);
	}

	public void hideUserStatusInfo(){
		mVideoInfo.setVisibility(View.GONE);
	}
	
	public void showEmptyCell() {
		if (avatar.getId() != R.id.myAvatar)  avatar.setVisibility(ImageView.INVISIBLE);
		if (mVideoView.getId() != R.id.myVideoSurface) mVideoView.setVisibility(GLSurfaceView.INVISIBLE);
		nameBox.setVisibility(GLSurfaceView.INVISIBLE);
		mVideoInfo.setVisibility(View.GONE);
	}

	public void showVideo() {
		if (avatar.getId() != R.id.myAvatar) avatar.setVisibility(ImageView.INVISIBLE);
		if (mVideoView.getId() != R.id.myVideoSurface) mVideoView.setVisibility(GLSurfaceView.VISIBLE);
		if (mVideoInfo != null) mVideoInfo.setVisibility(View.GONE);
		if (nameBox != null) nameBox.setVisibility(TextView.VISIBLE);
	}

	public void setName(String sParticipantId) {
		final String nameToShow = sParticipantId;
		nameBox.setVisibility(nameToShow.equals("") ? TextView.INVISIBLE : TextView.VISIBLE);
		nameBox.setText(nameToShow);
	}

	public void moveTo(float toXDelta, float toYDelta,
			float toWidth, float toHeight) {
		DynamicAbsoluteLayout.LayoutParams view_params = (DynamicAbsoluteLayout.LayoutParams) getLayoutParams();
		view_params.x = (int) toXDelta;
		view_params.y = (int) toYDelta;
		view_params.width = (int) toWidth;
		view_params.height = (int) toHeight;
		setLayoutParams(view_params);
	}
	
	public void setState(States state) {
		mState = state;
	}

	public States getState() {
		return mState;
	}
	
	public void update() {
		
		switch (mState) {
			case STATE_EMPTY:
				showEmptyCell();
				break;
				
			case STATE_AVATAR:
				showAvatar();
				break;
				
			case STATE_VIDEO:
				showVideo();
				break;
	
			default:
				break;
		}
	}
}

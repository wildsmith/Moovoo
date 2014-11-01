//
// VCParticipantsController.java
// 
// Created by ooVoo on July 22, 2013
//
// © 2013 ooVoo, LLC.  Used under license. 
//
package com.ooVoo.oovoosample.VideoCall;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

import com.ooVoo.oovoosample.Common.ParticipantHolder;
import com.ooVoo.oovoosample.Common.ParticipantVideoSurface;

/**
 * Represents the layer contains and layouts the video call surfaces
 *
 * @author Anna Kandel
 *
 */

public class VCParticipantsController extends DynamicAbsoluteLayout{

    private static final String TAG = VCParticipantsController.class.getName();

    public static final short 			MULTI_MODE 					= 0;
    public static final short 			FULL_MODE 					= 1;

    private Context						mContext					= null;
    private ParticipantHolder			mAdapter					= null;
    private DisplayMetrics				mDisplayMetrics				= new DisplayMetrics();
    private int							mWidth						= 0;
    private int							mHeight						= 0;
    private float						mDensity					= 0;
    private short 						mCurrentUIMode 				= MULTI_MODE;

    public interface VCContentListener {
        public void onAddFriendsToSession();
    }

    public VCParticipantsController(Context context) {
        super(context);
        initView(context);
    }

    public VCParticipantsController(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public VCParticipantsController(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView(context);
    }

    public void onContentUpdate() {
        if (mAdapter == null)
            return;
        onResize();
    }

    public void onModeUpdated(short mode){
        if(mCurrentUIMode != mode){
            mCurrentUIMode = mode;
            onResize();
        }
    }

    /**
     * Initializes basic GUI components and listeners.
     *
     * @param context
     *            Interface to application's global information
     */
    private void initView(Context context) {
        try {
            mContext = context;
            mAdapter = ((VideoCallActivity) mContext).getParticipantHolder();
        } catch (Exception ex) {
            Log.e(TAG, "initView", ex);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        try {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            if (mWidth == 0 || mHeight == 0) {
                mWidth = measuredWidth;
                mHeight = measuredHeight;
                if (mDensity < 0) {
                    ((Activity) mContext).getWindowManager().getDefaultDisplay().getMetrics(mDisplayMetrics);
                    mDensity = mDisplayMetrics.density;
                }
                onResize();
            }
        } catch (Exception ex) {
            Log.e(TAG, "", ex);
        }
    }

    public int getVisibleSurfacesCount() {
        int countActiveSurfaces = 0;
        try {
            int numViews = getChildCount();
            for (int i = 0; i < numViews; i++) {
                View v = getChildAt(i);
                if (v instanceof ParticipantVideoSurface && v.getVisibility() == View.VISIBLE) {
                    countActiveSurfaces++;
                }
            }
        } catch (Exception ex) {
            Log.e(TAG, "", ex);
        }
        return countActiveSurfaces;
    }

    public void onResize() {
        try {
            int mChildCount = getChildCount();
            if (mDensity <= 0) {
                ((Activity) mContext).getWindowManager().getDefaultDisplay().getMetrics(mDisplayMetrics);
                mWidth = mDisplayMetrics.widthPixels;
                mHeight = mDisplayMetrics.heightPixels;
                mDensity = mDisplayMetrics.density;
            }
            int pos = 0;
            int xPos = 0;
            for (int i = 0; i < mChildCount; i++) {
                View v = getChildAt(i);
                xPos = mWidth * pos;
                if (v instanceof ParticipantVideoSurface && v.getVisibility() == View.VISIBLE) {
                    ((ParticipantVideoSurface) v).moveTo(xPos, 0, mWidth, mHeight);
                    pos++;
                }
            }

        } catch (Exception ex) {
            Log.e(TAG, "", ex);
        }
    }

}

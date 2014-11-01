//
// DynamicAbsoluteLayout.java
// 
// Created by ooVoo on July 22, 2013
//
// © 2013 ooVoo, LLC.  Used under license. 
//
package com.ooVoo.oovoosample.VideoCall;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.ooVoo.oovoosample.R;
import com.oovoo.core.Utils.LogSdk;

/**
 * A layout that lets you specify exact locations (x/y coordinates) of its
 * children.
 * @author Anna Kandel
 */

public class DynamicAbsoluteLayout extends ViewGroup {

	protected int measuredWidth = 0;
	protected int measuredHeight = 0;

	public DynamicAbsoluteLayout(Context context) {
		super(context);
	}

	public DynamicAbsoluteLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public DynamicAbsoluteLayout(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		try {
			int count = getChildCount();
			
			int maxHeight = 0;
			int maxWidth = 0;

			// Find out how big everyone wants to be
			measureChildren(widthMeasureSpec, heightMeasureSpec);

			// Find rightmost and bottom-most child
			for (int i = 0; i < count; i++) {
				View child = getChildAt(i);
				if (child.getVisibility() != GONE) {
					int childRight;
					int childBottom;

					LayoutParams lp = (LayoutParams) child
							.getLayoutParams();

					childRight = lp.x + child.getMeasuredWidth();
					childBottom = lp.y + child.getMeasuredHeight();

					maxWidth = Math.max(maxWidth, childRight);
					maxHeight = Math.max(maxHeight, childBottom);
				}
			}

			// Account for padding too
			maxWidth += getPaddingLeft() + getPaddingRight();
			maxHeight += getPaddingTop() + getPaddingBottom();

			// Check against minimum height and width
			maxHeight = Math.max(maxHeight, getSuggestedMinimumHeight());
			maxWidth = Math.max(maxWidth, getSuggestedMinimumWidth());
			measuredWidth = resolveSize(maxWidth, widthMeasureSpec);
			measuredHeight = resolveSize(maxHeight, heightMeasureSpec);
			setMeasuredDimension(measuredWidth, measuredHeight);
		} catch (Exception ex) {
			Log.e("","",ex);
		}
	}

	/**
	 * Returns a set of layout parameters with a width of
	 * {@link android.view.ViewGroup.LayoutParams#WRAP_CONTENT}, a height of
	 * {@link android.view.ViewGroup.LayoutParams#WRAP_CONTENT} and with the
	 * coordinates (0, 0).
	 */
	@Override
	protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
		return new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT, 0, 0);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		try {
			int count = getChildCount();
			for (int i = 0; i < count; i++) {
				View child = getChildAt(i);
				if (child.getVisibility() != GONE) {

					LayoutParams lp = (LayoutParams) child
							.getLayoutParams();

					int childLeft = getPaddingLeft() + lp.x;
					int childTop = getPaddingTop() + lp.y;
					child.layout(childLeft, childTop, childLeft
							+ child.getMeasuredWidth(), childTop
							+ child.getMeasuredHeight());

				}
			}
		} catch (Exception ex) {
			Log.e("","",ex);
		}
	}

	@Override
	public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
		return new LayoutParams(getContext(), attrs);
	}

	// Override to allow type-checking of LayoutParams.
	@Override
	protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
		return p instanceof LayoutParams;
	}

	@Override
	protected ViewGroup.LayoutParams generateLayoutParams(
			ViewGroup.LayoutParams p) {
		return new LayoutParams(p);
	}

	/**
	 * Per-child layout information associated with DynamicAbsoluteLayout. See
	 * {@link android.R.styleable#AbsoluteLayout_Layout Absolute Layout
	 * Attributes} for a list of all child view attributes that this class
	 * supports.
	 */
	public static class LayoutParams extends ViewGroup.LayoutParams {
		/**
		 * The horizontal, or X, location of the child within the view group.
		 */
		public int x;
		/**
		 * The vertical, or Y, location of the child within the view group.
		 */
		public int y;

		/**
		 * Creates a new set of layout parameters with the specified width,
		 * height and location.
		 * 
		 * @param width
		 *            the width, either {@link #FILL_PARENT},
		 *            {@link #WRAP_CONTENT} or a fixed size in pixels
		 * @param height
		 *            the height, either {@link #FILL_PARENT},
		 *            {@link #WRAP_CONTENT} or a fixed size in pixels
		 * @param x
		 *            the X location of the child
		 * @param y
		 *            the Y location of the child
		 */
		public LayoutParams(int width, int height, int x, int y) {
			super(width, height);
			this.x = x;
			this.y = y;
		}

		/**
		 * Creates a new set of layout parameters. The values are extracted from
		 * the supplied attributes set and context. The XML attributes mapped to
		 * this set of layout parameters are:
		 * 
		 * <ul>
		 * <li><code>layout_x</code>: the X location of the child</li>
		 * <li><code>layout_y</code>: the Y location of the child</li>
		 * <li>All the XML attributes from
		 * {@link android.view.ViewGroup.LayoutParams}</li>
		 * </ul>
		 * 
		 * @param c
		 *            the application environment
		 * @param attrs
		 *            the set of attributes fom which to extract the layout
		 *            parameters values
		 */
		public LayoutParams(Context c, AttributeSet attrs) {
			super(c, attrs);
			TypedArray a = c.obtainStyledAttributes(attrs,
					R.styleable.DynamicAbsoluteLayout_Layout);
			x = a.getDimensionPixelOffset(
					R.styleable.DynamicAbsoluteLayout_Layout_layout_x, 0);
			y = a.getDimensionPixelOffset(
					R.styleable.DynamicAbsoluteLayout_Layout_layout_y, 0);
			a.recycle();
		}

		/**
		 * {@inheritDoc}
		 */
		public LayoutParams(ViewGroup.LayoutParams source) {
			super(source);
		}
	}
}

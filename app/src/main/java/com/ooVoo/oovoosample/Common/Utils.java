//
// Utils.java
// 
// Created by ooVoo on July 22, 2013
//
// © 2013 ooVoo, LLC.  Used under license. 
//
package com.ooVoo.oovoosample.Common;

import android.app.AlertDialog;
import android.content.Context;
import android.view.Gravity;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.ooVoo.oovoosample.R;
import com.oovoo.core.Utils.MethodUtils;

import java.util.List;

public class Utils {	
		
	// Retrieve the ooVoo tag for log prints
	public static String getOoVooTag()
	{
		return OoVooSampleApplication.getOoVooSampleResources().getString(R.string.ooVooTag);
	}
	
	public static String getCurrentMethodName(int i)  
	{
		return MethodUtils.getCallingMethodName();
	}
	
	public static void printCurrentMethodNameToLog() 
	{
		MethodUtils.printCurrentMethodNameToLog();
	}
	
	// Sets the requested spinner's value
	public static <T> void setSelectedSpinnerValue(Spinner spinner, T valueToSet) {
		ArrayAdapter<T> adapter = (ArrayAdapter<T>) spinner.getAdapter();
		int 	spinnerPosition = adapter.getPosition(valueToSet);
		spinner.	setSelection(spinnerPosition);
	}	

	// Gets the requested spinner's value
	public static <T>  T getSelectedSpinnerValue(Spinner spinner) {
		return ((T) spinner.getSelectedItem());
	}

	// Sets the available spinner's values
	public static <T> void setSpinnerValues(Context context,Spinner spinner,
			List<T> values) {
		ArrayAdapter<T> adapter;
		adapter = new ArrayAdapter<T>(context,
				android.R.layout.simple_spinner_item, values);
		spinner.setAdapter(adapter);
	}
	
	public static void ShowMessageBox(Context context,String title,String msg)
	{
		AlertDialog.Builder popupBuilder = new AlertDialog.Builder(context);
		TextView myMsg = new TextView(context);
		myMsg.setText(msg);
		myMsg.setGravity(Gravity.CENTER_HORIZONTAL);
		popupBuilder.setTitle(title);
		popupBuilder.setPositiveButton("OK", null);
		popupBuilder.setView(myMsg);
		
		popupBuilder.show();
	
	}
}

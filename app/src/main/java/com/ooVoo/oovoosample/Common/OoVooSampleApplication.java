//
// OoVooSampleApplication.java
// 
// Created by ooVoo on July 22, 2013
//
// © 2013 ooVoo, LLC.  Used under license. 
//
package com.ooVoo.oovoosample.Common;

import android.app.Application;
import android.content.res.Resources;

public class OoVooSampleApplication extends Application 
{
	 private static Application mInstance;

	 @Override
	 public void onCreate() 
	 {
		 super.onCreate();
		 mInstance = this;
	 }
	 
	 protected static Resources getOoVooSampleResources() 
	 {
		 return mInstance.getResources();	  
	 }
}

package com.ooVoo.oovoosample;

import java.lang.Thread;
import java.util.concurrent.SynchronousQueue;
import java.util.Calendar;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;

import android.util.Log;

import com.oovoo.core.ILoggerListener;
import com.oovoo.core.IConferenceCore.LogLevel;
import com.oovoo.core.Utils.AppUtils;

public class FileLogger implements ILoggerListener, Runnable {

	private static final String TAG = FileLogger.class.getName();
	private SynchronousQueue<String> mLogQueue = new SynchronousQueue<String>();
	private SimpleDateFormat mDateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	private FileOutputStream mFileOutputStream = null;
	private Thread mThread = null;

	public FileLogger()
	{
		mThread = new Thread(this);

		Start();
	}

	public void Start()
	{
		if (mThread != null && !mThread.isAlive()) {
			mThread.start();
		}
	}

	public void Stop()
	{
		try {
			mLogQueue.put("");
		} catch (InterruptedException ex) {
			Log.e(TAG, "Put to sync log queue failed: " + ex.toString());
		}

		if (mThread != null) {
			try {
				mThread.join();
				mThread = null;
			} catch (InterruptedException ex) {
				Log.e(TAG, "Thread join failed: " + ex.toString());
			}
		}
	}

	@Override
	public void run() {
		try {
			while (true) {
				String logMsg = mLogQueue.take();

				if (logMsg.isEmpty()) {
					break;
				}

				writeLogMsgToFile(logMsg);
			}
		} catch (InterruptedException ex) {
			Log.e(TAG, "Writing log string failed: " + ex.toString());
		}

		try {
			mFileOutputStream.flush();
			mFileOutputStream.close();
			mFileOutputStream = null;
		} catch (IOException ex) {
			Log.e(TAG, "Error during flush or close file output stream: " + ex.toString());
		}
	}

	public void OnLog(LogLevel level, String tag, String message)
	{
		switch(level)
		{
		case Fatal:
			logFatal(level, tag, message);
			break;

		case Error:
			logError(level, tag, message);
			break;

		case Warning:
			logWarning(level, tag, message);
			break;

		case Info:
			logInfo(level, tag, message);
			break;

		case Trace:
			logTrace(level, tag, message);
			break;

		case Debug:
		default:
			logDebug(level, tag, message);
		}
	}

	protected void openLogFile()
	{
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy_HH.mm.ss");

		
		String sLogDirPath = AppUtils.getAppLogDir();
		//String fileName = "ooVooSampleLogFile_" + dateFormat.format(Calendar.getInstance().getTime()) + "_" + timeFormat.format(Calendar.getInstance().getTime()) + ".txt";
		String fileName = AppUtils.getAppName() + "_" + dateFormat.format(Calendar.getInstance().getTime()) + ".txt";

		try {
			mFileOutputStream = new FileOutputStream(new File(sLogDirPath + "/" + fileName), true);
		}
		catch (FileNotFoundException ex) {
			Log.e(TAG, "File <" + fileName + "> not found exception caught: " + ex.toString());
		}
		catch (Exception ex) {
			Log.e(TAG, "Open file output stream failed: " + ex.toString());
		}
	}

	protected void writeLogMsgToFile(String message)
	{
		try {
			if (mFileOutputStream == null)
			{
				openLogFile();
			}

			mFileOutputStream.write(message.getBytes());
			mFileOutputStream.flush();
		}
		catch (IOException ex) {
			Log.e(TAG, "File output stream IOException: " + ex.toString());
		}
		catch (Exception ex) {
			Log.e(TAG, "File output stream error: " + ex.toString());
		}
	}

	protected void addLogMsgToQueue(String message)
	{
		String currentTimeString = mDateTimeFormat.format(Calendar.getInstance().getTime());

		try {
			mLogQueue.put(currentTimeString + " " + message);
		}
		catch (InterruptedException ex) {
			Log.e(TAG, "Put into SynchronousQueue failed:" + ex.toString());
		}
	}

	protected void logFatal(LogLevel level, String tag, String message)
	{
		addLogMsgToQueue("[" + level.toString() + "] " + message + "\n");
		Log.wtf(tag, "[" + level.toString() + "] " + message);
	}
	protected void logError(LogLevel level, String tag, String message)
	{
		addLogMsgToQueue("[" + level.toString() + "] " + message + "\n");
		Log.e(tag, "[" + level.toString() + "] " + message);
	}

	protected void logWarning(LogLevel level, String tag, String message)
	{
		addLogMsgToQueue("[" + level.toString() + "] " + message + "\n");
		Log.w(tag, "[" + level.toString() + "] " + message);
	}

	protected void logInfo(LogLevel level, String tag, String message)
	{
		addLogMsgToQueue("[" + level.toString() + "] " + message + "\n");
		Log.i(tag, "[" + level.toString() + "] " + message);
	}

	protected void logTrace(LogLevel level, String tag, String message)
	{
		addLogMsgToQueue("[" + level.toString() + "] " + message + "\n");
		Log.v(tag, "[" + level.toString() + "] " + message);
	}

	protected void logDebug(LogLevel level, String tag, String message)
	{
		addLogMsgToQueue("[" + level.toString() + "] " + message + "\n");
		Log.d(tag, "[" + level.toString() + "] " + message);
	}
}

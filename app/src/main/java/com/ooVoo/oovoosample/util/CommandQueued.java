//
// CommandQueued.java
// 
// Created by ooVoo on July 22, 2013
//
// © 2013 ooVoo, LLC.  Used under license. 
//
package com.ooVoo.oovoosample.util;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.util.ArrayList;


public class CommandQueued extends Thread {
	private final String TAG = CommandQueued.class.getName();
	public static boolean LOG_ENABLED = true;
	/***
	 * Command events handler
	 */
	public Handler commandHandler = null;
	protected static final byte OK = 0;
	protected static final byte ERR = 1;
	private boolean isPreExit = false;
	private boolean isQueueStarted = false;
	private ArrayList<String> commands_in_use = null ; 
	/***
	 * Create an instance of CommandQueued
	 * 
	 * @param queueName
	 *            name
	 * @throws Exception
	 */
	public CommandQueued(String queueName) throws Exception {
		this(queueName, true);
	}

	public CommandQueued(String queueName, boolean startImedatly) throws Exception {
		super(queueName);
		commands_in_use = new ArrayList<String>();
		log("Create an new instance of command name [" + getName() + "], starting ...");
		if (startImedatly) {
			start();
			int timeout = 0;

			while (commandHandler == null && timeout < 1000) {
				timeout++;
				Thread.sleep(1);
			}
			if (commandHandler == null)
				throw new Exception("CommandQueue for [" + getName() + "] failed");
			log("CommandQueue  [" + getName() + "], started");
			isQueueStarted = true;
		}
	}
	
	public boolean isQueueStarted(){
		return isQueueStarted ;
	}

	public final void startCommandQueue() {		
			try {
				if (!isQueueStarted) {
					isQueueStarted = true;
					start();
					int timeout = 0;

					while (commandHandler == null && timeout < 1000) {
						timeout++;
						Thread.sleep(1);
					}
					if (commandHandler == null)
						throw new Exception("CommandQueue for [" + getName() + "] failed");
					log("CommandQueue  [" + getName() + "], started");

				}
			} catch (Exception err) {
				log("err", err);
			}
		
	}

	/***
	 * Send simple message
	 * 
	 * @param what
	 *            message id
	 */
	public void sendMessage(int what) {
		
			try {
				if (isPreExit)
					return;
				if(!commands_in_use.contains(""+what))
					commands_in_use.add(""+what);
				
				if (commandHandler != null)
					commandHandler.sendEmptyMessage(what);
			} catch (Exception err) {
				log("sendMessage", err);
			}

	}

	/**
	 * Attempts to cancel operation that has not already started. Note that
	 * there is no guarantee that the operation will be canceled. They still may
	 * result in a call to on[Query/Insert/Update/Delete]Complete after this
	 * call has completed.
	 * 
	 * @param token
	 *            The token representing the operation to be canceled. If
	 *            multiple operations have the same token they will all be
	 *            canceled.
	 */
	public final void removeMessages(int token) {
		
			try {
				if (isPreExit)
					return;
				if(!commands_in_use.contains(""+token))
					commands_in_use.add(""+token);
				commandHandler.removeMessages(token);
			} catch (Exception ex) {
				if (!isPreExit)
					log("sendMessage", ex);
			}
		
	}

	public final boolean hasMessages(int what) {

			try {
				if (isPreExit)
					return false;
				return commandHandler.hasMessages(what);
			} catch (Exception ex) {
				if (!isPreExit)
					log("", ex);
				return false;
			}

	}

	/***
	 * Send simple message<br>
	 * Note: this method cannot be used when sending messages across processes.
	 * 
	 * @param object
	 *            message object
	 * @see com.oovoo.utils.EventObject
	 */
	public void sendMessage(EventObject object) {

			try {
				if (isPreExit)
					return;
				// Use the token as what so cancelOperations works properly
				Message msg = commandHandler.obtainMessage(object.eventId());

				if(!commands_in_use.contains(""+object.eventId()))
					commands_in_use.add(""+object.eventId());
				
				msg.what = object.eventId();
				msg.obj = object;
				if (object.delay() > 0)
					commandHandler.sendMessageDelayed(msg, object.delay());
				else
					commandHandler.sendMessage(msg);
			} catch (Exception err) {
				if (!isPreExit)
					log("sendMessage Object ", err);
			}

	}

	public void sendMessage(long id, Object object) {

			try {
				if (isPreExit)
					return;
				// Use the token as what so cancelOperations works properly
				Message msg = commandHandler.obtainMessage((int) id);
				if(!commands_in_use.contains(""+(int) id))
					commands_in_use.add(""+(int) id);
				// Message msg = new Message();
				msg.what = (int) id;
				msg.obj = object;
				commandHandler.sendMessage(msg);
			} catch (Exception err) {
				if (!isPreExit)
					log("sendMessage Object ", err);
			}
		
	}
	

	
	public void sendMessage(long id, Object object,long delay) {

		try {
			if (isPreExit)
				return;
			// Use the token as what so cancelOperations works properly
			Message msg = commandHandler.obtainMessage((int) id);
			if(!commands_in_use.contains(""+(int) id))
				commands_in_use.add(""+(int) id);
			// Message msg = new Message();
			msg.what = (int) id;
			msg.obj = object;
			commandHandler.sendMessageDelayed(msg, delay);
		} catch (Exception err) {
			if (!isPreExit)
				log("sendMessage Object ", err);
		}
	
}

	public void sendMessageAtFront(EventObject object) {

			try {
				if (isPreExit)
					return;
				// Use the token as what so cancelOperations works properly
				Message msg = commandHandler.obtainMessage(object.eventId());

				if(!commands_in_use.contains(""+object.eventId()))
					commands_in_use.add(""+object.eventId());
				msg.what = object.eventId();
				msg.obj = object;
				commandHandler.sendMessageAtFrontOfQueue(msg);

			} catch (Exception err) {
				if (!isPreExit)
					log("sendMessage Object ", err);
			}
		
	}

	public void sendMessage(EventObject object, long delay) {

			try {
				if (isPreExit)
					return;
				if(!commands_in_use.contains(""+object.eventId()))
					commands_in_use.add(""+object.eventId());
				
				Message msg = new Message();
				msg.what = object.eventId();
				msg.obj = object;
				commandHandler.sendMessageDelayed(msg, delay);

			} catch (Exception err) {
				if (!isPreExit)
					log("sendMessage Object ", err);
			}
		
	}
	
	public void sendMessage(Object object){
		sendMessage(object,0);
	}
			
	public void sendMessage(Object object, long delay) {

		try {
			if (isPreExit)
				return;
//			if(!commands_in_use.contains(""+object.eventId()))
//				commands_in_use.add(""+object.eventId());
			
			Message msg = new Message();
			//msg.what = object.eventId();
			msg.obj = object;
			commandHandler.sendMessageDelayed(msg, delay);

		} catch (Exception err) {
			if (!isPreExit)
				log("sendMessage Object ", err);
		}
	
}
	
	public void sendMessage(EventObject object, int arg1, long delay) {

		try {
			if (isPreExit)
				return;
			if(!commands_in_use.contains(""+object.eventId()))
				commands_in_use.add(""+object.eventId());
			
			Message msg = new Message();
			msg.what = object.eventId();
			msg.obj = object;
			msg.arg1 = arg1;
			commandHandler.sendMessageDelayed(msg, delay);

		} catch (Exception err) {
			if (!isPreExit)
				log("sendMessage Object ", err);
		}
	
}

	public void execute(Runnable runnable, long time) {

			try {
				if (isPreExit)
					return;
				commandHandler.postDelayed(runnable, time);

			} catch (Exception err) {
				if (!isPreExit)
					log("sendMessage Object ", err);
			}
		
	}

	/***
	 * Send command with delay before execute
	 * 
	 * @param what
	 * @param delay
	 */
	public void sendMessage(int what, long delay) {
			try {
				if (isPreExit)
					return;
				if(!commands_in_use.contains(""+what))
					commands_in_use.add(""+what);
				commandHandler.sendEmptyMessageDelayed(what, delay);
			} catch (Exception err) {
				if (!isPreExit)
					log("sendMessage with delay error", err);
			}
	}

	public void sendMessageAtFrontOfQueue(int what) {
		
			try {
				if (isPreExit)
					return;
				if(!commands_in_use.contains(""+what))
					commands_in_use.add(""+what);
				Message msg = commandHandler.obtainMessage(what);
				commandHandler.sendMessageAtFrontOfQueue(msg);
			} catch (Exception err) {
				if (!isPreExit)
					log("sendMessage with delay error", err);
			}
	}

	/**
	 * Command queue routine
	 * 
	 * @param msg
	 *            current command
	 * @see android.os.Message
	 */
	protected void onHandleCommandMessage(Message msg) {

	}

	/***
	 * Command queue of connection
	 */
	public void run() {
		
			try {
				Looper.prepare();
				commandHandler = new Handler() {
					public void handleMessage(Message msg) {
						commands_in_use.remove(""+msg.what);
						onHandleCommandMessage(msg);
					}
				};
				Looper.loop();
			} catch (Exception err) {
				if (!isPreExit)
					log("run", err);
			}
	
	}

	public synchronized void end() {
		
			try {
				isPreExit = true;
				if(commands_in_use != null && !commands_in_use.isEmpty())
				{
					try
					{
						for(int i = 0 ; i < commands_in_use.size();i++)
						{
							commandHandler.removeMessages(Integer.parseInt(commands_in_use.get(i)));
						}
					}
					catch(Exception err)
					{
						if (!isPreExit)
							log("",err);
					}
				}
				if (commandHandler != null && commandHandler.getLooper() != null) {
					commandHandler.getLooper().quit();
				}
				commandHandler = null;
			} catch (Exception err) {
				if (!isPreExit)
					log("end", err);
			}
		
	}


	/**
	 * Send to standard err out a message
	 * */
	public void log(String log) {
		Log.i(TAG, log);
	}

	public void logD(String log) {
		Log.d(TAG, log);
	}

	public void logW(String log) {
		Log.w(TAG, log);
	}
	
	public void logW(String log, Exception ex) {
		Log.w(TAG, log, ex);
	}
	
	public void logE(String log) {
		Log.e(TAG, log);
	}

	public void log(String log, Exception err) {
		Log.e(TAG, log, err);
	}

	public void log(String log, Throwable err) {
		Log.e(TAG, log, err);
	}
}

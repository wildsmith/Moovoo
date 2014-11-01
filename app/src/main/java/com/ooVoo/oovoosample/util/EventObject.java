//
// EventObject.java
// 
// Created by ooVoo on July 22, 2013
//
// © 2013 ooVoo, LLC.  Used under license. 
//
package com.ooVoo.oovoosample.util;

import java.io.Serializable;

public class EventObject implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8019369641557487377L;

	/** Event ID , all developer must define as necessary for new each event */
	protected int eventId = -1;
	/** Event data that transferred by this event */
	protected Object eventData = null;
	protected long delay = 0;

	/***
	 * Create a new instance of EventObject
	 * 
	 * @param id
	 *            event ID
	 * @param obj
	 *            event data
	 */
	public EventObject(int id, Object obj) {
		eventId = id;
		eventData = obj;
	}

	/***
	 * Create a new instance of EventObject
	 * 
	 * @param id
	 * @param obj
	 * @param delay
	 */
	public EventObject(int id, Object obj, long delay) {
		eventId = id;
		eventData = obj;
		this.delay = delay;
	}

	/***
	 * Create a new instance of EventObject
	 * 
	 * @param id
	 */
	public EventObject(int id) {
		eventId = id;
	}

	/***
	 * Create a new instance of EventObject
	 * 
	 * @param id
	 * @param delay
	 */
	public EventObject(int id, long delay) {
		eventId = id;
		this.delay = delay;
	}

	/***
	 * Get message after which time message will be delivered to Handler loop
	 * 
	 * @return
	 */
	public long delay() {
		return delay;
	}

	/***
	 * Get event id
	 * 
	 * @return id
	 */
	public int eventId() {
		return eventId;
	}

	/**
	 * Get event data
	 * 
	 * @return data
	 */
	public Object eventData() {
		return eventData;
	}

	public void setResult(Object obj) {
		eventData = obj;
	}

	public Object getResult() {
		return eventData;
	}

	public void destroy() {
		eventId = -1;
		eventData = null;
	}

	public String toString() {
		return "id = " + eventId + ", data = " + eventData;
	}
}

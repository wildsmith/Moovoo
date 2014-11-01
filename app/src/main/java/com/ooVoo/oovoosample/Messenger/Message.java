package com.ooVoo.oovoosample.Messenger;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Message {
	/**
	 * The owner of the message
	 */
	private String owner;
	/**
	 * The content of the message
	 */
	private String message;
	/**
	 * The owner of the message
	 */
	private String timestamp;
	/**
	 * boolean to determine, who is sender of this message
	 */
	private boolean isMine;
	
	/**
	 * Constructor to make a Message object
	 */
	public Message(String message, boolean isMine) {
		super();
		this.message = message;
		this.isMine = isMine;
		
		SimpleDateFormat timestampFormat = new SimpleDateFormat("MMM dd, yyy, HH:mm");
		this.timestamp = timestampFormat.format(new Date());
		
		if (isMine) owner = "Me";
	}
	public Message(String message, String owner, boolean isMine) {
		this(message, isMine);
		this.owner = owner;
	}
	public String getOwner() {
		return owner;
	}
	public void setOwner(String owner) {
		this.owner = owner;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}
	public boolean isMine() {
		return isMine;
	}
	public void setMine(boolean isMine) {
		this.isMine = isMine;
	}
}

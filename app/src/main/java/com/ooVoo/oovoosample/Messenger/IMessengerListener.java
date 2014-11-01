package com.ooVoo.oovoosample.Messenger;

public interface IMessengerListener {
	
	public void onTextReceived(byte[] buffer, String participantName);

}

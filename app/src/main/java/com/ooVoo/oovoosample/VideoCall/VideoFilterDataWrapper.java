package com.ooVoo.oovoosample.VideoCall;

import com.oovoo.core.device.deviceconfig.VideoFilterData;

public class VideoFilterDataWrapper {
	private VideoFilterData _data;
	
	public VideoFilterDataWrapper( VideoFilterData data) {
		_data = data;
	}
	
	public String id() { return _data.id(); }
	public String toString() { return _data.name(); }
}

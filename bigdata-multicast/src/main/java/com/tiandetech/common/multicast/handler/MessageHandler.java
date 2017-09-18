package com.tiandetech.common.multicast.handler;

import com.tiandetech.common.multicast.message.TiandeMulticastMessage;

public interface MessageHandler {
	public void handler(TiandeMulticastMessage message);
}

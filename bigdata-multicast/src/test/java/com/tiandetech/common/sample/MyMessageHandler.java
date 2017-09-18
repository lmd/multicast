package com.tiandetech.common.sample;

import com.tiandetech.common.multicast.handler.MessageHandler;
import com.tiandetech.common.multicast.message.TiandeMulticastMessage;

public class MyMessageHandler implements MessageHandler {

	static int index = 1;
	@Override
	public void handler(TiandeMulticastMessage message) {
		String method = message.getMethod();
		Object name = message.getProperty("name");
		String body = message.getBody();
		
//		System.out.println("method=" + method + "   body=" + body);
		if(body.length() > 1000){
			System.out.println("method=" + method + "   body=" + body.substring(0, 100));
		}else{
			System.out.println("method=" + method + "   body=" + body);
		}
//		
		
		System.out.println("end=" + System.currentTimeMillis());
		synchronized (this) {
			System.out.println(index++);
		}
	}

}

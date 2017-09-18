package com.tiandetech.common.sample;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import com.tiandetech.common.multicast.TiandeMulticastSocket;
import com.tiandetech.common.multicast.message.TiandeMulticastMessage;

/**
 * 天德组播 使用样板
 * @author xiaoming
 */
public class Sample {

	public static void main(String[] args) throws InterruptedException {
		MyMessageHandler handler = new MyMessageHandler();
		
		/* 开启接收监听 */
		TiandeMulticastSocket tiandeMulticastSocket = new TiandeMulticastSocket();
		tiandeMulticastSocket.setGroup("228.0.0.10");
		tiandeMulticastSocket.setIntervalSend(0);
		tiandeMulticastSocket.setReceiveTimeOut(2000);
		tiandeMulticastSocket.setBlockMaxSize(1024 * 50);
		tiandeMulticastSocket.setReceivePacketSize(1024 * 60);
		
		tiandeMulticastSocket.registerHandler(handler);
		tiandeMulticastSocket.start();
		
		
		Thread.sleep(1000);
		
		
		/* 组播消息 (发送一个注册的请求) */
		String text = getText();
		for(int i = 0; i < 3; i++){
			System.out.println("send start " + System.currentTimeMillis());
			
			TiandeMulticastMessage msg = new TiandeMulticastMessage();
			msg.setMethod("register_"+i);
			msg.setProperty("key", "value");
			msg.setBody(i+"__" + text);
			tiandeMulticastSocket.send(msg);
			
			Thread.sleep(1000);
		}
		
	}
	
	private static String getText(){
		try {
			FileInputStream fin = new FileInputStream("C:\\lmdapps\\multicast.txt");
			
			int end = 0;
			byte[] b = new byte[1024];
		    
			StringBuilder sb = new StringBuilder();
			while((end = fin.read(b)) != -1){
				sb.append(new String(b,0,end,"gbk"));
			}
			
			return sb.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

}

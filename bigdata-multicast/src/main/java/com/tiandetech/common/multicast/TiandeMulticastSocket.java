package com.tiandetech.common.multicast;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.tiandetech.common.multicast.message.TiandeMulticastMessageManager;

import it.sauronsoftware.base64.Base64;

import com.tiandetech.common.multicast.handler.MessageHandler;
import com.tiandetech.common.multicast.message.MessageBlock;
import com.tiandetech.common.multicast.message.MessageBlockPool;
import com.tiandetech.common.multicast.message.TiandeMulticastMessage;

/**
 * 
 * @author xiaoming
 */
public class TiandeMulticastSocket {
	private static int port = 8000;//监听端口
	private static InetAddress group = null;
	private static int receivePacketSize = 1024 * 5;//接收缓冲大小
	private static MulticastSocket cast = null;
	private int blockMaxSize = 2048;//拆分消息包最大byte
	private int receiveTimeOut = 10000;
	
	/* 是否间隔发送,拆包后循环发送时是否间隔发送 */
	private long intervalSend = 1;
	
	/* thread pool */
	private ExecutorService cachedThreadPool = Executors.newCachedThreadPool();
	
	/* message pool */
	private MessageBlockPool pool = null;
	
	static{
		try {
			group = InetAddress.getByName("228.0.0.1");
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}
	
	public TiandeMulticastSocket(){}
	
	public TiandeMulticastSocket(int port, InetAddress group, int receivePacketSize, int blockMaxSize){
		this.port = port;
		this.group = group;
		this.receivePacketSize = receivePacketSize;
		this.blockMaxSize = blockMaxSize;
	}
	
	public TiandeMulticastSocket(int port, String group, int receivePacketSize, int blockMaxSize){
		this.port = port;
		this.receivePacketSize = receivePacketSize;
		this.blockMaxSize = blockMaxSize;
		
		try {
			this.group = InetAddress.getByName(group);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		};
	}
	
	public TiandeMulticastSocket(int port, InetAddress group){
		this.port = port;
		this.group = group;
	}
	
	public TiandeMulticastSocket(InetAddress group){
		this.group = group;
	}
	
	public TiandeMulticastSocket(int port){
		this.port = port;
	}
	
	/**
	 * 默认局域网内组播
	 */
	private void initCast(){
		try {
			cast = new MulticastSocket(port);
			cast.setSoTimeout(receiveTimeOut);
			cast.joinGroup(group);
		} catch (IOException e) {
			throw new TiandeMultiCastException(e.getMessage());
		}
	}
	
	/**
	 * 异步开启组播服务
	 */
	public void start(){
		initCast();
		
		/* 异步开启接收消息 */
		cachedThreadPool.execute(new Runnable(){
			@Override
			public void run() {
				receiveRun();
			}
		});
		
	}
	/**
	 * 组播接收消息
	 */
	private void receiveRun(){
		if(cast == null){
			throw new TiandeMultiCastException("Multi cast server is closed!");
		}
		
		/* 死循环接收包 */
		TiandeMulticastMessageManager messageManager  = new TiandeMulticastMessageManager();
		while(true){
			byte[] receiveBuffer = new byte[receivePacketSize];
			DatagramPacket packet = new DatagramPacket(receiveBuffer, receiveBuffer.length);
			try {
				cast.receive(packet);
				
				/* 异步解析处理消息 */
				this.getCachedThreadPool().execute(new Runnable(){
					@Override
					public void run() {
						//解析
						MessageBlock messageBlock = null;
						try {
							messageBlock = messageManager.getMessageBlock(packet);
						} catch (UnsupportedEncodingException e) {
							e.printStackTrace();
						}
						
						/* 交由消息池处理 */
						pool.put(messageBlock);
					}
				});
				
			} catch (UnsupportedEncodingException e1) {
				//解析MessageBlock 失败
				e1.printStackTrace();
			} catch (Exception e) {
				
				/* 可能由于网络断开链接丢失,那么从新开启服务 */
				cast.disconnect();
				initCast();
			}
			
		}
	}
	
	/**
	 * 组播发送消息
	 * @param msg
	 */
	public void send(TiandeMulticastMessage msg){
		if(cast == null){
			throw new TiandeMultiCastException("Multi cast server is closed!");
		}
		
		/* 判断msg大小 */
		String msgJson = msg.toString();
		//base64转码
		String msgBase64 = Base64.encode(msgJson);
		
		/* 拆包发送 */
		TiandeMulticastMessageManager castMessageManager = new TiandeMulticastMessageManager(msgBase64, this);
		List<DatagramPacket> packetList = castMessageManager.getDatagramPacketList();
		for(int i = 0; i < packetList.size(); i++){
			try {
				/* 休眠时间 */
				Thread.sleep(intervalSend);
				
				cast.send(packetList.get(i));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public void registerHandler(MessageHandler handler){
		this.pool = new MessageBlockPool(handler, this);
	}
	
	public ExecutorService getCachedThreadPool() {
		return cachedThreadPool;
	}

	public static int getPort() {
		return port;
	}

	public static void setPort(int port) {
		TiandeMulticastSocket.port = port;
	}

	public static InetAddress getGroup() {
		return group;
	}

	public static void setGroup(InetAddress group) {
		TiandeMulticastSocket.group = group;
	}
	
	public static void setGroup(String group) {
		try {
			TiandeMulticastSocket.group = InetAddress.getByName(group);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	public static int getReceivePacketSize() {
		return receivePacketSize;
	}

	public static void setReceivePacketSize(int receivePacketSize) {
		TiandeMulticastSocket.receivePacketSize = receivePacketSize;
	}

	public int getBlockMaxSize() {
		return blockMaxSize;
	}

	public void setBlockMaxSize(int blockMaxSize) {
		this.blockMaxSize = blockMaxSize;
	}

	public int getReceiveTimeOut() {
		return receiveTimeOut;
	}

	public void setReceiveTimeOut(int receiveTimeOut) {
		this.receiveTimeOut = receiveTimeOut;
	}

	public long getIntervalSend() {
		return intervalSend;
	}

	public void setIntervalSend(long intervalSend) {
		if(intervalSend < 0){
			intervalSend = 0;
		}
		this.intervalSend = intervalSend;
	}
	
	public boolean isEmptyHandler(){
		if(this.pool == null || this.pool.getHanler() == null){
			return true;
		}else{
			return false;
		}
	}
}

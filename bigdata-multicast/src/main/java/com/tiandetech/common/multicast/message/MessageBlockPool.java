package com.tiandetech.common.multicast.message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.tiandetech.common.multicast.TiandeMulticastSocket;
import com.tiandetech.common.multicast.handler.MessageHandler;

/**
 * 消息区块池,负责存储消息区块数据,和拼接还原原始消息
 * 还原TiandeMulticastMessage 后交由 MessageHandler 实现去处理
 * @author xiaoming
 */
public class MessageBlockPool {
	private Map<String, Map<String, List<MessageBlock>>> messagePool = new HashMap<String, Map<String, List<MessageBlock>>>();//<send, Map<blockId, List<MessageBlock>>>
	/* handler */
	private MessageHandler hanler;
	
	private TiandeMulticastSocket tiandeMulticastSocket = null;
	
	private TiandeMulticastMessageManager messageManager  = new TiandeMulticastMessageManager();
	
	public MessageBlockPool(MessageHandler hanler, TiandeMulticastSocket tiandeMulticastSocket){
		this.hanler = hanler;
		this.tiandeMulticastSocket = tiandeMulticastSocket;
	}
	
	/**
	 * 多线程同时操作调用,使用方法锁同步保证池安全.方法内部不应该存在消耗性能的代码
	 * @param messageBlock
	 */
	public synchronized void put(MessageBlock messageBlock) {
		if(messageBlock == null){
			return;
		}
		
		/* 获取sender 域 */
		Map<String, List<MessageBlock>> sendDoMain = messagePool.get(messageBlock.getSender());
		if(sendDoMain == null){
			sendDoMain = new HashMap<String, List<MessageBlock>>();
			messagePool.put(messageBlock.getSender(), sendDoMain);
		}
		
		/* 获取block pool */
		List<MessageBlock> blockPool = sendDoMain.get(messageBlock.getId());
		if(blockPool == null){
			blockPool = new ArrayList<MessageBlock>(100);
			sendDoMain.put(messageBlock.getId(), blockPool);
		}
		
		/* put to block pool */
		blockPool.add(messageBlock);
		
		/* 验证是否已经收到够数的block 异步执行 */
		if(blockPool.size() == messageBlock.getHeight()){
			//清除pool 中块信息
			List<MessageBlock> pool = sendDoMain.remove(messageBlock.getId());
			
			tiandeMulticastSocket.getCachedThreadPool().execute(new Runnable(){
				@Override
				public void run() {
					TiandeMulticastMessage message = messageManager.getTiandeMulticastMessage(pool);//这里可能比较消耗性能
					hanler.handler(message);
				}
			});
			
		}
		
	}

	public MessageHandler getHanler() {
		return hanler;
	}
	
}

package com.tiandetech.common.multicast.message;

import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.tiandetech.common.multicast.TiandeMultiCastException;
import com.tiandetech.common.multicast.TiandeMulticastSocket;

import it.sauronsoftware.base64.Base64;

/**
 * 消息管理器
 * 1.负责拆分消息成多个package交由 udp 发送
 * 2.合并多个package得到一个原始消息体
 * @author xiaoming
 */
public class TiandeMulticastMessageManager {
	private TiandeMulticastSocket tiandeMulticastSocket;
	
	private String code = "UTF-8";
	private String message = "";
	
	public TiandeMulticastMessageManager(){}
	
	public TiandeMulticastMessageManager(String message, TiandeMulticastSocket tiandeMulticastSocket){
		this.message = message;
		this.tiandeMulticastSocket = tiandeMulticastSocket;
	}
	
	/**
	 * 解析MessageBlock
	 * @param packet
	 * @return
	 * @throws Exception
	 */
	public MessageBlock getMessageBlock(DatagramPacket packet) throws UnsupportedEncodingException{
		
		
		String blockJson = new String(packet.getData(), 0, packet.getLength(), code);
		
		JSONObject messageBlockJsonO = new JSONObject();
		try {
			messageBlockJsonO = JSONObject.parseObject(blockJson);
		} catch (JSONException e) {
			//可能是重复或者错误的包
			return null;
		}
		
		MessageBlock msgBlock = JSONObject.toJavaObject(messageBlockJsonO, MessageBlock.class);
		msgBlock.setSender(packet.getAddress().toString());
		return msgBlock;
		
	}
	
	/**
	 * 拼装还原消息
	 * @param msgBlockList
	 * @return
	 */
	public TiandeMulticastMessage getTiandeMulticastMessage(List<MessageBlock> msgBlockList){
		/* 检查 msgBlockList 是否正确 */
		if(!veryBlockList(msgBlockList)){
			throw new TiandeMultiCastException("Message block List is failed!");
		}
		
		/* 还原消息体 TiandeMulticastMessage */
		Map<Integer, String> bodyMap = new HashMap<Integer, String>();
		for(int i = 0; i < msgBlockList.size(); i++){
			MessageBlock block = msgBlockList.get(i);
			bodyMap.put(block.getIndex(), block.getBody());//保存每块的body
		}
		
		//拼接body
		StringBuilder msgSB = new StringBuilder();
		for(int index = 0; index < msgBlockList.size(); index++){
			String body = bodyMap.get(index);
			msgSB.append(body);
		}
		
		//base64解码
		String msgBase64 = msgSB.toString();
		
		TiandeMulticastMessage tiandeMulticastMessage = JSONObject.toJavaObject(JSONObject.parseObject(Base64.decode(msgBase64)), TiandeMulticastMessage.class);
		return tiandeMulticastMessage;
	}
	
	private boolean veryBlockList(List<MessageBlock> msgBlockList){
		//长度
		if(msgBlockList.size() != msgBlockList.get(0).getHeight()){
			return false;
		}
		
		//深度验证
		Map<Integer, Integer> indexMap = new HashMap<Integer, Integer>();
		String id = msgBlockList.get(0).getId();
		for(int index = 0; index < msgBlockList.size(); index++){
			MessageBlock block = msgBlockList.get(index);
			if(!id.equals(block.getId())){
				return false;
			}
			
			//index
			Integer count = indexMap.get(block.getIndex());
			if(count == null || count == 0){
				indexMap.put(block.getIndex(), 1);
			}else{
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * 获取udp的消息包集合,将原始消息体才分成n个消息包.
	 * @return
	 */
	public List<DatagramPacket> getDatagramPacketList(){
		if(message.isEmpty()){
			return null;
		}
		
		/* 拆分 message */
		String id = UUID.randomUUID().toString();
		List<DatagramPacket> packetList = new ArrayList<DatagramPacket>();
		int height = getMessageBlockNum();
		for(int index = 0; index < height; index++){
			String body = "";
			try {
				body = message.substring(index * tiandeMulticastSocket.getBlockMaxSize(), (index * tiandeMulticastSocket.getBlockMaxSize()) + tiandeMulticastSocket.getBlockMaxSize());
			} catch (Exception e) {
				body = message.substring(index * tiandeMulticastSocket.getBlockMaxSize(), message.length());
			}
			
			
			//MessageBlock
			MessageBlock msgBlock = new MessageBlock();
			msgBlock.setId(id);
			msgBlock.setIndex(index);
			msgBlock.setHeight(height);
			msgBlock.setBody(body);
			
			//保存消息包
			packetList.add(getMsgPacket(msgBlock.toString()));
		}
		
		return packetList;
	}
	
	private DatagramPacket getMsgPacket(String msg){
		try {
			byte[] buffer = msg.getBytes(code);
			DatagramPacket dataPachet = new DatagramPacket(buffer, buffer.length, tiandeMulticastSocket.getGroup(), tiandeMulticastSocket.getPort());
			return dataPachet;
		} catch (Exception e) {
			throw new TiandeMultiCastException(e.getMessage());
		}
	}
	
	/**
	 * 获取拆分个数
	 * @return
	 */
	private int getMessageBlockNum(){
		int num = message.length() / tiandeMulticastSocket.getBlockMaxSize();
		if((message.length() % tiandeMulticastSocket.getBlockMaxSize()) > 0){
			num++;
		}
		
		return num;
	}
	
}


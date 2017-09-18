package com.tiandetech.common.multicast.message;

import com.alibaba.fastjson.JSONObject;
/**
* 才分后的消息描述实体
* @author xiaoming
*/
public class MessageBlock{
	private String id;//原始消息实体唯一标识
	private int index;//被才分后的消息块坐标从0开始一次递增
	private int height;//原始消息才分出的高度. 相当于一个原始消息被才分成的块个数.
	private String sender;//发送者
	private String body;//才分得出的消息体
	
	public String getId(){
		return id;
	}
	
	public void setId(String id){
		this.id = id;
	}
	
	public int getIndex(){
		return index;
	}
	
	public void setIndex(int index){
		this.index = index;
	}
	
	public int getHeight(){
		return height;
	}
	
	public void setHeight(int height){
		this.height = height;
	}
	
	public String getSender(){
		return sender;
	}
	
	public void setSender(String sender){
		this.sender = sender;
	}
	
	public String getBody(){
		return body;
	}
	
	public void setBody(String body){
		this.body = body;
	}

	@Override
	public String toString() {
		return JSONObject.toJSONString(this);
	}
	
}


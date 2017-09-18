package com.tiandetech.common.multicast.message;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
/**
 * 天德组播原始消息实体
 * @author xiaoming
 */
public class TiandeMulticastMessage {
	private Map<String, Object> property = new HashMap<String, Object>();
	private String method;//目标方法
	private String body;//原始消息体
	
	public TiandeMulticastMessage(){}
	
	public TiandeMulticastMessage(String method, String body){
		this.method = method;
		this.body = body;
	}
	
	public TiandeMulticastMessage(String method, String body, Map<String, Object> property){
		this.method = method;
		this.body = body;
		this.property = property;
	}
	
	public Map<String, Object> getProperty() {
		return property;
	}
	
	public void setProperty(Map<String, Object> property) {
		this.property = property;
	}
	
	public Object getProperty(String key) {
		return property.get(key);
	}
	public void setProperty(String key, Object value) {
		this.property.put(key, value);
	}
	public String getMethod() {
		return method;
	}
	public void setMethod(String method) {
		this.method = method;
	}
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}
	
	@Override
	public String toString() {
		return JSONObject.toJSONString(this);
	}
	
}

package com.sumavision.talktv2.net;

import java.io.InputStream;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;

/**
 * 
 * @author 郭鹏
 * @createTime
 * @description 网络通信监听接口
 * @changeLog
 * 
 */
public interface NetListener {

	public void onBegin();

	public void onError(String errorMessage, JSONParser parser);

	public void onResponse(StatusLine statusLine, Header[] headers,
			HttpEntity entity, JSONParser parser);

	public void onResponse(int statusLine, InputStream stream, JSONParser parser);
	
	public void onResponse(int statusLine, String str, JSONParser parser);

}

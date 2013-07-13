package com.sumavision.talktv2.net;

/**
 * 
 * @author 郭鹏
 * @createTime
 * @description NetServer接口
 * @changeLog
 * 
 */
public interface NetServerListener {

	public void onBegin();

	public void onResponse(String errorMessage);

}

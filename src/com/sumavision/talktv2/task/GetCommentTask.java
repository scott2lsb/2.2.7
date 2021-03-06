package com.sumavision.talktv2.task;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.sumavision.talktv2.data.OtherCacheData;
import com.sumavision.talktv2.net.NetConnectionListener;
import com.sumavision.talktv2.net.NetUtil;
import com.sumavision.talktv2.net.TalkListNewParser;
import com.sumavision.talktv2.net.TalkListRequest;

/**
 * @author jianghao
 * @version 2.2
 * @createTime 2012-12-29
 * @description 取评论
 * @changLog
 */
public class GetCommentTask extends AsyncTask<Object, Void, String> {
	private final String TAG = "GetCommentTask";
	private NetConnectionListener listener;
	private final String method = "talkList";

	@Override
	protected String doInBackground(Object... params) {
		Context context;
		TalkListRequest request;
		TalkListNewParser parser;
		String data;
		context = (Context) params[0];
		listener = (NetConnectionListener) params[1];
		request = (TalkListRequest) params[2];
		parser = (TalkListNewParser) params[3];
		data = request.make();
		if (OtherCacheData.current().isDebugMode)
			Log.e(TAG, data);
		listener.onNetBegin(method);
		String s = NetUtil.execute(context, data, null);
		if (s != null) {
			if (OtherCacheData.current().isDebugMode)
				Log.e(TAG, s);
			// String result = parser.parse(s);
			// return result;

			return s;
		} else {
			return null;
		}
	}

	@Override
	protected void onCancelled() {
		try {
			listener.onCancel(method);
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
		if (OtherCacheData.current().isDebugMode)
			Log.e(TAG, "canceled");
		super.onCancelled();
	}

	@Override
	protected void onPostExecute(String result) {

		try {
			listener.onNetEnd(result, method);
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
		if (OtherCacheData.current().isDebugMode)
			Log.e(TAG, "finished");
		super.onPostExecute(result);
	}
}

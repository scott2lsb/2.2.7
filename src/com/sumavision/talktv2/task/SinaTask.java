package com.sumavision.talktv2.task;

import android.os.AsyncTask;
import android.util.Log;

import com.sumavision.talktv2.data.OtherCacheData;
import com.sumavision.talktv2.data.SinaData;
import com.weibo.sdk.android.api.StatusesAPI;

/**
 * @author 李梦思
 * @version 2.2
 * @createTime 2013-1-10
 * @description 同步到新浪微博
 * @changLog
 */
public class SinaTask extends AsyncTask<Object, Void, String> {
	private final String TAG = "Sina";

	@Override
	protected String doInBackground(Object... params) {
		StatusesAPI statusesAPI = new StatusesAPI(SinaData.weibo().accessToken);
		// statusesAPI.update(SinaData.content, UserNow.current().lat,
		// UserNow.current().lon, listener);
		return null;
	}

	@Override
	protected void onCancelled() {
		if (OtherCacheData.current().isDebugMode)
			Log.e(TAG, "canceled");
		super.onCancelled();
	}

	@Override
	protected void onPostExecute(String result) {
		if (OtherCacheData.current().isDebugMode)
			Log.e(TAG, "finished");
		super.onPostExecute(result);
	}
}

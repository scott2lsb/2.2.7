package com.sumavision.talktv2.net;

import java.io.BufferedWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpHostConnectException;
import com.sumavision.talktv2.net.JSONMessageType;
import android.util.Log;

import com.sumavision.talktv2.data.OtherCacheData;

/**
 * 
 * @author 郭鹏
 * @createTime
 * @description 网络通信类
 * @changeLog
 * 
 */
public class NetNew {
	protected NetListener listener;
	protected HttpClient client;
	protected JSONParser parser;
	protected HttpPost post;
	protected String proxyHost;
	protected int proxyPort;

	private Thread t;
	private long now;
	private HttpURLConnection conn;
	private InputStream returnValues;

	protected ResponseHandler<String> responseHandler = new ResponseHandler<String>() {
		@Override
		public String handleResponse(HttpResponse response)
				throws ClientProtocolException, IOException {

			if (OtherCacheData.current().isDebugMode) {
				Log.e("Net-responseHandler", System.currentTimeMillis() - now
						+ "");
			}
			notifyResponse(response.getStatusLine(), response.getAllHeaders(),
					response.getEntity());

			return null;
		}

	};

	public NetNew() {

	}

	public NetNew(NetListener listener) {
		this.listener = listener;
	}

	public void setListener(NetListener listener) {
		this.listener = listener;
	}

	public void request(final String url, final String data, JSONParser parser)
			throws HttpHostConnectException, SocketException,
			ConnectTimeoutException, SocketTimeoutException,
			UnknownHostException {
		this.parser = parser;
		t = new Thread((new Runnable() {

			@Override
			public void run() {
				notifyBegin();

				try {
					if (OtherCacheData.current().isDebugMode) {
						Log.e("Net-connection", "on here");
					}
					java.net.URL u = null;
					u = new java.net.URL(url);
					conn = (HttpURLConnection) u.openConnection();
					conn.setDoInput(true);
					conn.setDoOutput(true);
					conn.setUseCaches(false);
					conn.setInstanceFollowRedirects(true);
					conn.setRequestProperty("Charset", "UTF-8");
					conn.setRequestProperty("Content-Type",
							"application/x-javascript");
					// conn.setRequestProperty("Content-Type", "text/json");
					// conn.setRequestProperty("connection", "Keep-Alive");
					// conn.setRequestProperty("Content-Length",
					// String.valueOf(data.length()));
					conn.setReadTimeout(JSONMessageType.NET_READ_TIME_OUT_TIME);
					conn.setConnectTimeout(JSONMessageType.NET_TIME_OUT_TIME);
					// conn.setChunkedStreamingMode(64 * 1024);
					conn.setRequestMethod("POST");
					conn.connect();

					OutputStream outStream = conn.getOutputStream();
					OutputStreamWriter objSW = new OutputStreamWriter(outStream);
					BufferedWriter out = new BufferedWriter(objSW);
					out.write(data.toCharArray(), 0, data.toCharArray().length);
					out.flush();
					out.close();

					int status = conn.getResponseCode();
					returnValues = conn.getInputStream();

					if (status == 200) {
						String str = null;
						if (returnValues != null) {
							ByteArrayOutputStream os = new ByteArrayOutputStream();
							byte[] buffer = new byte[10240];
							int len = -1;
							while ((len = returnValues.read(buffer)) != -1) {
								os.write(buffer, 0, len);
							}
							byte[] data1 = os.toByteArray();
							os.close();
							returnValues.close();
							str = new String(data1, "UTF-8");
							if (OtherCacheData.current().isDebugMode) {
								Log.e("NetNew-", str);
							}
						}

						if (str != null) {
							notifyResponse(status, str);
						}
					} else {
						if (OtherCacheData.current().isDebugMode) {
							Log.e("NetNew-StatusError", "statusCode is not 200");
						}
					}
					conn.disconnect();
					if (returnValues != null) {
						returnValues.close();
					}
					if (OtherCacheData.current().isDebugMode) {
						Log.e("Net-connection", "on over");
					}

				} catch (UnsupportedEncodingException e1) {
					e1.printStackTrace();
//					notifyError("UnsupportedEncodingException");
				} catch (ClientProtocolException e) {
					e.printStackTrace();
//					notifyError("ClientProtocolException");
				} catch (IOException e) {
					e.printStackTrace();
//					notifyError("IOException");
				} catch (Exception e) {
					e.printStackTrace();
//					notifyError("Exception");
				} finally {

					if (OtherCacheData.current().isDebugMode) {
						Log.e("NetNew", "on finally");
					}

					if (conn != null) {
						conn.disconnect();
					}
					if (returnValues != null) {
						try {
							returnValues.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					if (t != null) {
						t.interrupt();
					}

					System.gc();
				}
			}
		}));
		t.start();

	}

	public void cancel() {
		if (t != null) {
			t.interrupt();
			t = null;
		}

		if (conn != null) {
			conn.disconnect();
			conn = null;
		}
		if (returnValues != null) {
			try {
				returnValues.close();
				returnValues = null;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		System.gc();
	}

	protected void notifyBegin() {
		if (listener != null)
			listener.onBegin();
	}

	protected void notifyError(String errorMessage) {
		if (listener != null)
			listener.onError(errorMessage, parser);
	}

	protected void notifyResponse(StatusLine statusLine, Header[] headers,
			HttpEntity entity) {

		if (listener != null)
			listener.onResponse(statusLine, headers, entity, parser);
	}

	protected void notifyResponse(int statusLine, InputStream stream) {
		if (listener != null)
			listener.onResponse(statusLine, stream, parser);
	}

	protected void notifyResponse(int statusLine, String stream) {
		if (listener != null)
			listener.onResponse(statusLine, stream, parser);
	}
}

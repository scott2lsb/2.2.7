package com.sumavision.talktv2.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpHostConnectException;

import android.util.Log;

import com.sumavision.talktv2.data.OtherCacheData;
import com.sumavision.talktv2.user.UserNow;

/**
 * 
 * @author 郭鹏
 * @createTime
 * @description 对整个网络通信与数据解析过程进行封装
 * @changeLog
 * 
 */
public class NetServer implements NetListener {
	public static final String url = "http://172.16.16.78:8180";
	public static final String host = // "http://218.89.192.146:80/clientProcess.do";
	// 成都服务器
	// "http://59.151.82.78:8180/clientProcess.do";
	// "http://tvfan.cn/clientProcess.do";
	"http://172.16.16.78:8180/clientProcess.do";

	// "http://59.151.82.78:8180/clientProcess.do";
	// "http://59.151.82.78:10010/clientProcess.do";
	// "http://172.16.16.71:8280/clientProcess.do";
	// "http://172.16.17.71:8280/clientProcess.do";
	// "http://www.tvfan.com.cn/clientProcess.do";
	// "http://10.5.0.3:8180/clientProcess.do";

	protected List<ServerListenerNode> listeners;
	protected String proxyHost;
	protected int proxyPort;
	protected NetNew net = new NetNew();
	private static NetServer instance = null;

	private NetServer() {

	}

	public static NetServer current() {
		if (instance == null) {
			instance = new NetServer();
		}
		return instance;
	}

	private class ServerListenerNode {
		public NetServerListener listener = null;
		public boolean isRemoved = false;
	}

	public void service(JSONRequest maker, JSONParser parser) {
		// SimpleDateFormat sdf = new SimpleDateFormat("mm-ss");
		// Log.e("service", sdf.format(new Date()));
		// System.currentTimeMillis()+"");

		long now = System.currentTimeMillis();

		String data = maker.make();
		if (net == null) {
			net = new NetNew();
		}
		if (OtherCacheData.current().isDebugMode)
			Log.e("Request", data);
		// net.shutdownConnection();
		// net = new Net();

		net.setListener(this);

		JSONMessageType.checkServerIP();
		if (OtherCacheData.current().isDebugMode) {
			Log.e("NetServer", UserNow.current().getMyServerAddress());
			Log.e("NetServer", JSONMessageType.URL_TITLE_SERVER);
		}
		try {
			// TODO: 内网1
			// net.request(UserNow.current().getMyServerAddress(), data,
			// parser);
			if (OtherCacheData.current().isDebugMode) {
				Log.e("NetServer-service", System.currentTimeMillis() - now
						+ "");
			}
			// TODO: 内网3
			net.request(host, data, parser);

		} catch (HttpHostConnectException e) {
			e.printStackTrace();
			UserNow.current().errMsg = JSONMessageType.SERVER_NETFAIL;
			notifyResponse(JSONMessageType.SERVER_NETFAIL);
		} catch (SocketException e) {
			e.printStackTrace();
			UserNow.current().errMsg = JSONMessageType.SERVER_NETFAIL;
			notifyResponse(JSONMessageType.SERVER_NETFAIL);
		} catch (ConnectTimeoutException e) {
			UserNow.current().errMsg = JSONMessageType.SERVER_NETFAIL;
			notifyResponse(JSONMessageType.SERVER_NETFAIL);
			e.printStackTrace();
		} catch (SocketTimeoutException e) {
			e.printStackTrace();
			UserNow.current().errMsg = JSONMessageType.SERVER_NETFAIL;
			notifyResponse(JSONMessageType.SERVER_NETFAIL);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			UserNow.current().errMsg = JSONMessageType.SERVER_NETFAIL;
			notifyResponse(JSONMessageType.SERVER_NETFAIL);
		} catch (Exception e) {
			e.printStackTrace();
			UserNow.current().errMsg = JSONMessageType.SERVER_NETFAIL;
			notifyResponse(JSONMessageType.SERVER_NETFAIL);
		} finally {

			if (OtherCacheData.current().isDebugMode) {
				Log.e("NetServer", "on finally");
			}
			cancel();
		}

	}

	public void cancel() {
		if (net != null) {
			net.cancel();
		}
	}

	public void addListener(NetServerListener listener) {
		if (listeners == null)
			listeners = new ArrayList<ServerListenerNode>();
		else {
			listeners.clear();
		}
		ServerListenerNode node = new ServerListenerNode();
		node.listener = listener;
		node.isRemoved = false;
		listeners.add(node);
	}

	public synchronized void removeListener(NetServerListener listener) {
		if (listener != null && listeners != null) {
			// listeners.remove(listener);
			// Iterator<ServerListenerNode> iterator = listeners.iterator();
			// while (iterator.hasNext()) {
			// ServerListenerNode temp = iterator.next();
			// if (temp.listener.equals(listener))
			// temp.isRemoved = true;
			// }
			listeners.clear();

		}
	}

	protected void notifyBegin() {
		if (listeners != null && listeners.size() > 0) {
			// Iterator<ServerListenerNode> iterator = listeners.iterator();
			// while (iterator.hasNext()) {
			// ServerListenerNode temp = iterator.next();
			// if (temp != null) {
			// if (!temp.isRemoved)
			// temp.listener.onBegin();
			// }
			// }

			listeners.get(0).listener.onBegin();
		}
	}

	protected void notifyResponse(String errorMessage) {

		if (listeners != null) {
			if (OtherCacheData.current().isDebugMode)
				Log.e("NetServer", "listeners.size()=" + listeners.size());
			Iterator<ServerListenerNode> iterator = listeners.iterator();
			while (iterator.hasNext()) {
				ServerListenerNode temp = iterator.next();

				if (!temp.isRemoved) {
					temp.listener.onResponse(errorMessage);
					if (OtherCacheData.current().isDebugMode)
						Log.e("NetServer", "onResponse correctly");
				} else {
					if (OtherCacheData.current().isDebugMode)
						Log.e("NetServer", "listeners is removed");
				}
			}
		}

	}

	@Override
	public void onBegin() {
		notifyBegin();
	}

	@Override
	public void onError(String errorMessage, JSONParser parser) {
		try {
			notifyResponse(errorMessage);
		} catch (ConcurrentModificationException e) {
			e.printStackTrace();
			UserNow.current().errMsg = JSONMessageType.SERVER_NETFAIL;
			notifyResponse(JSONMessageType.SERVER_NETFAIL);
		}
	}

	@Override
	public void onResponse(int status, InputStream body, JSONParser parser) {
		if (status != 200 || parser == null) {
			onError("未知错误", parser);
			if (OtherCacheData.current().isDebugMode)
				Log.e("NetServer", "onResponse error");
			return;
		}

		String parseResult = "";
		if (parser != null) {
			try {
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(body, "UTF-8"));
				StringBuilder builder = new StringBuilder();
				for (String line = null; (line = reader.readLine()) != null;) {
					builder.append(line).append("\n");
				}
				if (OtherCacheData.current().isDebugMode) {
					System.out.println(builder.toString());
				}
				// 解析JSON
				parseResult = parser.parse(builder.toString());
				builder = null;
				reader.close();
			} catch (IllegalStateException e) {
				e.printStackTrace();
				onError("数据解析出错", parser);
				if (OtherCacheData.current().isDebugMode)
					Log.e("NetServer", "onResponse error");
				return;
			} catch (Exception e) {
				e.printStackTrace();
				onError("数据解析出错", parser);
				if (OtherCacheData.current().isDebugMode)
					Log.e("NetServer", "onResponse error");
				return;
			}

			notifyResponse(parseResult);
			if (OtherCacheData.current().isDebugMode)
				Log.e("NetServer", "onResponse right");
		}
	}

	@Override
	public void onResponse(StatusLine statusLine, Header[] headers,
			HttpEntity entity, JSONParser parser) {
		if (statusLine.getStatusCode() != 200 || parser == null) {
			onError("未知错误", parser);
			return;
		}

		if (parser != null) {
			String parseResult = "";
			try {
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(entity.getContent(), "UTF-8"));
				StringBuilder builder = new StringBuilder();
				for (String line = null; (line = reader.readLine()) != null;) {
					builder.append(line).append("\n");
				}
				if (OtherCacheData.current().isDebugMode) {
					System.out.println(builder.toString());
				}
				// 解析JSON
				parseResult = parser.parse(builder.toString());
				builder = null;
				reader.close();
			} catch (IllegalStateException e) {
				e.printStackTrace();
				onError("数据解析出错", parser);
				return;
			} catch (IOException e) {
				e.printStackTrace();
				onError("数据解析出错", parser);
				return;
			}
			notifyResponse(parseResult);
		}
	}

	@Override
	public void onResponse(int statusLine, String str, JSONParser parser) {
		if (statusLine != 200 || parser == null) {
			onError("未知错误", parser);
			if (OtherCacheData.current().isDebugMode)
				Log.e("NetServer", "onResponse error");
			return;
		}

		String parseResult = "";
		if (parser != null) {
			try {
				if (OtherCacheData.current().isDebugMode) {
					System.out.println(str);
				}
				// 解析JSON
				parseResult = parser.parse(str);
			} catch (IllegalStateException e) {
				e.printStackTrace();
				onError("数据解析出错", parser);
				if (OtherCacheData.current().isDebugMode)
					Log.e("NetServer", "onResponse error");
				return;
			} catch (Exception e) {
				e.printStackTrace();
				onError("数据解析出错", parser);
				if (OtherCacheData.current().isDebugMode)
					Log.e("NetServer", "onResponse error");
				return;
			}
			notifyResponse(parseResult);
		}

	}

}

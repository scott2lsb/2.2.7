package com.sumavision.talktv2.activity;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.sumavision.talktv2.R;
import com.sumavision.talktv2.data.MailData;
import com.sumavision.talktv2.data.OtherCacheData;
import com.sumavision.talktv2.net.JSONMessageType;
import com.sumavision.talktv2.net.MailBoxParser;
import com.sumavision.talktv2.net.MailBoxRequest;
import com.sumavision.talktv2.net.MailListParser;
import com.sumavision.talktv2.net.MailListRequest;
import com.sumavision.talktv2.net.NetServer;
import com.sumavision.talktv2.net.NetServerListener;
import com.sumavision.talktv2.user.User;
import com.sumavision.talktv2.user.UserNow;
import com.sumavision.talktv2.user.UserOther;
import com.umeng.analytics.MobclickAgent;

/**
 * @author 李梦思
 * @createTime 2012-6-18
 * @description 私信界面
 * @changeLog
 */
public class MailActivity extends Activity implements NetServerListener,
		OnItemClickListener, OnClickListener {
	private NetServer server = NetServer.current();

	private ListView mailList;
	private MailListAdapter ma;
	private Button more;
	private LinearLayout bottom;
	private LayoutParams FWlayoutParams = new LinearLayout.LayoutParams(
			LinearLayout.LayoutParams.FILL_PARENT,
			LinearLayout.LayoutParams.WRAP_CONTENT);
	private List<MailData> list;
	private int flag = -1;
	private final int REFRESH = 1;
	private final int DETAIL = 2;
	private final int MORE = 3;

	private int lastItem = 0;

	private FrameLayout all;
	// 通信框
	private ImageView connectBg;
	private RelativeLayout connectFrame;

	private void hidepb() {
		connectBg.setVisibility(View.GONE);
		connectFrame.setVisibility(View.GONE);
	}

	private void showpb() {
		connectBg.setVisibility(View.VISIBLE);
		connectFrame.setVisibility(View.VISIBLE);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mail);
		mailList = (ListView) findViewById(R.id.mail_list);
		connectBg = (ImageView) findViewById(R.id.mail_bg_pic_loading);
		connectBg.setVisibility(View.GONE);
		connectFrame = (RelativeLayout) findViewById(R.id.mail_pb_layout);
		connectFrame.setVisibility(View.GONE);
		more = new Button(this);
		more.setOnClickListener(this);
		more.setId(JSONMessageType.MAILLIST_BTN_MORE);
		more.setText(R.string.more);
		more.setTextSize(20);
		more.setTextColor(Color.parseColor("#6f6f6f"));
		more.setBackgroundResource(R.drawable.playbill_listitem_bg);
		bottom = new LinearLayout(this);
		bottom.addView(more, FWlayoutParams);

		// UserOther.current().userID = getIntent().getIntExtra("id", 0);
		mailList.setOnItemClickListener(this);

		all = (FrameLayout) findViewById(R.id.mail_all);

		findViewById(R.id.mail_back).setOnClickListener(this);
		findViewById(R.id.mail_ok).setOnClickListener(this);
	}

	// handler
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {

			case JSONMessageType.NET_BEGIN:
				UserNow.current().errMsg = JSONMessageType.SERVER_NETFAIL;

				showpb();
				break;
			case JSONMessageType.NET_END:
				hidepb();
				if (msg.obj.equals("")) {
					switch (flag) {
					case REFRESH:
						ma = new MailListAdapter(MailActivity.this, UserNow
								.current().getMail(), mailList);
						mailList.setAdapter(ma);
						int s = UserNow.current().getMail().size();
						if (s < UserNow.current().mailCount)
							if (mailList.getFooterViewsCount() == 0)
								mailList.addFooterView(bottom);
						mailList.setSelection(s - 1);
						break;
					case DETAIL:
						Intent i = new Intent(MailActivity.this,
								UserMailActivity.class);
						startActivity(i);
						break;
					case MORE:
						ma = new MailListAdapter(MailActivity.this, UserNow
								.current().getMail(), mailList);
						mailList.setAdapter(ma);
						if (UserNow.current().getMail().size() >= UserNow
								.current().mailCount)
							mailList.removeFooterView(bottom);
						mailList.setSelection(lastItem);
						break;
					default:
						break;
					}
				} else {
					Toast.makeText(getApplicationContext(),
							UserNow.current().errMsg, Toast.LENGTH_SHORT)
							.show();
					switch (flag) {
					case REFRESH:
						UserNow.current().setMail(list);
						break;
					case MORE:
						OtherCacheData.current().offset = OtherCacheData
								.current().offset
								- OtherCacheData.current().pageCount;
						break;
					default:
						break;
					}
				}
				break;
			default:
				break;
			}
		};
	};

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.mail_back:
			finish();
			break;
		case R.id.mail_ok:
			flag = REFRESH;
			list = UserNow.current().getMail();
			UserNow.current().getMail().clear();
			OtherCacheData.current().offset = 0;
			server.addListener(this);
			server.service(new MailBoxRequest(), new MailBoxParser());
			break;
		case JSONMessageType.MAILLIST_BTN_MORE:
			flag = MORE;
			lastItem = UserNow.current().getMail().size() - 1;
			OtherCacheData.current().offset = OtherCacheData.current().offset
					+ OtherCacheData.current().pageCount;
			server.addListener(this);
			server.service(new MailBoxRequest(), new MailBoxParser());
			break;
		default:
			break;
		}

	}

	private UserOther uo;

	@Override
	public void onBegin() {
		handler.sendEmptyMessage(JSONMessageType.NET_BEGIN);
	}

	@Override
	public void onResponse(String errorMessage) {
		Message msg = new Message();
		msg.what = JSONMessageType.NET_END;
		msg.obj = errorMessage;
		handler.sendMessage(msg);
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// UserOther.current().userID =
		// UserNow.current().getMail().get(arg2).rid;
		// UserOther.current().name =
		// UserNow.current().getMail().get(arg2).rUserName;
		// UserOther.current().iconURL =
		// UserNow.current().getMail().get(arg2).rUserPhoto;
		// UserOther.current().mailCount =
		// UserNow.current().getMail().get(arg2).rid;

		int otherUserID = UserNow.current().getMail().get(arg2).rid;
		uo = new UserOther();
		flag = DETAIL;
		server.addListener(this);
		server.service(new MailListRequest(otherUserID), new MailListParser(uo));
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (User.current().needChangeBackground) {
			if (User.current().sdcardThemeDrawable != null) {
				all.setBackgroundDrawable(User.current().sdcardThemeDrawable);
			} else {
				Log.e("UserCenter", User.current().nowBg + "");
				all.setBackgroundResource(User.current().nowBg);
			}
		}

		MobclickAgent.onResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (connectBg.isShown()) {
				hidepb();
				server.removeListener(this);
				server.cancel();
				return true;
			} else {
				return super.onKeyDown(keyCode, event);
			}
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}

	@Override
	protected void onDestroy() {
		if (UserNow.current().getMail() != null) {
			UserNow.current().getMail().clear();
			UserNow.current().setMail(null);
		}
		OtherCacheData.current().offset = 0;
		OtherCacheData.current().pageCount = 10;
		super.onDestroy();
	}
}

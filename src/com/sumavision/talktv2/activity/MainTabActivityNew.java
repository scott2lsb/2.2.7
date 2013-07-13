package com.sumavision.talktv2.activity;

import android.app.TabActivity;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RadioButton;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;

import com.sumavision.talktv2.R;
import com.sumavision.talktv2.broadcastreceiver.ConnectivityReceiver;
import com.sumavision.talktv2.services.NotificationService;
import com.sumavision.talktv2.user.UserNow;
import com.sumavision.talktv2.utils.AppUtil;
import com.sumavision.talktv2.utils.Constants;
import com.umeng.analytics.MobclickAgent;

public class MainTabActivityNew extends TabActivity implements
		OnCheckedChangeListener {

	public static RadioButton rcmdBtn;
	private RadioButton liveBtn;
	public static RadioButton activitiesBtn;
	private RadioButton fansBtn;
	private RadioButton myBtn;

	public static TabHost tabHost;
	private TabWidget tabWidget;
	private final int TABBOTTOM = 1;

	private Intent rcmdIntent;
	private Intent liveIntent;
	private Intent activitiesIntent;
	private Intent fansIntent;
	private Intent myIntent;

	private SharedPreferences pushMsgpreferences;
	private ConnectivityReceiver receiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		try {
			super.onCreate(savedInstanceState);
		} catch (RuntimeException e) {
			e.printStackTrace();
		}
		setContentView(R.layout.maintabnew);
		setupIntent();
		initViews();
		pushMsgpreferences = getSharedPreferences(Constants.pushMessage, 0);
		if (!AppUtil.isNotifyServiceRunning(this, AppUtil.getPackageName(this)
				+ ".services.NotificationService")) {
			startNotificationService();
		}
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(ConnectivityReceiver.startAction);
		intentFilter.addAction(ConnectivityReceiver.netAction);
		receiver = new ConnectivityReceiver();
		registerReceiver(receiver, intentFilter);
	}

	private void setupIntent() {

		rcmdIntent = new Intent(this, RecommendNewActivity.class);
		liveIntent = new Intent(this, CustomChannelListActivity.class);
		activitiesIntent = new Intent(this, ActivititesActivity.class);
		fansIntent = new Intent(this, FriendActivityNew.class);
		myIntent = new Intent(this, MyActivity.class);
		tabHost = this.getTabHost();

		TabHost localTabHost;
		localTabHost = tabHost;
		localTabHost.addTab(localTabHost.newTabSpec("rcmd")
				.setIndicator("rcmd").setContent(rcmdIntent));
		localTabHost.addTab(localTabHost.newTabSpec("live")
				.setIndicator("live").setContent(liveIntent));
		localTabHost.addTab(localTabHost.newTabSpec("activities")
				.setIndicator("activities").setContent(activitiesIntent));
		localTabHost.addTab(localTabHost.newTabSpec("fan").setIndicator("fan")
				.setContent(fansIntent));
		localTabHost.addTab(localTabHost.newTabSpec("my").setIndicator("my")
				.setContent(myIntent));

		tabWidget = (TabWidget) findViewById(android.R.id.tabs);
		for (int i = 0; i < tabWidget.getChildCount(); i++) {
			tabHost.setPadding(tabHost.getPaddingLeft(),
					tabHost.getPaddingTop(), tabHost.getPaddingRight(),
					tabHost.getPaddingBottom() - TABBOTTOM);

			View v = tabWidget.getChildAt(i);
			v.setPadding(0, 1, 0, 0);

		}
	}

	private void initViews() {

		rcmdBtn = (RadioButton) findViewById(R.id.mt_rc);
		liveBtn = (RadioButton) findViewById(R.id.mt_live);
		activitiesBtn = (RadioButton) findViewById(R.id.mt_activities);
		fansBtn = (RadioButton) findViewById(R.id.mt_fans);
		myBtn = (RadioButton) findViewById(R.id.mt_my);

		rcmdBtn.setOnCheckedChangeListener(this);
		liveBtn.setOnCheckedChangeListener(this);
		activitiesBtn.setOnCheckedChangeListener(this);
		fansBtn.setOnCheckedChangeListener(this);
		myBtn.setOnCheckedChangeListener(this);

		rcmdBtn.setTag(0);
		liveBtn.setTag(1);
		activitiesBtn.setTag(2);
		fansBtn.setTag(3);
		myBtn.setTag(4);

		tipView = (TextView) findViewById(R.id.maintab_pushmessage_hint);
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

		if (isChecked) {
			switch (buttonView.getId()) {
			case R.id.mt_rc:
				currentPosition = 0;
				tabHost.setCurrentTabByTag("rcmd");
				MobclickAgent.onEvent(this, "recommend");
				break;
			case R.id.mt_live:
				currentPosition = 1;
				tabHost.setCurrentTabByTag("live");

				MobclickAgent.onEvent(this, "live");
				break;
			case R.id.mt_activities:
				currentPosition = 2;
				tabHost.setCurrentTabByTag("activities");
				ActivititesActivity.fromMy = 0;
				MobclickAgent.onEvent(this, "activity");
				break;
			case R.id.mt_fans:
				currentPosition = 3;
				tabHost.setCurrentTabByTag("fan");
				MobclickAgent.onEvent(this, "friends");
				break;
			case R.id.mt_my:
				currentPosition = 4;
				tabHost.setCurrentTabByTag("my");
				MobclickAgent.onEvent(this, "my");
				break;
			default:
				break;
			}
			onTagChanged();
		}
	}

	private int currentPosition;

	private void onTagChanged() {

		switch (currentPosition) {
		case 0:
			rcmdBtn.setBackgroundResource(R.drawable.mt_recommend);
			liveBtn.setBackgroundResource(R.drawable.mt_live_normal);
			activitiesBtn.setBackgroundResource(R.drawable.mt_activity_normal);
			fansBtn.setBackgroundResource(R.drawable.mt_friend_normal);
			myBtn.setBackgroundResource(R.drawable.mt_my_normal);
			break;
		case 1:
			rcmdBtn.setBackgroundResource(R.drawable.mt_recommend_normal);
			liveBtn.setBackgroundResource(R.drawable.mt_live);
			activitiesBtn.setBackgroundResource(R.drawable.mt_activity_normal);
			fansBtn.setBackgroundResource(R.drawable.mt_friend_normal);
			myBtn.setBackgroundResource(R.drawable.mt_my_normal);
			break;
		case 2:
			rcmdBtn.setBackgroundResource(R.drawable.mt_recommend_normal);
			liveBtn.setBackgroundResource(R.drawable.mt_live_normal);
			activitiesBtn.setBackgroundResource(R.drawable.mt_activity);
			fansBtn.setBackgroundResource(R.drawable.mt_friend_normal);
			myBtn.setBackgroundResource(R.drawable.mt_my_normal);
			break;
		case 3:
			rcmdBtn.setBackgroundResource(R.drawable.mt_recommend_normal);
			liveBtn.setBackgroundResource(R.drawable.mt_live_normal);
			activitiesBtn.setBackgroundResource(R.drawable.mt_activity_normal);
			fansBtn.setBackgroundResource(R.drawable.mt_friend);
			myBtn.setBackgroundResource(R.drawable.mt_my_normal);
			break;
		case 4:
			rcmdBtn.setBackgroundResource(R.drawable.mt_recommend_normal);
			liveBtn.setBackgroundResource(R.drawable.mt_live_normal);
			activitiesBtn.setBackgroundResource(R.drawable.mt_activity_normal);
			fansBtn.setBackgroundResource(R.drawable.mt_friend_normal);
			myBtn.setBackgroundResource(R.drawable.mt_my);

			break;
		default:
			break;
		}
	}

	private void startNotificationService() {

		Intent intent = new Intent(this, NotificationService.class);
		startService(intent);
	}

	private TextView tipView;

	private void dealPushMsgHint(boolean isShow) {

		if (isShow) {
			tipView.setVisibility(View.VISIBLE);
		} else {
			tipView.setVisibility(View.GONE);
		}
	}

	@Override
	protected void onResume() {

		super.onResume();
		MobclickAgent.onResume(this);
		boolean isShow = pushMsgpreferences.getBoolean("newPushMsg", false);
		dealPushMsgHint(isShow);
		getUserLocalData();
	}

	private void getUserLocalData() {

		SharedPreferences spUser;
		spUser = getSharedPreferences("userInfo", 0);
		UserNow.current().userID = spUser.getInt("userID", 0);
		if (UserNow.current().userID != 0) {
			UserNow.current().nickName = spUser.getString("nickName", "");
			UserNow.current().eMail = spUser.getString("address", "");
			UserNow.current().sessionID = spUser.getString("sessionID", "");
			UserNow.current().checkInCount = spUser.getInt("checkInCount", 0);
			UserNow.current().commentCount = spUser.getInt("commentCount", 0);
			UserNow.current().fansCount = spUser.getInt("fansCount", 0);
			UserNow.current().privateMessageAllCount = spUser.getInt(
					"messageCount", 0);
			UserNow.current().privateMessageOnlyCount = spUser.getInt(
					"messagePeopleCount", 0);

			UserNow.current().iconURL = spUser.getString("iconURL", "");
			UserNow.current().signature = spUser.getString("signature", "");
			UserNow.current().point = (int) spUser.getLong("point", 0);
			UserNow.current().level = spUser.getString("level", "1");
			UserNow.current().gender = spUser.getInt("gender", 1);
			UserNow.current().exp = (int) spUser.getLong("exp", 0);

			UserNow.current().name = spUser.getString("name", "xxx");
			UserNow.current().friendCount = spUser.getInt("friendCount", 0);
			UserNow.current().commentCount = spUser.getInt("commentCount", 0);

		} else {
			UserNow.current().isSelf = false;
		}

	}

	@Override
	public void onPause() {

		super.onPause();
		MobclickAgent.onPause(this);
	}

	@Override
	protected void onDestroy() {
		unregisterReceiver(receiver);
		super.onDestroy();
	}
}

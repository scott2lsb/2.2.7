package com.sumavision.talktv2.activity;

import java.io.IOException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.sumavision.talktv2.R;
import com.sumavision.talktv2.data.ClientData;
import com.sumavision.talktv2.data.ShareData;
import com.sumavision.talktv2.data.SinaData;
import com.sumavision.talktv2.net.JSONMessageType;
import com.sumavision.talktv2.user.UserNow;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.SendMessageToWX;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.tencent.mm.sdk.openapi.WXMediaMessage;
import com.tencent.mm.sdk.openapi.WXTextObject;
import com.umeng.analytics.MobclickAgent;
import com.weibo.sdk.android.Oauth2AccessToken;
import com.weibo.sdk.android.Weibo;
import com.weibo.sdk.android.WeiboAuthListener;
import com.weibo.sdk.android.WeiboDialogError;
import com.weibo.sdk.android.WeiboException;
import com.weibo.sdk.android.api.StatusesAPI;
import com.weibo.sdk.android.net.RequestListener;
import com.weibo.sdk.android.sso.SsoHandler;

/**
 * @author 李梦思
 * @description 分享界面
 * @createTime 2012-11-7
 */
public class ShareToFriendsActivity extends Activity implements
		OnClickListener, RequestListener {

	private final int WEIBOAUTH = 1;
	private final int CONTACTNUMBER = 2;

	private final int SINA_WEIBO_START = 111;
	private final int SINA_WEIBO_OVER = 112;
	private RelativeLayout all;
	// 微博
	private Weibo mWeibo;
	private SsoHandler ssh;
	private StatusesAPI statusesAPI;
	// 微信
	private final String WEIXIN_APP_ID = "wxcfaa020ee248a2f2";
	private IWXAPI mWeixin;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sharetofriends);
		mWeibo = Weibo.getInstance("2064721383", "http://www.tvfan.cn");
		ssh = new SsoHandler(this, mWeibo);
		regToWX();
		initViews();
	}

	/**
	 * 注册到微信
	 */
	private void regToWX() {
		mWeixin = WXAPIFactory.createWXAPI(this, WEIXIN_APP_ID, false);
		mWeixin.registerApp(WEIXIN_APP_ID);
	}

	private void initViews() {
		all = (RelativeLayout) findViewById(R.id.share_2_friend_all);
		all.startAnimation(AnimationUtils.loadAnimation(
				getApplicationContext(), R.anim.open2up));

		close = AnimationUtils.loadAnimation(getApplicationContext(),
				R.anim.activity_close2bottom);
		findViewById(R.id.share_cancel).setOnClickListener(this);
		findViewById(R.id.share_contact).setOnClickListener(this);
		findViewById(R.id.share_weixin).setOnClickListener(this);
		findViewById(R.id.share_sina).setOnClickListener(this);
		findViewById(R.id.s2f_comment_bg).setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.s2f_comment_bg:
			all.startAnimation(close);
			handler.sendEmptyMessageDelayed(MSG_CLOSE_ACTIVITY, 300);
			break;
		case R.id.share_cancel:
			all.startAnimation(close);
			handler.sendEmptyMessageDelayed(MSG_CLOSE_ACTIVITY, 300);
			break;
		case R.id.share_contact:
			startActivityForResult(new Intent(Intent.ACTION_PICK,
					ContactsContract.Contacts.CONTENT_URI), CONTACTNUMBER);
			overridePendingTransition(R.anim.ct_slide_right,
					R.anim.exit_activity_2left);
			break;
		case R.id.share_weixin:
			if (mWeixin == null)
				regToWX();
			sendWeixinMessage();
			overridePendingTransition(R.anim.ct_slide_right,
					R.anim.exit_activity_2left);
			break;
		case R.id.share_sina:
			// overridePendingTransition(R.anim.ct_slide_right,
			// R.anim.exit_activity_2left);
			ssh.authorize(new WeiboAuthListener() {

				@Override
				public void onWeiboException(WeiboException arg0) {

				}

				@Override
				public void onError(WeiboDialogError arg0) {

				}

				@Override
				public void onComplete(Bundle arg0) {
					ClientData c = new ClientData();
					c.token = arg0.getString("access_token");
					c.expire = arg0.getString("expires_in");
					SinaData.weibo().accessToken = new Oauth2AccessToken(
							c.token, c.expire);
					if (SinaData.weibo().accessToken.isSessionValid()) {

						SinaData.isSinaBind = true;

					}
				}

				@Override
				public void onCancel() {

				}
			});
			break;
		default:
			break;
		}
	}

	/**
	 * 发送信息到微信
	 */
	private void sendWeixinMessage() {
		WXTextObject textObject = new WXTextObject();
		WXMediaMessage mediaMessage = new WXMediaMessage();
		textObject.text = ShareData.text;
		mediaMessage.mediaObject = textObject;
		mediaMessage.description = ShareData.text;
		SendMessageToWX.Req req = new SendMessageToWX.Req();
		req.transaction = buildTransaction("text");
		req.message = mediaMessage;
		if (mWeixin.sendReq(req)) {
			Toast.makeText(this, "分享成功", Toast.LENGTH_SHORT).show();
			finish();
		} else
			Toast.makeText(this, "分享失败", Toast.LENGTH_SHORT).show();

	}

	private String buildTransaction(final String type) {
		return (type == null) ? String.valueOf(System.currentTimeMillis())
				: type + System.currentTimeMillis();
	}

	public void sendWeibOnlyTxt(String content) {
		statusesAPI = new StatusesAPI(SinaData.weibo().accessToken);
		statusesAPI.update(content, UserNow.current().lat,
				UserNow.current().lon, this);
	}

	public void sendWeibWithPic(String content, String picPath) {
		statusesAPI = new StatusesAPI(SinaData.weibo().accessToken);
		statusesAPI.upload(content, picPath, UserNow.current().lat,
				UserNow.current().lon, this);
	}

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_CLOSE_ACTIVITY:
				finish();
				break;
			case SINA_WEIBO_START:
				showTipsDialog();
				break;
			case SINA_WEIBO_OVER:
				hideTipsDialog();
				Toast.makeText(getApplicationContext(), "新浪微博发送成功",
						Toast.LENGTH_SHORT).show();
				all.startAnimation(close);
				handler.sendEmptyMessageDelayed(MSG_CLOSE_ACTIVITY, 300);
				break;
			default:
				break;
			}
		};
	};

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case WEIBOAUTH:
				ssh.authorizeCallBack(requestCode, resultCode, data);
				break;
			case CONTACTNUMBER:
				if (data == null) {
					return;
				}
				Uri result = data.getData();
				String phoneNumber = getURINumber(result);
				Uri uri1 = Uri.parse("smsto:" + phoneNumber);
				Intent intent = new Intent(Intent.ACTION_SENDTO, uri1);
				intent.putExtra("sms_body", JSONMessageType.SHARE_MESSAGE);
				startActivity(intent);
				finish();
				break;
			default:
				break;
			}
		} else {
			switch (requestCode) {
			case WEIBOAUTH:

				break;
			case CONTACTNUMBER:

				break;
			default:
				break;
			}
		}
	}

	private String getURINumber(Uri uri) {

		String string = "";
		ContentResolver cr = getContentResolver();
		Cursor cursor = cr.query(uri, null, null, null, null);
		while (cursor.moveToNext()) {
			String contactId = cursor.getString(cursor
					.getColumnIndex(ContactsContract.Contacts._ID));
			Cursor phone = cr.query(
					ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
					ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "="
							+ contactId, null, null);
			while (phone.moveToNext()) {
				int numberFieldColumnIndex = phone
						.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
				String strPhoneNumber = phone.getString(numberFieldColumnIndex);
				string += " " + strPhoneNumber;
			}
			phone.close();
		}
		cursor.close();
		return string;
	}

	private ProgressDialog tipsDialog;

	private void showTipsDialog() {
		if (tipsDialog != null) {
			tipsDialog.dismiss();
			tipsDialog = null;
		}
		tipsDialog = ProgressDialog.show(ShareToFriendsActivity.this, "",
				"处理中，请稍后...", true, true,
				new DialogInterface.OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {
						dialog.dismiss();
					}
				});
	}

	private void hideTipsDialog() {
		if (tipsDialog != null) {
			tipsDialog.dismiss();
			tipsDialog = null;
		}
	}

	private final int MSG_CLOSE_ACTIVITY = 103;
	private Animation close;

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK) {
			all.startAnimation(close);
			handler.sendEmptyMessageDelayed(MSG_CLOSE_ACTIVITY, 300);
			return true;
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}

	@Override
	public void onComplete(String arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onError(WeiboException arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onIOException(IOException arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}
}

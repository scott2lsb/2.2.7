package com.sumavision.talktv2.activity;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sumavision.talktv2.R;
import com.sumavision.talktv2.activity.MyListView.OnRefreshListener;
import com.sumavision.talktv2.data.EventData;
import com.sumavision.talktv2.data.MainPageData;
import com.sumavision.talktv2.data.OtherCacheData;
import com.sumavision.talktv2.net.EventFriendParser;
import com.sumavision.talktv2.net.EventFriendRequest;
import com.sumavision.talktv2.net.EventRoomParser;
import com.sumavision.talktv2.net.EventRoomRequest;
import com.sumavision.talktv2.net.NetConnectionListener;
import com.sumavision.talktv2.net.RecommendUserListParser;
import com.sumavision.talktv2.net.RecommendUserListRequest;
import com.sumavision.talktv2.net.SearchUserParser;
import com.sumavision.talktv2.net.SearchUserRequest;
import com.sumavision.talktv2.task.GetEventFriendTask;
import com.sumavision.talktv2.task.GetEventRoomTask;
import com.sumavision.talktv2.task.GetRecommendUserTask;
import com.sumavision.talktv2.task.SearchUserTask;
import com.sumavision.talktv2.user.User;
import com.sumavision.talktv2.user.UserNow;
import com.sumavision.talktv2.utils.Constants;
import com.sumavision.talktv2.utils.DialogUtil;
import com.umeng.analytics.MobclickAgent;

public class FriendActivityNew extends Activity implements
		OnPageChangeListener, OnClickListener, OnRefreshListener,
		NetConnectionListener, OnItemClickListener, TextWatcher {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.friend_new);
		initOthers();
		initViews();
		setListeners();
		if (UserNow.current().userID == 0) {
			friendViewPager.setCurrentItem(2);
		} else {
			friendViewPager.setCurrentItem(1);
		}
	}

	private ImageLoaderHelper imageLoaderHelper;

	private void initOthers() {
		OtherCacheData.current().offset = 0;
		OtherCacheData.current().pageCount = 10;
		imageLoaderHelper = new ImageLoaderHelper();
	}

	private void setListeners() {
		fellowingTextView.setOnClickListener(this);
		findTextView.setOnClickListener(this);
		allTextView.setOnClickListener(this);
		myFellowingListView.setTag(0);
		myFellowingListView.setOnItemClickListener(this);
		myRecommandListView.setTag(1);
		myRecommandListView.setOnItemClickListener(this);
		myAllListView.setTag(2);
		myAllListView.setOnItemClickListener(this);
	}

	private void initViews() {

		tagLayout = (LinearLayout) findViewById(R.id.tag_layout);
		fellowingTextView = (TextView) findViewById(R.id.following_tag);
		findTextView = (TextView) findViewById(R.id.find_tag);
		allTextView = (TextView) findViewById(R.id.all_tag);
		fellowingTextView.setTag(0);
		findTextView.setTag(1);
		allTextView.setTag(2);

		friendViewPager = (ViewPager) findViewById(R.id.viewPager);
		friendViewPager.setOnPageChangeListener(this);
		initFriendViewPager();
	}

	/*
	 * 表示当前的推荐标签位置
	 */
	private int tagPosition;
	private LinearLayout tagLayout;
	private TextView fellowingTextView;
	private TextView findTextView;
	private TextView allTextView;
	private final int REQUEST_LOGIN = 1;
	private int currentPosition;
	private Button calcel;
	private Button delete;
	private EditText searchInput;

	private RelativeLayout recommendLayout;

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case REQUEST_LOGIN:
				switch (currentPosition) {
				case 0:

					if (fellowingList.size() == 0) {
						getMyFellowingData();
					}
					break;
				case 1:
					if (recommandUserList.size() == 0) {
						getRecommendUserData();
					}
					break;
				default:
					break;
				}
				break;
			default:
				break;
			}
		} else {
			switch (requestCode) {
			case REQUEST_LOGIN:
				// serverHandler.sendEmptyMessageDelayed(DELAY_CHANGE_TAG,
				// 1000);
				break;

			default:
				break;
			}
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	/**
	 * 当标签切换时执行
	 */
	private void onTagSelected(int position, boolean fromViewPager) {
		if (tagPosition != position) {
			tagPosition = position;
			for (int i = 0; i < 3; ++i) {
				TextView textView = (TextView) tagLayout.findViewWithTag(i);
				if (tagPosition == i) {

					textView.setTextColor(getResources()
							.getColor(R.color.white));
					textView.setBackgroundResource(R.drawable.recommand_tag_bg);
				} else {
					textView.setTextColor(getResources().getColor(
							R.color.tag_default));
					textView.setBackgroundDrawable(null);
				}
			}
		}
		if (!fromViewPager) {
			friendViewPager.setCurrentItem(position);
		}
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {

	}

	private void openLoginActivity() {
		Intent intent = new Intent(FriendActivityNew.this, LoginActivity.class);
		startActivityForResult(intent, REQUEST_LOGIN);
	}

	private void processPageNet(int arg0) {
		switch (arg0) {
		case 0:
			MobclickAgent.onEvent(this, "following");
			if (UserNow.current().userID == 0) {
				openLoginActivity();
			} else {
				if (fellowingList.size() == 0) {
					getMyFellowingData();
				}
			}
			break;
		case 1:
			MobclickAgent.onEvent(this, "findfriends");
			if (UserNow.current().userID == 0) {
				openLoginActivity();
			} else {
				if (recommandUserList.size() == 0) {
					getRecommendUserData();
				}
			}
			break;
		case 2:
			MobclickAgent.onEvent(this, "allpeople");
			if (allList.size() == 0) {
				getAllUserList();
			}
			break;
		default:
			break;
		}
	}

	@Override
	public void onPageSelected(int arg0) {
		currentPosition = arg0;
		onTagSelected(arg0, true);
		processPageNet(arg0);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.err_text:
			processPageNet(currentPosition);
			break;
		case R.id.search_delete:
			searchInput.setText("");
			break;
		case R.id.search_cancle:
			calcel.setVisibility(View.GONE);
			delete.setVisibility(View.GONE);
			recommendLayout.setVisibility(View.VISIBLE);
			searchUserListView.setVisibility(View.GONE);
			searchInput.setText("");
			hideSoftPad();
			break;
		case R.id.search_edit:
			if (!calcel.isShown()) {
				calcel.setVisibility(View.VISIBLE);
				delete.setVisibility(View.VISIBLE);
				recommendLayout.setVisibility(View.GONE);
				searchUserListView.setVisibility(View.VISIBLE);
			}
			break;
		case R.id.following_tag:
			onTagSelected(0, false);
			break;
		case R.id.find_tag:
			onTagSelected(1, false);
			break;
		case R.id.all_tag:
			onTagSelected(2, false);
			break;
		default:
			break;
		}
	}

	private ViewPager friendViewPager;

	private void initFriendViewPager() {
		ArrayList<View> views = new ArrayList<View>();
		LayoutInflater inflater = LayoutInflater.from(this);
		View fellowView = inflater.inflate(R.layout.friend_viewpager_following,
				null);
		initFellowingPager(fellowView);

		View findView = inflater.inflate(R.layout.friend_viewpager_find, null);
		initRecommendUserPager(findView);
		View allView = inflater.inflate(R.layout.friend_viewpager_all, null);
		initAllPager(allView);
		views.add(fellowView);
		views.add(findView);
		views.add(allView);
		AwesomeAdapter adapter = new AwesomeAdapter(views);
		friendViewPager.setAdapter(adapter);

	}

	private void initFellowingPager(View view) {
		myFellowingListView = (MyListView) view.findViewById(R.id.listView);
		fellowErrText = (TextView) view.findViewById(R.id.err_text);
		fellowErrText.setOnClickListener(this);
		fellowProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);
		myFellowingListView.setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				getMyFellowingData();
			}

			@Override
			public void onLoadingMore() {
				int start = 0;
				int count = fellowingList.size() + 10;
				getMyFellowingData(start, count);
			}
		});
	}

	private TextView fellowErrText;
	private ProgressBar fellowProgressBar;
	private MyListView myFellowingListView;
	private ArrayList<EventData> fellowingList = new ArrayList<EventData>();

	private GetEventFriendTask getEventFriendTask;

	private void getMyFellowingData() {
		if (getEventFriendTask == null) {
			getEventFriendTask = new GetEventFriendTask(this);
			getEventFriendTask.execute(this, new EventFriendRequest(),
					new EventFriendParser());
			if (fellowingList.size() == 0) {
				fellowErrText.setVisibility(View.GONE);
				fellowProgressBar.setVisibility(View.VISIBLE);
			}
		}
	}

	private void getMyFellowingData(int start, int count) {
		if (getEventFriendTask == null) {
			OtherCacheData.current().offset = start;
			OtherCacheData.current().pageCount = count;
			getEventFriendTask = new GetEventFriendTask(this);
			getEventFriendTask.execute(this, new EventFriendRequest(),
					new EventFriendParser());
		}
	}

	FellowingAdapter fellowAdapter;

	private void updateFellowingListView() {
		ArrayList<EventData> temp = MainPageData.current().myFriendEventDatas;
		if (temp != null) {
			fellowingList = temp;
			if (fellowingList.size() == 0) {
				fellowErrText.setText("暂无粉友");
				fellowErrText.setVisibility(View.VISIBLE);
			} else {
				fellowErrText.setVisibility(View.GONE);
				fellowAdapter = new FellowingAdapter(fellowingList);
				myFellowingListView.setAdapter(fellowAdapter);
			}
		} else {
			fellowErrText.setVisibility(View.VISIBLE);
		}
		fellowProgressBar.setVisibility(View.GONE);
	}

	private void initRecommendUserPager(View view) {
		searchInput = (EditText) view.findViewById(R.id.search_edit);
		searchInput.setClickable(true);
		searchInput.setOnClickListener(this);
		searchInput.addTextChangedListener(this);
		calcel = (Button) view.findViewById(R.id.search_cancle);
		calcel.setOnClickListener(this);
		delete = (Button) view.findViewById(R.id.search_delete);
		delete.setOnClickListener(this);

		recommendLayout = (RelativeLayout) view
				.findViewById(R.id.recommend_list_layout);

		searchUserListView = (ProgramCommentListView) view
				.findViewById(R.id.searchListView);

		myRecommandListView = (MyListView) view.findViewById(R.id.listView);
		recommandErrText = (TextView) view.findViewById(R.id.err_text);
		recommendProgressBar = (ProgressBar) view
				.findViewById(R.id.progressBar);
		myRecommandListView.setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				getRecommendUserData();
			}

			@Override
			public void onLoadingMore() {
				// TODO
				int start = 0;
				int count = recommandUserList.size() + 10;
				getRecommendUserData(start, count);
			}
		});
	}

	private TextView recommandErrText;
	private ProgressBar recommendProgressBar;
	private MyListView myRecommandListView;
	private ArrayList<User> recommandUserList = new ArrayList<User>();

	private GetRecommendUserTask getRecommendUserTask;

	private void getRecommendUserData() {
		if (getRecommendUserTask == null) {
			getRecommendUserTask = new GetRecommendUserTask(this);
			getRecommendUserTask.execute(this, new RecommendUserListRequest(),
					new RecommendUserListParser());

			if (recommandUserList.size() == 0) {
				recommandErrText.setVisibility(View.GONE);
				recommendProgressBar.setVisibility(View.VISIBLE);
			}
		}
	}

	private void getRecommendUserData(int start, int count) {
		if (getRecommendUserTask == null) {
			OtherCacheData.current().offset = start;
			OtherCacheData.current().pageCount = count;
			getRecommendUserTask = new GetRecommendUserTask(this);
			getRecommendUserTask.execute(this, new RecommendUserListRequest(),
					new RecommendUserListParser());

		}
	}

	private ProgramCommentListView searchUserListView;

	private ArrayList<User> searchUserList = new ArrayList<User>();

	private SearchUserTask searchUserTask;

	private void getSearchUserData() {
		if (searchUserTask == null) {
			searchUserTask = new SearchUserTask(this);
			searchUserTask.execute(this, new SearchUserRequest(),
					new SearchUserParser());
		}
	}

	private void getSearchUserData(int start, int count) {
		if (searchUserTask == null) {
			OtherCacheData.current().offset = start;
			OtherCacheData.current().pageCount = count;
			searchUserTask = new SearchUserTask(this);
			searchUserTask.execute(this, new SearchUserRequest(),
					new SearchUserParser());
		}
	}

	private void updateSearchListView() {
		ArrayList<User> temp = (ArrayList<User>) UserNow.current()
				.getSearchUser();
		if (temp != null) {
			searchUserList = temp;
			FindAdapter adapter = new FindAdapter(searchUserList);
			searchUserListView.setAdapter(adapter);
			searchUserListView.setOnItemClickListener(searchListClicked);

		}
	}

	private OnItemClickListener searchListClicked = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			if (arg2 == searchUserList.size()) {
				getSearchUserData(0, arg2 + 10);
				return;
			}
			int fid = searchUserList.get(arg2).userId;
			// UserOther.current().iconURL = searchUserList.get(arg2).iconURL;
			openOtherUserCenterActivity(fid, searchUserList.get(arg2).iconURL);
		}
	};

	private void updateFindListView() {
		searchUserListView.setVisibility(View.GONE);
		recommendLayout.setVisibility(View.VISIBLE);
		ArrayList<User> temp = (ArrayList<User>) UserNow.current().getFriend();
		if (temp != null) {
			recommandUserList = temp;
			if (recommandUserList.size() == 0) {
				recommandErrText.setText("暂无推荐用户");
				recommandErrText.setVisibility(View.VISIBLE);
			} else {
				recommandErrText.setVisibility(View.GONE);
				FindAdapter adapter = new FindAdapter(recommandUserList);
				myRecommandListView.setAdapter(adapter);
			}
		} else {
			recommandErrText.setVisibility(View.VISIBLE);
		}
		recommendProgressBar.setVisibility(View.GONE);
	}

	private void initAllPager(View view) {
		myAllListView = (MyListView) view.findViewById(R.id.listView);
		allErrText = (TextView) view.findViewById(R.id.err_text);
		allProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);
		myAllListView.setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				getAllUserList();
			}

			@Override
			public void onLoadingMore() {
				// TODO
				int start = 0;
				int count = allList.size() + 10;
				getAllUserList(start, count);
			}
		});
	}

	private TextView allErrText;
	private ProgressBar allProgressBar;
	private MyListView myAllListView;
	private ArrayList<EventData> allList = new ArrayList<EventData>();

	private GetEventRoomTask getEventRoomTask;

	private void getAllUserList() {
		if (getEventRoomTask == null) {
			getEventRoomTask = new GetEventRoomTask(this);
			getEventRoomTask.execute(this, new EventRoomRequest(),
					new EventRoomParser());

			if (allList.size() == 0) {
				allErrText.setVisibility(View.GONE);
				allProgressBar.setVisibility(View.VISIBLE);
			}
		}
	}

	private void getAllUserList(int start, int count) {
		if (getEventRoomTask == null) {
			getEventRoomTask = new GetEventRoomTask(this);
			getEventRoomTask.execute(this, new EventRoomRequest(),
					new EventRoomParser());

			if (allList.size() == 0) {
				allErrText.setVisibility(View.GONE);
				allProgressBar.setVisibility(View.VISIBLE);
			}
		}
	}

	private void updateAllListView() {
		ArrayList<EventData> temp = MainPageData.current().eventDatas;
		if (temp != null) {
			allList = temp;
			if (allList.size() == 0) {
				allErrText.setText("暂无信息");
				allErrText.setVisibility(View.VISIBLE);
			} else {
				allErrText.setVisibility(View.GONE);
				AllAdapter adapter = new AllAdapter(allList);
				myAllListView.setAdapter(adapter);
			}
		} else {
			allErrText.setVisibility(View.VISIBLE);
		}
		allProgressBar.setVisibility(View.GONE);
	}

	private class FellowingAdapter extends BaseAdapter {
		private ArrayList<EventData> list;

		public FellowingAdapter(ArrayList<EventData> list) {
			this.list = list;
		}

		@Override
		public int getCount() {
			if (list == null) {
				return 0;
			} else {
				return list.size();
			}
		}

		@Override
		public Object getItem(int position) {
			return list.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder viewHolder;
			if (convertView == null) {
				viewHolder = new ViewHolder();
				LayoutInflater inflater = LayoutInflater
						.from(FriendActivityNew.this);
				convertView = inflater.inflate(
						R.layout.friend_following_list_item, null);
				viewHolder.nameTxt = (TextView) convertView
						.findViewById(R.id.name);
				viewHolder.headPic = (ImageView) convertView
						.findViewById(R.id.pic);
				viewHolder.contentText = (TextView) convertView
						.findViewById(R.id.intro);
				viewHolder.timeText = (TextView) convertView
						.findViewById(R.id.time);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}
			EventData temp = list.get(position);
			String name = temp.userName;
			if (name != null) {
				viewHolder.nameTxt.setText(name);
			}
			String intro = temp.preMsg;
			if (intro != null) {
				viewHolder.contentText.setText(intro);
			}
			String time = temp.createTime;
			if (time != null) {
				viewHolder.timeText.setText(time);
			}
			String url = temp.userPicUrl;
			viewHolder.headPic.setTag(url);
			imageLoaderHelper.loadImage(viewHolder.headPic, url,
					R.drawable.list_headpic_default);
			return convertView;
		}

		public class ViewHolder {
			public TextView nameTxt;
			public ImageView headPic;
			public TextView contentText;
			public TextView timeText;
		}
	}

	private class FindAdapter extends BaseAdapter {
		private ArrayList<User> list;

		public FindAdapter(ArrayList<User> list) {
			this.list = list;
		}

		@Override
		public int getCount() {
			if (list == null) {
				return 0;
			} else {
				return list.size();
			}
		}

		@Override
		public Object getItem(int position) {
			return list.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			FindViewHolder viewHolder;
			if (convertView == null) {
				viewHolder = new FindViewHolder();
				LayoutInflater inflater = LayoutInflater
						.from(FriendActivityNew.this);
				convertView = inflater.inflate(R.layout.friend_find_list_item,
						null);
				viewHolder.nameTxt = (TextView) convertView
						.findViewById(R.id.name);
				viewHolder.introTxt = (TextView) convertView
						.findViewById(R.id.intro);
				viewHolder.headPic = (ImageView) convertView
						.findViewById(R.id.pic);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (FindViewHolder) convertView.getTag();

			}
			User temp = list.get(position);
			String name = temp.name;
			if (name != null) {
				viewHolder.nameTxt.setText(name);
			}
			String intro = temp.signature;
			if (intro != null) {
				viewHolder.introTxt.setText(intro);
			}
			String url = temp.iconURL;
			viewHolder.headPic.setTag(url);
			imageLoaderHelper.loadImage(viewHolder.headPic, url,
					R.drawable.list_headpic_default);
			return convertView;
		}

	}

	static class FindViewHolder {
		public TextView nameTxt;
		public TextView introTxt;
		public ImageView headPic;
	}

	private class AllAdapter extends BaseAdapter {
		private ArrayList<EventData> list;

		public AllAdapter(ArrayList<EventData> list) {
			this.list = list;
		}

		@Override
		public int getCount() {
			if (list == null) {
				return 0;
			} else {
				return list.size();
			}
		}

		@Override
		public Object getItem(int position) {
			return list.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder viewHolder;
			if (convertView == null) {
				viewHolder = new ViewHolder();
				LayoutInflater inflater = LayoutInflater
						.from(FriendActivityNew.this);
				convertView = inflater.inflate(R.layout.friend_all__list_item,
						null);
				viewHolder.nameTxt = (TextView) convertView
						.findViewById(R.id.name);
				viewHolder.intro = (TextView) convertView
						.findViewById(R.id.intro);
				viewHolder.headPic = (ImageView) convertView
						.findViewById(R.id.pic);
				viewHolder.timeText = (TextView) convertView
						.findViewById(R.id.time);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}
			EventData temp = list.get(position);
			String name = temp.userName;
			if (name != null) {
				viewHolder.nameTxt.setText(name);
			}
			String intro = temp.preMsg;
			if (intro != null) {
				viewHolder.intro.setText(intro);
			}
			String time = temp.createTime;
			if (time != null) {
				viewHolder.timeText.setText(time);
			}
			String url = temp.userPicUrl;
			imageLoaderHelper.loadImage(viewHolder.headPic, url,
					R.drawable.list_headpic_default);
			return convertView;
		}

		public class ViewHolder {
			public TextView nameTxt;
			public TextView intro;
			public ImageView headPic;
			public TextView timeText;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {

		if (UserNow.current().userID == 0) {
			openLoginActivity();
		} else {

			switch ((Integer) parent.getTag()) {
			case 0:
				if (position - 1 == fellowingList.size() || position - 1 < 0) {
					return;
				}
				int fid = fellowingList.get(position - 1).userId;
				// UserOther.current().iconURL = fellowingList.get(position -
				// 1).userPicUrl;
				openOtherUserCenterActivity(fid,
						fellowingList.get(position - 1).userPicUrl);
				break;
			case 1:
				if (position - 1 == recommandUserList.size()
						|| position - 1 < 0) {
					return;
				}
				int rId = recommandUserList.get(position - 1).userId;
				openOtherUserCenterActivity(rId,
						recommandUserList.get(position - 1).iconURL);
				break;
			case 2:
				if (position - 1 == allList.size() || position - 1 < 0) {
					return;
				}
				int uId = allList.get(position - 1).userId;
				// UserOther.current().iconURL = allList.get(position -
				// 1).userPicUrl;
				openOtherUserCenterActivity(uId,
						allList.get(position - 1).userPicUrl);
				break;
			default:
				break;
			}
		}
	}

	private void openOtherUserCenterActivity(int id, String iconURL) {
		Intent intent = new Intent(this, OtherUserCenterActivity.class);
		intent.putExtra("id", id);
		intent.putExtra("iconURL", iconURL);

		startActivity(intent);
	}

	@Override
	public void onRefresh() {

	}

	@Override
	public void onLoadingMore() {
		// TODO
	}

	@Override
	public void onNetBegin(String method) {

	}

	@Override
	public void onNetEnd(String msg, String method) {
		if ("eventFriendList".equals(method)) {
			if (msg != null && "".equals(msg)) {
				updateFellowingListView();
			} else {
				fellowProgressBar.setVisibility(View.GONE);
				if (fellowingList.size() == 0) {
					fellowErrText.setVisibility(View.VISIBLE);
				}
				myFellowingListView.onLoadError();
			}
			getEventFriendTask = null;
		} else if ("recommendUserList".equals(method)) {
			if (msg != null && "".equals(msg)) {
				updateFindListView();
			} else {
				recommendProgressBar.setVisibility(View.GONE);
				if (recommandUserList.size() == 0) {
					recommandErrText.setVisibility(View.VISIBLE);
				}
				myRecommandListView.onLoadError();
			}
			getRecommendUserTask = null;
		} else if ("eventRoomList".equals(method)) {
			if (msg != null && "".equals(msg)) {
				updateAllListView();
			} else {
				allProgressBar.setVisibility(View.GONE);
				if (allList.size() == 0) {
					allErrText.setVisibility(View.VISIBLE);
				}
				myAllListView.onLoadError();
			}
			getEventRoomTask = null;
		} else if (Constants.searchUser.equals(method)) {
			if (msg != null && "".equals(msg)) {
				updateSearchListView();
			} else {
				DialogUtil.alertToast(getApplicationContext(), "查询失败");
			}
			searchUserTask = null;
		}
	}

	@Override
	public void onNetEnd(String msg, String method, int type) {

	}

	@Override
	public void onCancel(String method) {

	}

	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
		if (OtherCacheData.isNeedUpdateFriendActivity) {
			fellowingList.clear();
			if (fellowAdapter != null)
				fellowAdapter.notifyDataSetChanged();
			getMyFellowingData();
			OtherCacheData.isNeedUpdateFriendActivity = false;
		}
		if (UserNow.current().userID == 0) {
			friendViewPager.setCurrentItem(2);
		}
	}

	private void hideSoftPad() {

		InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
		inputMethodManager.hideSoftInputFromWindow(FriendActivityNew.this
				.getCurrentFocus().getWindowToken(),
				InputMethodManager.HIDE_NOT_ALWAYS);

	}

	@Override
	public void afterTextChanged(Editable arg0) {

		String keywords = arg0.toString().trim();
		if (!keywords.equals("")) {
			recommendLayout.setVisibility(View.GONE);
			User.current().name = keywords;
			if (searchUserTask != null) {
				searchUserTask.cancel(true);
				searchUserTask = null;
			}
			getSearchUserData();

		}
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		// TODO Auto-generated method stub

	}

	protected void dialog() {
		AlertDialog.Builder builder = new Builder(this);
		builder.setTitle("确定要退出吗?");
		builder.setCancelable(false);
		builder.setIcon(R.drawable.icon_small);
		builder.setPositiveButton("确定",
				new android.content.DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						finish();
					}
				});
		builder.setNegativeButton("取消",
				new android.content.DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
		builder.create().show();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			dialog();
			return true;
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}
}

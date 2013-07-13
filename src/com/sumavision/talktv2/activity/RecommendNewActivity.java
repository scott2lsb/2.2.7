package com.sumavision.talktv2.activity;

import io.vov.utils.Log;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v4.view.ViewPager.SimpleOnPageChangeListener;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sumavision.talktv2.R;
import com.sumavision.talktv2.activity.FocusGallery.OnItemSelectionChangeListener;
import com.sumavision.talktv2.activity.MyListView.OnRefreshListener;
import com.sumavision.talktv2.data.ColumnData;
import com.sumavision.talktv2.data.OtherCacheData;
import com.sumavision.talktv2.data.RecommendData;
import com.sumavision.talktv2.data.RecommendPageData;
import com.sumavision.talktv2.data.VodProgramData;
import com.sumavision.talktv2.net.NetConnectionListener;
import com.sumavision.talktv2.net.RecommendPageParser;
import com.sumavision.talktv2.net.RecommendPageRequest;
import com.sumavision.talktv2.net.RecommendProgramListParser;
import com.sumavision.talktv2.net.RecommendProgramListRequest;
import com.sumavision.talktv2.net.RecommendVodProgramListParser;
import com.sumavision.talktv2.net.RecommendVodProgramListRequest;
import com.sumavision.talktv2.net.SubColumnListParser;
import com.sumavision.talktv2.net.SubColumnListRequest;
import com.sumavision.talktv2.task.ColumnVideoListTask;
import com.sumavision.talktv2.task.GetSpecialDataTask;
import com.sumavision.talktv2.task.RecommandDetailTask;
import com.sumavision.talktv2.task.RecommandHotTask;
import com.sumavision.talktv2.task.RecommendVodProgramTask;
import com.sumavision.talktv2.task.SubColumnTask;
import com.sumavision.talktv2.utils.CommonUtils;
import com.umeng.analytics.MobclickAgent;

public class RecommendNewActivity extends Activity implements
		OnPageChangeListener, OnClickListener, NetConnectionListener,
		OnItemClickListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.recommend_new);
		initUtils();
		initViews();
		setListeners();
		// 加载页面
		getRecommandDetail();
	}

	private AsyncImageLoader imageLoader;
	private ImageLoaderHelper imageLoaderHelper;

	private void initUtils() {
		imageLoader = new AsyncImageLoader();
		OtherCacheData.current().offset = 0;
		OtherCacheData.current().pageCount = 15;
		imageLoaderHelper = new ImageLoaderHelper();
		fadeOutAnim = AnimationUtils.loadAnimation(this, R.anim.fade_out);
		leftInAnim = AnimationUtils.loadAnimation(this, R.anim.left_in);
		rightInAnim = AnimationUtils.loadAnimation(this, R.anim.right_in);
	}

	private void setListeners() {

		hotListView.setOnRefreshListener(hotRefreshListener);
		tvListView.setOnRefreshListener(tvRefreshListener);
		overseaListView.setOnRefreshListener(overseaOnRefreshListener);
		varietyListView.setOnRefreshListener(varietyRefreshListener);
		specialListView.setOnRefreshListener(specialOnRefreshListener);

		hotListView.setTag(0);
		tvListView.setTag(1);
		varietyListView.setTag(2);
		overseaListView.setTag(3);
		specialListView.setTag(4);

		hotListView.setOnItemClickListener(this);
		tvListView.setOnItemClickListener(this);
		overseaListView.setOnItemClickListener(this);
		varietyListView.setOnItemClickListener(this);
		specialListView.setOnItemClickListener(this);

		hotErr.setOnClickListener(errOnClickListener);
		tvErr.setOnClickListener(errOnClickListener);
		varietyErr.setOnClickListener(errOnClickListener);
		overseaErr.setOnClickListener(errOnClickListener);
		specialErr.setOnClickListener(errOnClickListener);
		hotErr.setTag(0);
		tvErr.setTag(1);
		varietyErr.setTag(2);
		overseaErr.setTag(3);
		specialErr.setTag(4);

		findViewById(R.id.playhistory).setOnClickListener(this);
		findViewById(R.id.search).setOnClickListener(this);
		title.setOnClickListener(this);
		typeLayout.setOnClickListener(this);
	}

	private TextView title;
	private RelativeLayout typeLayout;

	private void initViews() {
		title = (TextView) findViewById(R.id.title);
		tagLayout = (ListView) findViewById(R.id.tag_layout);
		typeLayout = (RelativeLayout) findViewById(R.id.type_layout);
		programViewPager = (ViewPager) findViewById(R.id.program_viewpager);
		initProgramViewPager();

	}

	private ViewPager programViewPager;
	private RecommendListView hotListView;
	private MyListView tvListView;
	private MyListView varietyListView;
	private MyListView overseaListView;
	private MyListView specialListView;

	private ProgressBar hotProgressBar;
	private ProgressBar tvProgressBar;
	private ProgressBar varietyProgressBar;
	private ProgressBar overseaProgressBar;
	private ProgressBar specialProgressBar;

	private boolean hotProgressHide;

	private TextView hotErr;
	private TextView tvErr;
	private TextView varietyErr;
	private TextView overseaErr;
	private TextView specialErr;

	private void initProgramViewPager() {
		LayoutInflater inflater = LayoutInflater.from(this);
		View hotView = inflater.inflate(
				R.layout.rcmd_recommend_program_viewpager_item, null);
		hotListView = (RecommendListView) hotView.findViewById(R.id.listView);
		hotErr = (TextView) hotView.findViewById(R.id.err_text);
		hotProgressBar = (ProgressBar) hotView.findViewById(R.id.progressBar);
		View headerView = inflater
				.inflate(R.layout.rcmd_focus_pic_layout, null);
		hotListView.addHeaderView(headerView);
		picViewPager = (FocusGallery) headerView.findViewById(R.id.pic_view);
		picStarsLayout = (LinearLayout) headerView.findViewById(R.id.pic_star);
		picTitleTextView = (TextView) headerView.findViewById(R.id.pic_title);
		// picViewPager
		// .setOnPageChangeListener(picViewPagerOnPageSelectedListener);
		picViewPager.setOnItemClickListener(focusItemClickListener);
		// picViewPager.setOnItemSelectionChangeListener(selectionChangeListener);
		picViewPager.setOnItemSelectedListener(focusItemSelectedListener);
		// 去抖动
		picViewPager.setAnimationDuration(0);
		picViewPager.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					serverHandler.removeMessages(AUTO_CHANGE_FOCUS_PIC);
					break;
				case MotionEvent.ACTION_UP:
					serverHandler.sendEmptyMessageDelayed(
							AUTO_CHANGE_FOCUS_PIC, AUTO_CHANGE_FOCUS_PIC_TIME);
					break;
				default:
					break;
				}
				return false;
			}
		});

		hotListView.setViewPager(programViewPager);
		View tvView = inflater.inflate(R.layout.rcmd_program_viewpager_item,
				null);
		tvListView = (MyListView) tvView.findViewById(R.id.listView);
		tvErr = (TextView) tvView.findViewById(R.id.err_text);
		tvProgressBar = (ProgressBar) tvView.findViewById(R.id.progressBar);

		View varietytView = inflater.inflate(
				R.layout.rcmd_program_viewpager_item, null);
		varietyListView = (MyListView) varietytView.findViewById(R.id.listView);
		varietyErr = (TextView) varietytView.findViewById(R.id.err_text);
		varietyProgressBar = (ProgressBar) varietytView
				.findViewById(R.id.progressBar);

		View overseaView = inflater.inflate(
				R.layout.rcmd_program_viewpager_item, null);
		overseaListView = (MyListView) overseaView.findViewById(R.id.listView);
		overseaErr = (TextView) overseaView.findViewById(R.id.err_text);
		overseaProgressBar = (ProgressBar) overseaView
				.findViewById(R.id.progressBar);

		View specialView = inflater.inflate(
				R.layout.rcmd_program_viewpager_item, null);
		specialListView = (MyListView) specialView.findViewById(R.id.listView);
		specialErr = (TextView) specialView.findViewById(R.id.err_text);
		specialProgressBar = (ProgressBar) specialView
				.findViewById(R.id.progressBar);

		ArrayList<View> programViews = new ArrayList<View>();
		programViews.add(hotView);
		programViews.add(tvView);
		programViews.add(varietytView);
		programViews.add(overseaView);
		programViews.add(specialView);
		AwesomeAdapter programAwesomeAdapter = new AwesomeAdapter(programViews);
		programViewPager.setAdapter(programAwesomeAdapter);
		programViewPager.setOnPageChangeListener(this);
	}

	SimpleOnPageChangeListener picViewPagerOnPageSelectedListener = new SimpleOnPageChangeListener() {
		@Override
		public void onPageSelected(int position) {
			resetAutoChangeRecommand();
			try {
				onPicSelected(position);
			} catch (NullPointerException e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		};
	};

	private void onPicSelected(int position) {
		int size = list.size();
		for (int i = 0; i < size; i++) {
			ImageView imageView = (ImageView) picStarsLayout.findViewWithTag(i)
					.findViewById(R.id.imageView);
			if (i == position) {
				imageView.setImageResource(R.drawable.rcmd_pic_star_focus);
			} else {
				imageView.setImageDrawable(null);
			}
		}
		picTitleTextView.setText(list.get(position).name);
	}

	/*
	 * 表示当前的推荐标签位置
	 */
	private int tagPosition;
	private ListView tagLayout;

	/**
	 * 当标签切换时执行
	 */
	private void onTagSelected(int position, boolean fromViewPager) {
		if (tagPosition != position) {
			if (columnDatas != null && columnDatas.size() > 0) {
				switch (position) {
				case 0:
					if (hotList.size() == 0) {
						getHotList();
					}
					break;
				case 1:
					MobclickAgent.onEvent(RecommendNewActivity.this, "tv");
					if (tvList.size() == 0) {
						int cid = ((ArrayList<ColumnData>) RecommendPageData
								.current().getColumn()).get(position).id;
						getTvList(cid, 0, 10);
					}
					tvListView.checkState();
					break;
				case 2:
					MobclickAgent.onEvent(RecommendNewActivity.this, "variety");
					if (varietyList.size() == 0) {
						int cid = ((ArrayList<ColumnData>) RecommendPageData
								.current().getColumn()).get(position).id;
						getVarietyList(cid, 0, 10);
					}
					varietyListView.checkState();
					break;
				case 3:
					MobclickAgent.onEvent(RecommendNewActivity.this, "search");
					if (overseaList.size() == 0) {
						int cid = ((ArrayList<ColumnData>) RecommendPageData
								.current().getColumn()).get(position).id;
						getOverseaList(cid, 0, 10);
					}
					overseaListView.checkState();
					break;
				case 4:
					MobclickAgent.onEvent(RecommendNewActivity.this, "topic");
					if (specialList.size() == 0) {
						int cid = ((ArrayList<ColumnData>) RecommendPageData
								.current().getColumn()).get(position).id;
						getSpecialListNew(cid, 0, 10);
					}
					specialListView.checkState();
					break;
				default:
					break;
				}
				tagPosition = position;
				tagAdapter.notifyDataSetChanged();
				tagLayout.setVisibility(View.GONE);
				typeLayout.setVisibility(View.GONE);
				title.setText(columnDatas.get(position).name);
			}
		}
		if (!fromViewPager) {
			programViewPager.setCurrentItem(position);
		}
	}

	private boolean isScrolling;
	private int lastScrollPosition;
	private boolean scollLeft;

	@Override
	public void onPageScrollStateChanged(int arg0) {
		// if (arg0 == 1) {
		// isScrolling = true;
		// } else
		// isScrolling = false;
		// lastScrollPosition = 0;
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
		// if (isScrolling) {
		// if (lastScrollPosition > arg2) {
		// scollLeft = false;
		// } else {
		// scollLeft = true;
		// }
		// lastScrollPosition = arg2;
		// }
	}

	@Override
	public void onPageSelected(int arg0) {
		if (!isFromTagListItemClick) {
			// TODO
			int animType = 0;
			if (scollLeft) {
				animType = 1;
			} else {
				animType = -1;
			}
			try {
				changeViewText(arg0, animType);
			} catch (NullPointerException e) {
				e.printStackTrace();
			}
		} else {
			isFromTagListItemClick = false;
		}
		onTagSelected(arg0, true);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.playhistory:
			MobclickAgent.onEvent(this, "history");
			openPlayHistoryActivity();
			break;
		case R.id.search:
			MobclickAgent.onEvent(this, "search");
			openSearchProgramActivity();
			break;
		case R.id.title:
			onTitleClick();
			break;
		case R.id.type_layout:
			tagLayout.setVisibility(View.GONE);
			typeLayout.setVisibility(View.GONE);
			break;
		default:
			break;
		}
	}

	private void onTitleClick() {
		if (columnDatas != null && columnDatas.size() > 0) {
			if (typeLayout.isShown()) {
				tagLayout.setVisibility(View.GONE);
				typeLayout.setVisibility(View.GONE);
			} else {
				tagLayout.setVisibility(View.VISIBLE);
				typeLayout.setVisibility(View.VISIBLE);
			}
		}
	}

	private final OnClickListener errOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch ((Integer) v.getTag()) {
			case 0:
				getHotList();
				break;
			case 1:
				int id = columnDatas.get(0).id;
				getTvList(id, 0, 15);
				break;
			case 2:
				getVarietyList(columnDatas.get(1).id, 0, 15);
				break;
			case 3:
				getOverseaList(columnDatas.get(2).id, 0, 15);
				break;
			case 4:
				getSpecialListNew(columnDatas.get(3).id, 0, 15);
				break;
			default:
				break;
			}
		}
	};

	private void openPlayHistoryActivity() {
		Intent intent = new Intent(this, PlayHistoryActivity.class);
		startActivity(intent);
	}

	private void openSearchProgramActivity() {
		Intent intent = new Intent(this, SearchActivity.class);
		startActivity(intent);
	}

	private RecommandDetailTask recommandDetailTask;

	private void getRecommandDetail() {
		if (recommandDetailTask == null) {
			recommandDetailTask = new RecommandDetailTask();
			recommandDetailTask.execute(this, this, new RecommendPageRequest(),
					new RecommendPageParser());
		}
	}

	private RecommandHotTask recommandHotTask;

	private com.sumavision.talktv2.activity.RecommendListView.OnRefreshListener hotRefreshListener = new com.sumavision.talktv2.activity.RecommendListView.OnRefreshListener() {
		@Override
		public void onRefresh() {
			getHotList();
		}

		@Override
		public void onLoadingMore() {
			// TODO
			int start = 0;
			int count = hotList.size() + 20;
			getHotList(start, count);
		}

	};

	private void getHotList() {
		if (recommandHotTask == null) {
			recommandHotTask = new RecommandHotTask();
			recommandHotTask.execute(this, this,
					new RecommendProgramListRequest(),
					new RecommendProgramListParser());
			if (!hotProgressHide) {
				hotProgressBar.setVisibility(View.VISIBLE);
			}
			hotErr.setVisibility(View.GONE);
		}
	}

	private RecommendVodProgramTask recommendVodTask;

	private void getHotList(int start, int count) {
		if (recommendVodTask == null) {
			OtherCacheData.current().offset = start;
			OtherCacheData.current().pageCount = count;
			recommendVodTask = new RecommendVodProgramTask();
			recommendVodTask.execute(this, this,
					new RecommendVodProgramListRequest(),
					new RecommendVodProgramListParser());
			hotErr.setVisibility(View.GONE);
		}
	}

	private ArrayList<VodProgramData> hotList;
	private FocusGallery picViewPager;
	private LinearLayout picStarsLayout;
	private TextView picTitleTextView;
	// 推荐页焦点大图数量
	private int bigPicSize = 0;
	// 推荐页焦点大图当前被选中的位置
	private int bigPicPosition = 0;
	// 自动切换焦点图
	private final int AUTO_CHANGE_FOCUS_PIC = 5;
	// 自动切换焦点图时间
	private final int AUTO_CHANGE_FOCUS_PIC_TIME = 4000;

	private void updateHotProgramList() {
		list = (ArrayList<RecommendData>) RecommendPageData.current()
				.getRecommend();
		if (list != null) {
			// ArrayList<View> views = new ArrayList<View>();
			LayoutInflater inflater = LayoutInflater.from(this);
			// int y = -1;
			// for (RecommendData rData : list) {
			// FrameLayout frame = (FrameLayout) inflater.inflate(
			// R.layout.rcmd_pic_item, null);
			// frame.setTag(++y);
			// ImageView imageView = (ImageView) frame
			// .findViewById(R.id.imageView);
			// imageView.setScaleType(ScaleType.CENTER_CROP);
			// loadImage(imageView, rData.pic,
			// R.drawable.recommend_pic_default);
			// frame.setOnClickListener(recommendClick);
			// views.add(frame);
			// }
			// AwesomeAdapter adapter = new AwesomeAdapter(views);
			GalleryAdapter adapter = new GalleryAdapter(this, list);
			picViewPager.setAdapter(adapter);
			bigPicSize = list.size();
			if (bigPicSize > 0) {
				picStarsLayout.removeAllViews();
			}
			for (int i = 0; i < bigPicSize; i++) {
				FrameLayout frame = (FrameLayout) inflater.inflate(
						R.layout.rcmd_pic_star, null);
				frame.setTag(i);
				if (i == 0) {
					((ImageView) frame.findViewById(R.id.imageView))
							.setImageResource(R.drawable.rcmd_pic_star_focus);
				}
				picStarsLayout.addView(frame);
			}
			picTitleTextView.setText(list.get(0).name);
			// picViewPager.setOnClickListener(recommendClick);
			serverHandler.sendEmptyMessageDelayed(AUTO_CHANGE_FOCUS_PIC,
					AUTO_CHANGE_FOCUS_PIC_TIME);
		}

		hotList = new ArrayList<VodProgramData>();
		ArrayList<VodProgramData> temp = (ArrayList<VodProgramData>) RecommendPageData
				.current().getLiveProgram();
		if (temp != null && temp.size() > 0) {
			hotList.addAll(temp);
		}
		temp = (ArrayList<VodProgramData>) RecommendPageData.current()
				.getVodProgram();
		if (temp != null && temp.size() > 0) {
			hotList.addAll(temp);
		}
		if (hotList.size() != 0) {
			HotProgramListAdapter adapter = new HotProgramListAdapter(hotList);
			hotListView.setAdapter(adapter);
			hotErr.setVisibility(View.GONE);
			hotProgressHide = true;
		} else {
			hotErr.setVisibility(View.VISIBLE);
			hotProgressHide = false;
		}
		hotProgressBar.setVisibility(View.GONE);
	}

	private OnRefreshListener tvRefreshListener = new OnRefreshListener() {
		@Override
		public void onRefresh() {
			int id = ((ArrayList<ColumnData>) RecommendPageData.current()
					.getColumn()).get(1).id;
			getTvList(id, 0, 10);
		}

		@Override
		public void onLoadingMore() {
			// TODO
			int id = ((ArrayList<ColumnData>) RecommendPageData.current()
					.getColumn()).get(1).id;
			int count = tvList.size() + 20;
			getTvList(id, 0, count);
		}
	};

	private static final int TYPE_TV = 0;
	private static final int TYPE_VARIETY = 1;
	private static final int TYPE_OVERSEA = 2;
	private int type;

	private ColumnVideoListTask recommandTvTask;
	private final ArrayList<VodProgramData> tvList = new ArrayList<VodProgramData>();

	/**
	 * 加载第二标签的内容
	 * 
	 * @param id
	 * @param offset
	 * @param count
	 */
	private void getTvList(int id, int offset, int count) {
		type = TYPE_TV;
		if (recommandTvTask == null) {
			recommandTvTask = new ColumnVideoListTask();
			recommandTvTask.execute(this, this, tvList, id, offset, count,
					TYPE_TV);
			if (tvList.size() == 0) {
				tvProgressBar.setVisibility(View.VISIBLE);
			}
			tvErr.setVisibility(View.GONE);
		}
	}

	private void updateTvProgramList() {
		if (tvList.size() != 0) {
			tvProgressBar.setVisibility(View.GONE);
			VodProgramListViewAdapter adapter = new VodProgramListViewAdapter(
					tvList);
			tvListView.setAdapter(adapter);
		} else {
			tvErr.setVisibility(View.VISIBLE);
		}

	}

	private ColumnVideoListTask recommandVarietyTask;
	private final ArrayList<VodProgramData> varietyList = new ArrayList<VodProgramData>();

	private final OnRefreshListener varietyRefreshListener = new OnRefreshListener() {
		@Override
		public void onRefresh() {
			int id = ((ArrayList<ColumnData>) RecommendPageData.current()
					.getColumn()).get((Integer) varietyListView.getTag()).id;
			getVarietyList(id, 0, 10);
		}

		@Override
		public void onLoadingMore() {
			int id = ((ArrayList<ColumnData>) RecommendPageData.current()
					.getColumn()).get((Integer) varietyListView.getTag()).id;
			int count = varietyList.size() + 20;
			getVarietyList(id, 0, count);
		}
	};

	/**
	 * 加载第3标签的内容
	 * 
	 * @param id
	 * @param offset
	 * @param count
	 */
	private void getVarietyList(int id, int offset, int count) {
		type = TYPE_VARIETY;
		if (recommandVarietyTask == null) {
			recommandVarietyTask = new ColumnVideoListTask();
			recommandVarietyTask.execute(this, this, varietyList, id, offset,
					count, TYPE_VARIETY);
			if (varietyList.size() == 0) {
				varietyProgressBar.setVisibility(View.VISIBLE);
			}
			varietyErr.setVisibility(View.GONE);
		}
	}

	private void updateVarietyProgramList() {
		if (varietyList.size() != 0) {
			varietyProgressBar.setVisibility(View.GONE);
			VodProgramListViewAdapter adapter = new VodProgramListViewAdapter(
					varietyList);
			varietyListView.setAdapter(adapter);
		} else {
			varietyErr.setVisibility(View.VISIBLE);
		}
	}

	private ColumnVideoListTask recommandOveseaTask;
	private final ArrayList<VodProgramData> overseaList = new ArrayList<VodProgramData>();

	private final OnRefreshListener overseaOnRefreshListener = new OnRefreshListener() {
		@Override
		public void onRefresh() {
			int id = ((ArrayList<ColumnData>) RecommendPageData.current()
					.getColumn()).get((Integer) overseaListView.getTag()).id;
			getOverseaList(id, 0, 10);
		}

		@Override
		public void onLoadingMore() {
			// TODO
			int id = ((ArrayList<ColumnData>) RecommendPageData.current()
					.getColumn()).get((Integer) overseaListView.getTag()).id;
			int count = overseaList.size() + 20;
			getOverseaList(id, 0, count);
		}
	};

	/**
	 * 加载第4标签的内容
	 * 
	 * @param id
	 * @param offset
	 * @param count
	 */
	private void getOverseaList(int id, int offset, int count) {
		type = TYPE_VARIETY;
		if (recommandOveseaTask == null) {
			recommandOveseaTask = new ColumnVideoListTask();
			recommandOveseaTask.execute(this, this, overseaList, id, offset,
					count, TYPE_OVERSEA);
			if (overseaList.size() == 0) {
				overseaProgressBar.setVisibility(View.VISIBLE);
			}
			overseaErr.setVisibility(View.GONE);
		}
	}

	private void updateOverseaProgramList() {
		if (overseaList.size() != 0) {
			overseaProgressBar.setVisibility(View.GONE);
			VodProgramListViewAdapter adapter = new VodProgramListViewAdapter(
					overseaList);
			overseaListView.setAdapter(adapter);
		} else {
			overseaErr.setVisibility(View.VISIBLE);
		}
	}

	private SubColumnTask recommandSpecialTask;
	private ArrayList<ColumnData> specialList = new ArrayList<ColumnData>();
	private final OnRefreshListener specialOnRefreshListener = new OnRefreshListener() {
		@Override
		public void onRefresh() {
			int id = ((ArrayList<ColumnData>) RecommendPageData.current()
					.getColumn()).get(4).id;
			getSpecialListNew(id, 0, 10);
		}

		@Override
		public void onLoadingMore() {
			// TODO
			int id = ((ArrayList<ColumnData>) RecommendPageData.current()
					.getColumn()).get(4).id;
			int count = specialList.size() + 20;
			getSpecialListNew(id, 0, count);
		}
	};

	/**
	 * 加载第5标签的内容
	 * 
	 * @param id
	 * @param offset
	 * @param count
	 */
	private void getSpecialList(int id, int offset, int count) {
		if (recommandSpecialTask == null) {
			recommandSpecialTask = new SubColumnTask();

			recommandSpecialTask.execute(this, this, new SubColumnListRequest(
					id, offset, count), new SubColumnListParser());
			if (specialList.size() == 0) {
				specialProgressBar.setVisibility(View.VISIBLE);
			}
			specialErr.setVisibility(View.GONE);
		}
	}

	private GetSpecialDataTask getSpecicalDataTask;

	private void getSpecialListNew(int id, int offset, int count) {
		if (getSpecicalDataTask == null) {
			getSpecicalDataTask = new GetSpecialDataTask(this);
			tempSpeicalList = new ArrayList<ColumnData>();
			getSpecicalDataTask.execute(this, new SubColumnListRequest(id,
					offset, count), tempSpeicalList);
			if (specialList.size() == 0) {
				specialProgressBar.setVisibility(View.VISIBLE);
			}
			specialErr.setVisibility(View.GONE);
		}
	}

	private void updateSpecialProgramList() {
		ArrayList<ColumnData> temp = (ArrayList<ColumnData>) RecommendPageData
				.current().getSubColumn();
		if (temp != null && temp.size() > 0) {
			specialList = temp;
			SpecialProgramListViewAdapter adapter = new SpecialProgramListViewAdapter(
					specialList);
			specialListView.setAdapter(adapter);
		}
		if (specialList.size() == 0) {
			specialErr.setVisibility(View.VISIBLE);
		} else {
			specialErr.setVisibility(View.GONE);
		}
		specialProgressBar.setVisibility(View.GONE);
	}

	private void updateSpecialProgramListNew() {
		ArrayList<ColumnData> temp = tempSpeicalList;
		if (temp != null && temp.size() > 0) {
			specialList = temp;
			SpecialProgramListViewAdapter adapter = new SpecialProgramListViewAdapter(
					specialList);
			specialListView.setAdapter(adapter);
		}
		if (specialList.size() == 0) {
			specialErr.setVisibility(View.VISIBLE);
		} else {
			specialErr.setVisibility(View.GONE);
		}
		specialProgressBar.setVisibility(View.GONE);
	}

	private ArrayList<ColumnData> columnDatas;
	private TagAdapter tagAdapter;

	private void updateTagLayout() {
		columnDatas = (ArrayList<ColumnData>) RecommendPageData.current()
				.getColumn();
		ColumnData columnData = new ColumnData();
		columnData.id = -1;
		columnData.name = "推荐";
		columnDatas.add(0, columnData);
		tagAdapter = new TagAdapter(columnDatas);
		tagLayout.setAdapter(tagAdapter);
		tagLayout.setOnItemClickListener(tagOnItemClickListener);
	}

	private OnItemClickListener tagOnItemClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			if (position == 0) {
				if (hotList.size() == 0) {
					getHotList();
				}
			} else {
				if (columnDatas != null && columnDatas.size() > 0) {
					switch (position) {
					case 1:
						MobclickAgent.onEvent(RecommendNewActivity.this, "tv");
						if (tvList.size() == 0) {
							int cid = columnDatas.get(position).id;
							getTvList(cid, 0, 10);
						}
						break;
					case 2:
						MobclickAgent.onEvent(RecommendNewActivity.this,
								"variety");
						if (varietyList.size() == 0) {
							int cid = columnDatas.get(position).id;
							getVarietyList(cid, 0, 10);
						}
						break;
					case 3:
						MobclickAgent.onEvent(RecommendNewActivity.this,
								"search");
						if (overseaList.size() == 0) {
							int cid = columnDatas.get(position).id;
							getOverseaList(cid, 0, 10);
						}
						break;
					case 4:
						MobclickAgent.onEvent(RecommendNewActivity.this,
								"topic");
						if (specialList.size() == 0) {
							int cid = columnDatas.get(position).id;
							getSpecialListNew(cid, 0, 10);
						}
						break;
					default:
						break;
					}
				}
			}
			tagPosition = position;
			tagAdapter.notifyDataSetChanged();
			tagLayout.setVisibility(View.GONE);
			typeLayout.setVisibility(View.GONE);
			title.setText(columnDatas.get(position).name);
			isFromTagListItemClick = true;
			programViewPager.setCurrentItem(position);
		}

	};

	private class TagAdapter extends BaseAdapter {

		private final ArrayList<ColumnData> list;

		public TagAdapter(ArrayList<ColumnData> list) {
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
						.from(RecommendNewActivity.this);
				convertView = inflater.inflate(R.layout.rcmd_tag_item, null);
				viewHolder.nameTxt = (TextView) convertView
						.findViewById(R.id.textView);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}
			ColumnData tempColumn = list.get(position);
			String name = tempColumn.name;
			if (name != null) {
				viewHolder.nameTxt.setText(name);
			}
			if (tagPosition == position) {
				viewHolder.nameTxt.setTextColor(getResources().getColor(
						R.color.red));
			} else {
				viewHolder.nameTxt.setTextColor(getResources().getColor(
						R.color.rcmd_tag_color));
			}
			return convertView;
		}

		public class ViewHolder {
			public TextView nameTxt;
		}

	}

	ArrayList<RecommendData> list = new ArrayList<RecommendData>();

	OnClickListener recommendClick = new OnClickListener() {
		@Override
		public void onClick(View v) {
			int position = (Integer) v.getTag();
			int type = list.get(position).type;
			switch (type) {
			case 1: // program
				MobclickAgent.onEvent(RecommendNewActivity.this, "banner",
						list.get(position).name);
				String programId = String.valueOf(list.get(position).id);
				String topicId = list.get(position).topicId;
				// VodProgramData.current.cpId = 0;
				openProgramDetailActivity(programId, topicId,
						list.get(position).name, 0);
				break;
			case 2: // activity
				int acitivtyId = (int) list.get(position).id;
				openActivityDetailActivity(acitivtyId);
				break;
			case 3:// user
				break;
			case 4:// star
				break;
			case 12:
				Intent iAd = new Intent(RecommendNewActivity.this,
						WebBrowserActivity.class);
				iAd.putExtra("url", list.get(position).url);
				iAd.putExtra("title", list.get(position).name);
				startActivity(iAd);
				break;
			default:
				break;
			}
		}
	};

	private class HotProgramListAdapter extends BaseAdapter {
		private ArrayList<VodProgramData> list;

		public HotProgramListAdapter(ArrayList<VodProgramData> list) {
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
						.from(RecommendNewActivity.this);
				convertView = inflater.inflate(R.layout.rcmd_hot_list_item,
						null);
				viewHolder.nameTxt = (TextView) convertView
						.findViewById(R.id.name);
				viewHolder.updateTxt = (TextView) convertView
						.findViewById(R.id.update);
				viewHolder.introTxt = (TextView) convertView
						.findViewById(R.id.intro);
				viewHolder.viewerCount = (TextView) convertView
						.findViewById(R.id.viewercount);
				viewHolder.programPic = (ImageView) convertView
						.findViewById(R.id.pic);
				viewHolder.status = (TextView) convertView
						.findViewById(R.id.status);
				viewHolder.time = (TextView) convertView
						.findViewById(R.id.time);
				viewHolder.scoreView = (TextView) convertView
						.findViewById(R.id.score);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}
			VodProgramData temp = list.get(position);
			String name = temp.name;
			if (name != null) {
				viewHolder.nameTxt.setText(name);
			}
			int viewerCount = temp.playTimes;
			viewHolder.viewerCount.setText(CommonUtils
					.processPlayCount(viewerCount));
			String intro = temp.shortIntro;
			if (intro != null) {
				viewHolder.introTxt.setText(intro);
			}
			if (temp.livePlay == 0) {
				String tvName = temp.channelName;
				if (tvName != null) {
					viewHolder.updateTxt.setText(tvName);
				}
				String startTime = temp.startTime;
				String endTime = temp.endTime;
				if (startTime != null && endTime != null) {
					viewHolder.time.setText(startTime + "-" + endTime);
					viewHolder.time.setVisibility(View.VISIBLE);
				} else {
					viewHolder.time.setVisibility(View.GONE);
				}
				if (temp.isPlaying == 0) {
					String playMinutes = temp.playMinutes;
					if (playMinutes != null) {
						viewHolder.status.setText(playMinutes);
						viewHolder.status.setBackgroundDrawable(null);
					}
				} else {
					viewHolder.status.setText("");
				}
				viewHolder.status.setVisibility(View.VISIBLE);
			} else {
				String updateText = temp.updateName;
				if (updateText != null) {
					viewHolder.updateTxt.setText(updateText);
				}
				viewHolder.status.setVisibility(View.GONE);
				viewHolder.time.setVisibility(View.GONE);
			}
			String score = temp.point;
			if (score != null) {
				try {
					float floatScore = Float.valueOf(score);
					if (floatScore <= 1) {
						viewHolder.scoreView.setVisibility(View.GONE);
					} else {
						if (score.length() > 3) {
							score = score.substring(0, 3);
						}
						viewHolder.scoreView.setText(score + "分");
						viewHolder.scoreView.setVisibility(View.VISIBLE);
					}
				} catch (Exception e) {
					viewHolder.scoreView.setVisibility(View.GONE);
				}

			} else {
				viewHolder.scoreView.setVisibility(View.GONE);
			}
			String url = temp.pic;
			viewHolder.programPic.setTag(url);
			imageLoaderHelper.loadImage(viewHolder.programPic, url,
					R.drawable.recommend_default);
			return convertView;
		}

		public class ViewHolder {
			public TextView nameTxt;
			public TextView updateTxt;
			public TextView introTxt;
			public TextView viewerCount;
			public ImageView programPic;
			public TextView status;
			public TextView time;
			public TextView scoreView;
		}

	}

	/**
	 * 
	 * @description 点播节目adapter
	 * 
	 */
	private class VodProgramListViewAdapter extends BaseAdapter {

		private final ArrayList<VodProgramData> list;

		public VodProgramListViewAdapter(ArrayList<VodProgramData> list) {
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
						.from(RecommendNewActivity.this);
				convertView = inflater.inflate(R.layout.rcmd_list_item, null);
				viewHolder.nameTxt = (TextView) convertView
						.findViewById(R.id.name);
				viewHolder.updateTxt = (TextView) convertView
						.findViewById(R.id.update);
				viewHolder.introTxt = (TextView) convertView
						.findViewById(R.id.intro);
				viewHolder.viewerCount = (TextView) convertView
						.findViewById(R.id.viewercount);
				viewHolder.programPic = (ImageView) convertView
						.findViewById(R.id.pic);
				viewHolder.scoreView = (TextView) convertView
						.findViewById(R.id.score);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}
			VodProgramData temp = list.get(position);
			String name = temp.name;
			if (name != null) {
				viewHolder.nameTxt.setText(name);
			}
			String updateText = temp.updateName;
			if (updateText != null) {
				viewHolder.updateTxt.setText(updateText);
			}
			int viewerCount = temp.playTimes;
			viewHolder.viewerCount.setText(CommonUtils
					.processPlayCount(viewerCount));
			String intro = temp.shortIntro;
			if (intro != null) {
				viewHolder.introTxt.setText(intro);
			}
			String score = temp.point;
			if (score != null) {
				if (score.length() > 3) {
					score = score.substring(0, 3);
				}
				viewHolder.scoreView.setText(score + "分");
				viewHolder.scoreView.setVisibility(View.VISIBLE);
			} else {
				viewHolder.scoreView.setVisibility(View.GONE);
			}
			String url = temp.pic;
			viewHolder.programPic.setTag(url);
			imageLoaderHelper.loadImage(viewHolder.programPic, url,
					R.drawable.recommend_default);
			return convertView;
		}

		public class ViewHolder {
			public TextView nameTxt;
			public TextView updateTxt;
			public TextView introTxt;
			public TextView viewerCount;
			public ImageView programPic;
			public TextView scoreView;
		}
	}

	/**
	 * 
	 * @description 专题adapter
	 * 
	 */
	private class SpecialProgramListViewAdapter extends BaseAdapter {

		private final ArrayList<ColumnData> list;

		public SpecialProgramListViewAdapter(ArrayList<ColumnData> list) {
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
						.from(RecommendNewActivity.this);
				convertView = inflater.inflate(R.layout.rcmd_special_list_item,
						null);
				viewHolder.nameTxt = (TextView) convertView
						.findViewById(R.id.name);
				viewHolder.introTxt = (TextView) convertView
						.findViewById(R.id.intro);
				viewHolder.programPic = (ImageView) convertView
						.findViewById(R.id.pic);
				viewHolder.viewCountText = (TextView) convertView
						.findViewById(R.id.viewercount);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}
			ColumnData temp = list.get(position);
			String name = temp.name;
			if (name != null) {
				viewHolder.nameTxt.setText(name);
			}
			String intro = temp.intro;
			if (intro != null) {
				viewHolder.introTxt.setText(intro);
			}
			int viewerCount = temp.playTimes;
			viewHolder.viewCountText.setText(CommonUtils
					.processPlayCount(viewerCount));
			String url = temp.pic;
			viewHolder.programPic.setTag(url);
			/*
			 * loadListImage(viewHolder.programPic, url,
			 * R.drawable.recommend_default);
			 */
			imageLoaderHelper.loadImage(viewHolder.programPic, url,
					R.drawable.recommend_default);
			return convertView;
		}

		public class ViewHolder {
			public TextView nameTxt;
			public TextView introTxt;
			public ImageView programPic;
			public TextView viewCountText;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		int listPosition = position - 1;
		switch ((Integer) parent.getTag()) {
		case 0:
			int hotPosition = position - hotListView.getHeaderViewsCount();
			if (hotPosition == hotList.size() || hotPosition < 0) {
				return;
			}
			VodProgramData tempHot = hotList.get(hotPosition);
			MobclickAgent.onEvent(RecommendNewActivity.this, "jiemu",
					tempHot.name);
			String pid = tempHot.id;
			String topicId = tempHot.topicId;
			openProgramDetailActivity(pid, topicId, tempHot.updateName,
					tempHot.cpId);
			break;
		case 1:
			if (listPosition == tvList.size() || listPosition < 0) {
				return;
			}
			VodProgramData tempTv = tvList.get(listPosition);
			String tvId = tempTv.id;
			String tvTopicId = tempTv.topicId;
			MobclickAgent.onEvent(RecommendNewActivity.this, "jiemu",
					tempTv.name);
			openProgramDetailActivity(tvId, tvTopicId, tempTv.updateName, 0L);
			break;
		case 2:
			if (listPosition == varietyList.size() || listPosition < 0) {
				return;
			}
			VodProgramData tempVariety = varietyList.get(listPosition);
			String varietyId = tempVariety.id;
			String varietyTopicId = tempVariety.topicId;
			MobclickAgent.onEvent(RecommendNewActivity.this, "jiemu",
					tempVariety.name);
			openProgramDetailActivity(varietyId, varietyTopicId,
					tempVariety.updateName, 0L);
			break;
		case 3:
			if (listPosition == overseaList.size() || listPosition < 0) {
				return;
			}
			VodProgramData tempOver = overseaList.get(listPosition);
			String overseaId = tempOver.id;
			String overseaTopicId = tempOver.topicId;
			MobclickAgent.onEvent(RecommendNewActivity.this, "jiemu",
					tempOver.name);
			openProgramDetailActivity(overseaId, overseaTopicId,
					tempOver.updateName, 0L);
			break;
		case 4:
			if (listPosition == specialList.size() || listPosition < 0) {
				return;
			}
			MobclickAgent.onEvent(RecommendNewActivity.this, "jiemu",
					specialList.get(listPosition).name);
			int specialId = specialList.get(listPosition).id;
			String title = specialList.get(listPosition).name;
			openSepecicalActivity(specialId, title);
			break;
		default:
			break;
		}
	}

	private void openProgramDetailActivity(String id, String topicId,
			String updateName, long cpId) {
		Intent intent = new Intent(this, ProgramNewActivity.class);
		intent.putExtra("programId", id);
		intent.putExtra("topicId", topicId);
		intent.putExtra("cpId", cpId);
		intent.putExtra("updateName", updateName);

		startActivity(intent);
	}

	private void openSepecicalActivity(int id, String title) {
		Intent intent = new Intent(this, SpecicalActivity.class);
		intent.putExtra("id", id);
		intent.putExtra("title", title);
		startActivity(intent);
	}

	private void openActivityDetailActivity(int id) {
		Intent intent = new Intent(this, ActivitiesDetailActivity.class);
		intent.putExtra("id", id);
		startActivity(intent);
	}

	@Override
	public void onNetBegin(String method) {

	}

	@Override
	public void onNetEnd(String msg, String method) {
		if ("hotProgramList".equals(method)) {
			if (msg != null && msg.equals("")) {
				updateHotProgramList();
			} else {
				hotErr.setVisibility(View.VISIBLE);
				hotProgressHide = false;
				hotProgressBar.setVisibility(View.GONE);
			}
			recommandHotTask = null;
		} else if ("recommendDetail".equals(method)) {
			if (msg != null && msg.equals("")) {
				// updateRecommandPicUI();
				updateHotProgramList();
				// startAutoChangeRecommand();
				try {
					updateTagLayout();
					updateTagActionBar();
				} catch (NullPointerException e) {
					// TODO: handle exception
					e.printStackTrace();
				}
			} else {
				alertToast(msg);
				hotListView.onLoadError();
			}
			recommandDetailTask = null;
		} else if ("subColumnList".equals(method)) {
			if (msg != null && msg.equals("")) {
				// updateSpecialProgramList();
				updateSpecialProgramListNew();
			} else {
				if (specialList != null && specialList.size() == 0) {
					specialErr.setVisibility(View.VISIBLE);
				}
				specialProgressBar.setVisibility(View.GONE);
				specialListView.onLoadError();
			}
			// recommandSpecialTask = null;
			getSpecicalDataTask = null;
		} else if ("columnVideoList".equals(method)) {
			switch (type) {
			case TYPE_TV:
				if (msg != null && msg.equals("")) {
					updateTvProgramList();
				} else {
					if (tvList.size() == 0) {
						tvErr.setVisibility(View.VISIBLE);
					}
					tvProgressBar.setVisibility(View.GONE);
					tvListView.onLoadError();
				}
				recommandTvTask = null;
				break;
			case TYPE_VARIETY:
				if (msg != null && msg.equals("")) {
					updateVarietyProgramList();
				} else {
					if (varietyList.size() == 0) {
						varietyErr.setVisibility(View.VISIBLE);
					}
					varietyProgressBar.setVisibility(View.GONE);
					varietyListView.onLoadError();
				}
				recommandVarietyTask = null;
				break;
			case TYPE_OVERSEA:
				if (msg != null && msg.equals("")) {
					updateOverseaProgramList();
				} else {
					if (overseaList.size() == 0) {
						overseaErr.setVisibility(View.VISIBLE);
					}
					overseaProgressBar.setVisibility(View.GONE);
					overseaListView.onLoadError();
				}
				recommandOveseaTask = null;
				break;
			default:
				break;
			}
		} else if ("hotVodProgramList".equals(method)) {
			if (msg != null && msg.equals("")) {
				updateHotProgramList();
			} else {
				// hotErr.setVisibility(View.VISIBLE);
				hotProgressHide = false;
				hotProgressBar.setVisibility(View.GONE);
			}
			recommendVodTask = null;
		}
	}

	@Override
	public void onNetEnd(String msg, String method, int type) {
		if ("columnVideoList".equals(method)) {
			switch (type) {
			case TYPE_TV:
				if (msg != null && msg.equals("")) {
					updateTvProgramList();
				} else {
					if (tvList.size() == 0) {
						tvErr.setVisibility(View.VISIBLE);
					}
					tvProgressBar.setVisibility(View.GONE);
					tvListView.onLoadError();
				}
				recommandTvTask = null;
				break;
			case TYPE_VARIETY:
				if (msg != null && msg.equals("")) {
					updateVarietyProgramList();
				} else {
					if (varietyList.size() == 0) {
						varietyErr.setVisibility(View.VISIBLE);
					}
					varietyProgressBar.setVisibility(View.GONE);
					varietyListView.onLoadError();
				}
				recommandVarietyTask = null;
				break;
			case TYPE_OVERSEA:
				if (msg != null && msg.equals("")) {
					updateOverseaProgramList();
				} else {
					if (overseaList.size() == 0) {
						overseaErr.setVisibility(View.VISIBLE);
					}
					overseaProgressBar.setVisibility(View.GONE);
					overseaListView.onLoadError();
				}
				recommandOveseaTask = null;
				break;
			default:
				break;
			}
		}
	}

	@Override
	public void onCancel(String method) {
		if ("hotProgramList".equals(method)) {
			recommandHotTask = null;
		} else if ("recommendDetail".equals(method)) {
			recommandDetailTask = null;
		} else if ("subColumnList".equals(method)) {
			recommandSpecialTask = null;
		}
	}

	public void alertToast(String msg) {
		Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
	}

	private void close() {
		if (recommandTvTask != null) {
			recommandTvTask.cancel(true);
		} else if (recommandVarietyTask != null) {
			recommandVarietyTask.cancel(true);
		} else if (recommandOveseaTask != null) {
			recommandOveseaTask.cancel(true);
		} else if (recommandHotTask != null) {
			recommandHotTask.cancel(true);
		} else if (recommandSpecialTask != null) {
			recommandSpecialTask.cancel(true);
		}
		if (imageLoader != null) {
			try {
				imageLoader.recyle();
			} catch (RuntimeException e) {
				e.printStackTrace();
			}
		}
		finish();
	}

	@Override
	public void onBackPressed() {
		close();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (bigPicSize > 0)
			serverHandler.sendEmptyMessageDelayed(AUTO_CHANGE_FOCUS_PIC,
					AUTO_CHANGE_FOCUS_PIC_TIME);
		MobclickAgent.onResume(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		serverHandler.removeMessages(AUTO_CHANGE_FOCUS_PIC);
		MobclickAgent.onPause(this);
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

	private void startAutoChangeRecommand() {
		serverHandler.sendEmptyMessageDelayed(AUTO_CHANGE_MESSAGE, 5000);
	}

	private void resetAutoChangeRecommand() {
		stopAutoChangeRecommand();
		startAutoChangeRecommand();
	}

	private void stopAutoChangeRecommand() {
		serverHandler.removeMessages(AUTO_CHANGE_MESSAGE);
	}

	private final int AUTO_CHANGE_MESSAGE = 1;
	private final Handler serverHandler = new Handler(new Callback() {

		@Override
		public boolean handleMessage(Message msg) {
			switch (msg.what) {
			case AUTO_CHANGE_FOCUS_PIC:
				if (bigPicPosition == (bigPicSize - 1))
					bigPicPosition = 0;
				else
					bigPicPosition++;

				picViewPager.setSelection(bigPicPosition);
				serverHandler.sendEmptyMessageDelayed(AUTO_CHANGE_FOCUS_PIC,
						AUTO_CHANGE_FOCUS_PIC_TIME);
				break;
			case AUTO_CHANGE_MESSAGE:
				processGalleryAutoScroll();
				break;
			case MESSAGE_HIDE_ACTION_BAR:
				try {
					hideActionBar();
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
				break;
			default:
				break;
			}
			return false;
		}
	});

	private void processGalleryAutoScroll() {
		int current = picViewPager.getSelectedItemPosition();
		int count = list.size();
		if (current == count - 1) {
			picViewPager.setSelection(0);
		} else {
			picViewPager.setSelection(current + 1, true);
		}
	}

	private OnItemClickListener focusItemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			if (OtherCacheData.current().isDebugMode)
				Log.e("focusItemClickListener", "item  clicked!");
			int position = arg2;
			int type = list.get(position).type;
			switch (type) {
			case 1: // program
				MobclickAgent.onEvent(RecommendNewActivity.this, "banner",
						list.get(position).name);
				String programId = String.valueOf(list.get(position).id);
				String topicId = list.get(position).topicId;
				// VodProgramData.current.cpId = 0;
				openProgramDetailActivity(programId, topicId,
						list.get(position).name, 0);
				break;
			case 2: // activity
				int acitivtyId = (int) list.get(position).id;
				openActivityDetailActivity(acitivtyId);
				break;
			case 3:// user
				break;
			case 4:// star
				break;
			case 12:
				Intent iAd = new Intent(RecommendNewActivity.this,
						WebBrowserActivity.class);
				RecommendData rcd = list.get(position);
				iAd.putExtra("url", rcd.url);
				iAd.putExtra("title", rcd.name);
				startActivity(iAd);
				break;
			case 13:
				Intent intent = new Intent(RecommendNewActivity.this,
						RecommandAppActivity.class);
				startActivity(intent);
				break;
			default:
				break;
			}
		}
	};
	private OnItemSelectedListener focusItemSelectedListener = new OnItemSelectedListener() {

		@Override
		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {

			bigPicPosition = arg2;

			try {
				onPicSelected(arg2);
			} catch (NullPointerException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
			// TODO Auto-generated method stub

		}
	};

	private static final int MESSAGE_PIC_CHANGE = 33;

	OnItemSelectionChangeListener selectionChangeListener = new OnItemSelectionChangeListener() {

		@Override
		public void onMoveLeft(int position) {
			Message msg = new Message();
			msg.what = MESSAGE_PIC_CHANGE;
			msg.obj = position;
			serverHandler.sendMessage(msg);
			Toast.makeText(getApplicationContext(), "position=" + position,
					position).show();
		}

		@Override
		public void onMoveRight(int position) {
			Message msg = new Message();
			msg.what = MESSAGE_PIC_CHANGE;
			msg.obj = position;
			serverHandler.sendMessage(msg);
			Toast.makeText(getApplicationContext(), "position=" + position,
					position).show();
		}

	};
	private RelativeLayout actionBarLinearLayout;
	private TextView leftTextView, rightTextView, centerTextView;

	/**
	 * 更新推荐页类型滑动展示布局
	 */
	private void updateTagActionBar() {
		ViewStub viewStub = (ViewStub) findViewById(R.id.type_actionbar_layout);
		viewStub.setLayoutResource(R.layout.recommend_actionbar_layout);
		actionBarLinearLayout = (RelativeLayout) viewStub.inflate();
		leftTextView = (TextView) actionBarLinearLayout.findViewById(R.id.tag1);
		centerTextView = (TextView) actionBarLinearLayout
				.findViewById(R.id.tag2);
		rightTextView = (TextView) actionBarLinearLayout
				.findViewById(R.id.tag3);
		changeViewText(0, 0);
	}

	/**
	 * 
	 * @param centerPosition
	 *            中间文本的在标题数组中的位置
	 * @param fromDirection
	 *            表示viewPager从左边 或者右边划过来 或者需要直接显示 -1从左边来 0不动 1从右边来
	 */
	private void changeViewText(int centerPosition, int fromDirection) {
		try {
			showActionBar();// 显示actionBar
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
		resetHideActionBar();// 重置消失
		String leftText;
		String centerText;
		String rightText;
		switch (centerPosition) {
		case 0:
			leftText = "";
			centerText = columnDatas.get(0).name;
			rightText = columnDatas.get(1).name;
			break;
		case 1:
			leftText = columnDatas.get(0).name;
			centerText = columnDatas.get(1).name;
			rightText = columnDatas.get(2).name;

			break;
		case 2:
			leftText = columnDatas.get(1).name;
			centerText = columnDatas.get(2).name;
			rightText = columnDatas.get(3).name;
			break;
		case 3:
			leftText = columnDatas.get(2).name;
			centerText = columnDatas.get(3).name;
			rightText = columnDatas.get(4).name;
			break;
		case 4:
			leftText = columnDatas.get(3).name;
			centerText = columnDatas.get(4).name;
			rightText = "";
			break;
		default:
			leftText = "";
			centerText = "";
			rightText = "";
			break;
		}
		leftTextView.setText(leftText);
		centerTextView.setText(centerText);
		rightTextView.setText(rightText);
		if (fromDirection == -1) {
			centerTextView.startAnimation(leftInAnim);
		} else {
			centerTextView.startAnimation(rightInAnim);
		}
	}

	private static final int MESSAGE_HIDE_ACTION_BAR = 22;

	private void resetHideActionBar() {
		serverHandler.removeMessages(MESSAGE_HIDE_ACTION_BAR);
		serverHandler.sendEmptyMessageDelayed(MESSAGE_HIDE_ACTION_BAR, 2000);
	}

	private void startDelayHideActionBar() {
		serverHandler.sendEmptyMessage(MESSAGE_HIDE_ACTION_BAR);
	}

	private void hideActionBar() {
		actionBarLinearLayout.setVisibility(View.GONE);
		actionBarLinearLayout.startAnimation(fadeOutAnim);
	}

	private void showActionBar() {
		if (actionBarLinearLayout != null && !actionBarLinearLayout.isShown())
			actionBarLinearLayout.setVisibility(View.VISIBLE);
	}

	// 表示是不是来自标题栏 下拉的点击 选择viewpager 如果是 那么不执行actionBar显示
	private boolean isFromTagListItemClick = false;
	// actionBar animation
	private Animation fadeOutAnim;
	private Animation leftInAnim;
	private Animation rightInAnim;

	/*************************************************************/
	private ArrayList<ColumnData> tempSpeicalList = null;
}

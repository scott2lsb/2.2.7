package com.sumavision.talktv2.activity;

import java.util.List;

import android.app.Service;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.sumavision.talktv2.R;
import com.sumavision.talktv2.data.PicAndTxtData;
import com.sumavision.talktv2.net.JSONMessageType;

/**
 * 
 * @author 郭鹏
 * @createTime 2012-6-17
 * @description 背景设置界面适配
 * @changeLog
 * 
 */
public class BackgroundSettingAdapter extends BaseAdapter {

	private Context c;
	private List<PicAndTxtData> lpd;
	private BackgroundSettingCache cache;

	public BackgroundSettingAdapter(Context c, List<PicAndTxtData> lpd) {
		this.c = c;
		this.lpd = lpd;
	}

	@Override
	public int getCount() {
		return lpd.size();
	}

	@Override
	public PicAndTxtData getItem(int arg0) {
		return lpd.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}

	@Override
	public View getView(int arg0, View arg1, ViewGroup arg2) {

		LayoutInflater li = (LayoutInflater) c
				.getSystemService(Service.LAYOUT_INFLATER_SERVICE);
		ImageView pic;
		TextView txt;

		if (arg1 == null) {
			arg1 = li.inflate(R.layout.background_setting_item, null);
			cache = new BackgroundSettingCache(arg1);
			arg1.setTag(cache);
		} else {
			cache = (BackgroundSettingCache) arg1.getTag();
		}

		pic = cache.getPic();
		txt = cache.getTxt();

		PicAndTxtData pad = getItem(arg0);
		txt.setText(pad.txt);

		if (!pad.isLocalSDcard) {
			pic.setImageResource(pad.type);
		} else {
			// TODO: 读取SDCard图片
			// pic.setImageResource(pad.type);
			String fileDir = JSONMessageType.THEME_SAVED_PIC_SDCARD_FOLDER;
			String filePath = fileDir + pad.url;
			Drawable d = Drawable.createFromPath(filePath);
			pic.setImageDrawable(d);
		}

		return arg1;
	}

}

package com.sumavision.talktv2.activity;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.sumavision.talktv2.R;

/**
 * 
 * @author 郭鹏
 * @createTime 2012-6-17
 * @description 背景设置界面缓存
 * @changeLog
 * 
 */
public class BackgroundSettingCache {

	private View v;
	private ImageView pic;
	private TextView txt;
	
	public BackgroundSettingCache(View v){
		this.v = v;
	}
	
	public ImageView getPic() {
		if (pic == null) {
			pic = (ImageView) v.findViewById(R.id.bs_type_item_pic);
		}
		return pic;
	}
	
	public TextView getTxt() {
		if (txt == null) {
			txt = (TextView) v.findViewById(R.id.bs_type_item_txt);
		}
		return txt;
	}
	
}

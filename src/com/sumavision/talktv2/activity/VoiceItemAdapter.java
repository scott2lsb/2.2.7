package com.sumavision.talktv2.activity;

import java.util.ArrayList;

import android.app.Service;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.sumavision.talktv2.R;

/**
 * 
 * @author 郭鹏
 * @description 节目单界面列表Adapter
 *
 */
public class VoiceItemAdapter extends BaseAdapter{

	private Context c;
	@SuppressWarnings("rawtypes")
	private ArrayList lp;
	@SuppressWarnings("rawtypes")
	public VoiceItemAdapter(Context c, ArrayList lp){
		this.c = c;
		this.lp = lp;
	}
	
	@Override
	public int getCount() {
		return lp.size();
	}

	@Override
	public Object getItem(int position) {
		return lp.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) c.getSystemService(Service.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.voice_item, null);
		
        TextView time = (TextView)v.findViewById(R.id.voice_item_txt);
        
        time.setText(lp.get(position).toString());
        
		return v;
	}

}

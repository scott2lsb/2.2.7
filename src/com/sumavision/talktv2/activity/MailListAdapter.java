package com.sumavision.talktv2.activity;

import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.sumavision.talktv2.R;
import com.sumavision.talktv2.activity.AsyncImageLoaderSmallPic.ImageCallback;
import com.sumavision.talktv2.data.MailData;
import com.sumavision.talktv2.net.JSONMessageType;
import com.sumavision.talktv2.utils.BitmapUtils;

/**
 * @author 李梦思
 * @version 2.0
 * @createTime 2012-6-15
 * @description 私信适配器
 * @changeLog
 */
public class MailListAdapter extends BaseAdapter {

	private List<MailData> lm;
	private Context context;
	AsyncImageLoaderSmallPic asyncImageLoader;

	public MailListAdapter(Context context, List<MailData> lm, ListView list) {
		this.context = context;
		this.lm = lm;
		asyncImageLoader = new AsyncImageLoaderSmallPic();
	}

	@Override
	public int getCount() {
		int i = 0;

		try {
			i = lm.size();
		} catch (NullPointerException e) {
			// TODO: handle exception
			e.printStackTrace();
			return 0;
		}
		return i;
	}

	@Override
	public MailData getItem(int position) {
		return lm.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = new ViewHolder();
		final MailData p = lm.get(position);
		LayoutInflater inflater = LayoutInflater.from(context);
		convertView = inflater.inflate(R.layout.mail_list_item, null);
		holder.pic = (ImageView) convertView.findViewById(R.id.mail_list_pic);
		holder.name = (TextView) convertView.findViewById(R.id.mail_list_name);
		holder.name.setText(p.rUserName);
		holder.content = (TextView) convertView
				.findViewById(R.id.mail_list_content);
		holder.content.setText(p.content + "条信息");
		holder.time = (TextView) convertView.findViewById(R.id.mail_list_time);
		holder.time.setText(p.timeStemp);
		holder.pic.setImageResource(R.drawable.mainpage_list_item_pic2);

		String fileDir = JSONMessageType.MY_SAVED_PIC_SDCARD_FOLDER;
		String filePath = fileDir + p.rUserName + ".jpg";
		Drawable d = Drawable.createFromPath(filePath);
		if (d != null) {
			holder.pic.setImageDrawable(d);
		} else {
			if (!p.rUserPhoto.equals("")) {
				// holder.pic.setTag(p.rUserPhoto + JSONMessageType.SMALL_JPG);
				Drawable cachedImage = asyncImageLoader.loadDrawable(
						p.rUserPhoto + JSONMessageType.SMALL_JPG,
						new ImageCallback() {
							public void imageLoaded(Drawable imageDrawable,
									String imageUrl) {
								// ImageView imageViewByTag = (ImageView) list
								// .findViewWithTag(imageUrl);
								//
								// if (imageViewByTag != null) {
								// imageViewByTag.setImageDrawable(imageDrawable);
								// }
								boolean sdExists = Environment
										.getExternalStorageState().equals(
												Environment.MEDIA_MOUNTED);
								if (sdExists) {
									try {
										BitmapUtils
												.saveDrawable(
														imageDrawable,
														JSONMessageType.MY_SDCARD_FOLDER_SHORT,
														p.rUserName);
									} catch (Exception e) {

									}
								}
							}
						});
				if (cachedImage != null) {
					holder.pic.setImageDrawable(cachedImage);
				}
			}
		}
		convertView.setTag(holder);
		return convertView;
	}

	public class ViewHolder {
		public ImageView pic;
		public TextView name;
		public TextView content;
		public TextView time;
	}
}

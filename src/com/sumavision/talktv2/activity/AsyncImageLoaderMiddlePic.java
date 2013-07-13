package com.sumavision.talktv2.activity;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.sumavision.talktv2.data.OtherCacheData;

/**
 * 
 * @author 郭鹏
 * @createTime
 * @description 异步加载图片
 * @changeLog
 * 
 */
public class AsyncImageLoaderMiddlePic {

	private HashMap<String, WeakReference<Drawable>> imageCache;

	public ArrayList<SoftReference<Bitmap>> bitmaps = new ArrayList<SoftReference<Bitmap>>();

	private ExecutorService executorService = Executors.newFixedThreadPool(3);

	public AsyncImageLoaderMiddlePic() {
		recyle();
		imageCache = new HashMap<String, WeakReference<Drawable>>();
	}

	public Drawable loadDrawable(final String imageUrl,
			final ImageCallback imageCallback) {
		if (imageCache.containsKey(imageUrl)) {
			WeakReference<Drawable> softReference = imageCache.get(imageUrl);
			Drawable drawable = softReference.get();
			if (drawable != null) {
				return drawable;
			}
		}

		final Handler handler = new Handler() {
			public void handleMessage(Message message) {
				imageCallback.imageLoaded((Drawable) message.obj, imageUrl);
			}
		};
		executorService.submit(new Runnable() {
			@Override
			public void run() {
				Drawable drawable = loadImageFromUrl(imageUrl);
				imageCache.put(imageUrl, new WeakReference<Drawable>(drawable));
				Message message = handler.obtainMessage(0, drawable);
				handler.sendMessage(message);
			}
		});
		return null;
	}

	public Drawable loadImageFromUrl(String url) {

		URL m;
		InputStream i = null;
		try {
			m = new URL(url);
			i = (InputStream) m.getContent();
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		BitmapFactory.Options opts = new BitmapFactory.Options();
		if (OtherCacheData.current().isLowAbilityDevice) {
			opts.inSampleSize = 3;
		} else {
			opts.inSampleSize = 2;
		}
		
		Bitmap bitmap;
		if(!OtherCacheData.current().isLowSKIAVersion){
			bitmap = BitmapFactory.decodeStream(i, null, opts);
		}else{
			bitmap = BitmapFactory.decodeStream(new FlushedInputStream(i), null, opts);
		}
		SoftReference<Bitmap> soft = new SoftReference<Bitmap>(bitmap);
		bitmaps.add(soft);
		// Drawable d = Drawable.createFromStream(i, "src");
		// Drawable d = Drawable.createFromStream(new FlushedInputStream(i) ,
		// "src");
		
		try {
			i.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		m = null;
		System.gc();
		
		return new BitmapDrawable(bitmap);
	}

	public interface ImageCallback {
		public void imageLoaded(Drawable imageDrawable, String imageUrl);
	}

	static class FlushedInputStream extends FilterInputStream {
		public FlushedInputStream(InputStream inputStream) {
			super(inputStream);
		}

		@Override
		public long skip(long n) throws IOException {
			long totalBytesSkipped = 0L;
			while (totalBytesSkipped < n) {
				long bytesSkipped = in.skip(n - totalBytesSkipped);
				if (bytesSkipped == 0L) {
					int b = read();
					if (b < 0) {
						break;
						// we reached EOF
					} else {
						bytesSkipped = 1;
						// we read one byte
					}
				}
				totalBytesSkipped += bytesSkipped;
			}

			return totalBytesSkipped;

		}

	}

	public void recyle() {
		if (executorService != null) {
			executorService.shutdownNow();
			executorService = null;
			executorService = Executors.newFixedThreadPool(3);
			if (OtherCacheData.current().isDebugMode) {
			    Log.e("AsyncImageLoaderMiddlePic-recyle", "executorService.shutdownNow()");
			}
		}
		if(imageCache != null){
			imageCache.clear();
			imageCache = null;
		}
		int size = bitmaps.size();
		for (int i = 0; i < size; ++i) {
			try {
				Bitmap bitmap = bitmaps.get(i).get();
				bitmap.recycle();
				bitmap = null;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		System.gc();
	}
}

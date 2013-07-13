package com.sumavision.talktv2.activity;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
public class AsyncImageLoaderSmallPic {

	private HashMap<String, WeakReference<Drawable>> imageCache;

	private ExecutorService executorService = Executors.newFixedThreadPool(5);

	public AsyncImageLoaderSmallPic() {
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

		Drawable d;
		if(!OtherCacheData.current().isLowSKIAVersion){
			d = Drawable.createFromStream(i, "src");
		}else{
			d = Drawable
					.createFromStream(new FlushedInputStream(i), "src");
		}
		
		try {
			i.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		m = null;
		System.gc();
		
		return d;
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
			    Log.e("AsyncImageLoaderSmallPic-recyle", "executorService.shutdownNow()");
			}
		}
		
		if(imageCache != null){
			imageCache.clear();
			imageCache = null;
		}
		
		System.gc();
	}
}

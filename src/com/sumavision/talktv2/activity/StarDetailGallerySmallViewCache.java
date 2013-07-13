package com.sumavision.talktv2.activity;

import android.view.View;
import android.widget.ImageView;

import com.sumavision.talktv2.R;
  

/**
* @author 郭鹏
* @version 2.0
*  @createTime 2012-6-4
* @description 明星详情界面小图Gallery
* @changeLog
*/
public class StarDetailGallerySmallViewCache {   
  
        private View baseView;   
        private ImageView imageView;   
  
        public StarDetailGallerySmallViewCache(View baseView) {   
            this.baseView = baseView;   
        }   
        
        public ImageView getImageView() {   
            if (imageView == null) {   
                imageView = (ImageView) baseView.findViewById(R.id.sd_gallery_img_small);   
            }   
            return imageView;   
        }   
  
} 


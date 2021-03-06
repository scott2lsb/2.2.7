package com.sumavision.talktv2.activity;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

public class StarGridView extends GridView {   
    public StarGridView(Context context, AttributeSet attrs) {   
        super(context, attrs);   
    }   
  
    public StarGridView(Context context) {   
        super(context);   
    }   
  
    public StarGridView(Context context, AttributeSet attrs, int defStyle) {   
        super(context, attrs, defStyle);   
    }   
  
    @Override   
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {   
  
        int expandSpec = MeasureSpec.makeMeasureSpec(   
                Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);   
        super.onMeasure(widthMeasureSpec, expandSpec);   
    }   
}  

package com.example.readnoveldemo.util;

import android.app.Activity;
import android.graphics.drawable.BitmapDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import com.example.readnoveldemo.R;

/**
 * Created by Administrator on 2015/7/5.
 */
public class PopupWinUtil {
    private static  PopupWindow popupWindow;
    public static PopupWindow createPopupWindow(Activity activity,int layout){
        View popView = View.inflate(activity,layout,null);
        popupWindow = new PopupWindow(popView, ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        popupWindow.setAnimationStyle(R.style.popwin_anim);
        popupWindow.setFocusable(true);
        return popupWindow;
    }
    public PopupWindow getPopupWindow() {
        return popupWindow;
    }
}

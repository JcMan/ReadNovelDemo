package com.example.readnoveldemo.util;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;

/**
 * Created by jcman on 16-8-19.
 */
public class DialogUtil {

    private static ProgressDialog mDialog;

    public static void show(Context context){
        if (mDialog==null){
            mDialog = new ProgressDialog(context);
            mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mDialog.setMessage("获取目录中...");
        }
        mDialog.show();
    }

    public static void dimiss(Activity activity){
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mDialog!=null)
                    mDialog.dismiss();
            }
        });
    }
}

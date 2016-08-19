package com.example.readnoveldemo.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by jcman on 16-8-19.
 */
public class SpUtil {
    private Context mContext;
    private SharedPreferences mPreferences;
    public SpUtil(Context context){
        mContext = context;
    }

    public void setHistoryPos(int pos){
        mPreferences = mContext.getSharedPreferences("book",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putInt("pos",pos);
        editor.commit();
    }

    public int getHistoryPos(){
        mPreferences = mContext.getSharedPreferences("book",Context.MODE_PRIVATE);
        return mPreferences.getInt("pos",0);
    }

    public void setEncodingName(String path,String encoding){
        mPreferences = mContext.getSharedPreferences("book",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(path,encoding);
        editor.commit();
    }

    public String getEncodingName(String path){
        mPreferences = mContext.getSharedPreferences("book",Context.MODE_PRIVATE);
        return mPreferences.getString(path,"");
    }
}

package com.example.readnoveldemo.adapter;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Administrator on 2015/8/27.
 */
public class ViewHolder {

    private SparseArray<View> mViews;
    private int mPosition;
    private View mConvertView;


    public ViewHolder(Context context,ViewGroup parent,int layoutId,int position){
        mPosition = position;
        mViews = new SparseArray<View>();
        mConvertView = LayoutInflater.from(context).inflate(layoutId,parent,false);
        mConvertView.setTag(this);
    }
    public static ViewHolder get(Context context,View convertView,
                                 ViewGroup parent,int layoutId,int position){
        if(convertView==null){
            return new ViewHolder(context,parent,layoutId,position);
        }else{
            ViewHolder holder = (ViewHolder) convertView.getTag();
            holder.mPosition = position;
            return holder;
        }
    }

    /**
     * 通过ViewId获取控件
     * @param viewId
     * @param <T>
     * @return
     */
    public <T extends View> T getView(int viewId){
        View v = mViews.get(viewId);
        if(v==null){
            v = mConvertView.findViewById(viewId);
            mViews.put(viewId,v);
        }
        return (T)v;
    }

    public View getConvertView(){
        return mConvertView;
    }

    public ViewHolder setText(int viewId,String text){
        TextView tv = getView(viewId);
        tv.setText(text);
        return this;
    }

    

    public ViewHolder display(int viewId,int  resId){
        ImageView v = getView(viewId);
        v.setImageResource(resId);
        return this;
    }


}

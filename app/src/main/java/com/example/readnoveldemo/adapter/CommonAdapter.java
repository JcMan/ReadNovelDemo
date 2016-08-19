package com.example.readnoveldemo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import java.util.List;

/**
 * Created by Administrator on 2015/8/27.
 */
public  abstract class CommonAdapter<T> extends BaseAdapter{

    private Context mContext;
    private List<T> mList;
    private LayoutInflater mInflater;
    private int mLayoutId;

    protected Context getContext() {
        return mContext;
    }

    protected List<T> getList() {
        return mList;
    }

    protected LayoutInflater getInflater() {
        return mInflater;
    }

    public CommonAdapter(Context context,List<T> list,int layoutId){
        mContext = context;
        mList = list;
        mLayoutId = layoutId;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public T getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public  View getView(int position, View convertView, ViewGroup parent){
        ViewHolder holder = ViewHolder.get(mContext,convertView,parent,mLayoutId,position);
        convert(holder,mList.get(position),position);
        return holder.getConvertView();
    }
    public abstract void convert(ViewHolder holder,T t,int pos);
}

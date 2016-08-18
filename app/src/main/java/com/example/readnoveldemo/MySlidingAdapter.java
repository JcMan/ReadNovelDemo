package com.example.readnoveldemo;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;

import com.martian.libsliding.SlidingAdapter;

/**
 * Created by jcman on 16-8-18.
 */
public class MySlidingAdapter extends SlidingAdapter<Bitmap> {
    private int mPos = 0;
    private int mFirstPageEndPos = 0;
    private TestFactory mBookFactory;
    private Context mContext;
    private OnPageChangedListener mListener;

    public MySlidingAdapter(Context context,TestFactory factory, int pos){
        mContext = context;
        mPos = pos;
        mBookFactory = factory;
        mBookFactory.getNextPageBitmap();
        mFirstPageEndPos = mBookFactory.getEndPos();
        mBookFactory.setEndPos(mPos);
    }
    @Override
    public View getView(View contentView, Bitmap bitmap){
        ImageView tv_content;
        if (contentView == null){
            tv_content = new ImageView(mContext);
        }else{
            tv_content = (ImageView) contentView;
        }
        tv_content.setImageBitmap(bitmap);
        contentView = tv_content;
        return contentView;
    }
    @Override
    public boolean hasNext(){
        return mBookFactory.islastPage()!=true;
    }

    @Override
    protected void computeNext(){
        mBookFactory.setEndPos(mPos);
        mBookFactory.getNextPageBitmap();
        mPos = mBookFactory.getEndPos();
    }
    @Override
    protected void computePrevious(){
        mBookFactory.setEndPos(mPos);
        mBookFactory.getPrePageBitmap();
        mPos = mBookFactory.getEndPos();
    }

    @Override
    public boolean hasPrevious(){
        return mPos>=mFirstPageEndPos;
    }
    @Override
    public Bitmap getNext(){
        mBookFactory.setEndPos(mPos);
        mBookFactory.getNextPageBitmap();
        Bitmap bitmap = mBookFactory.getNextPageBitmap();
        mBookFactory.getPrePageBitmap();
        mBookFactory.setEndPos(mPos);
        return bitmap;
    }
    @Override
    public Bitmap getPrevious(){
        mBookFactory.setEndPos(mPos);
        Bitmap bitmap = mBookFactory.getPrePageBitmap();
        mBookFactory.getNextPageBitmap();
        mBookFactory.setEndPos(mPos);
        return bitmap;
    }

    @Override
    public Bitmap getCurrent(){
        Bitmap bitmap = mBookFactory.getNextPageBitmap();
        mBookFactory.getPrePageBitmap();
        mPos = mBookFactory.getEndPos();
        if (mListener!=null)
            mListener.onProgress((float) (mPos*1.0/mBookFactory.getBufLength()));
        return bitmap;
    }

    public interface OnPageChangedListener{
        void onProgress(float progress);
    }

    public void setOnPageChangedListener(OnPageChangedListener listener){
        mListener = listener;
    }
}

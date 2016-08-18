package com.example.readnoveldemo;
import android.graphics.Bitmap;
import android.os.Environment;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.martian.libsliding.SlidingLayout;
import com.martian.libsliding.SlidingAdapter;
import com.martian.libsliding.slider.OverlappedSlider;
import com.martian.libsliding.slider.PageSlider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

public class MainActivity extends AppCompatActivity {

    private SlidingLayout mSlidingLayout;
    private boolean mPagerMode = false;
    private TestFactory mFactory;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        mFactory = new TestFactory(this);
        mFactory.setBgBitmap(R.mipmap.ic_bg_blue);
        try {
            mFactory.openbook(Environment.getExternalStorageDirectory().getAbsolutePath()+"/280.txt");
        } catch (IOException e){
            e.printStackTrace();
        }
        setContentView(R.layout.activity_main);
        mSlidingLayout = (SlidingLayout) findViewById(R.id.sliding_container);
        mSlidingLayout.setOnTapListener(new SlidingLayout.OnTapListener() {

            @Override
            public void onSingleTap(MotionEvent event) {
                int screenWidth = getResources().getDisplayMetrics().widthPixels;
                int x = (int) event.getX();
                if (x > screenWidth / 2) {
                    mSlidingLayout.slideNext();
                } else if (x <= screenWidth / 2) {
                    mSlidingLayout.slidePrevious();
                }
            }
        });

        // 默认为左右平移模式
        switchSlidingMode();
    }

    private void switchSlidingMode(){
        if (mPagerMode){
            mSlidingLayout.setAdapter(new MySlidingAdapter(this,mFactory,0));
            mSlidingLayout.setSlider(new OverlappedSlider());
            Toast.makeText(this, "已切换为左右覆盖模式", Toast.LENGTH_SHORT).show();
        } else {
            mSlidingLayout.setAdapter(new MySlidingAdapter(this,mFactory,0));
            mSlidingLayout.setSlider(new PageSlider());
            Toast.makeText(this, "已切换为左右平移模式", Toast.LENGTH_SHORT).show();
        }
        mPagerMode = !mPagerMode;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_switch) {
            switchSlidingMode();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

}
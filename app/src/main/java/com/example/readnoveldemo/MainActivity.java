package com.example.readnoveldemo;
import android.os.Environment;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;
import com.martian.libsliding.SlidingLayout;
import com.martian.libsliding.slider.OverlappedSlider;
import com.martian.libsliding.slider.PageSlider;

import java.io.IOException;
import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity {

    private SlidingLayout mSlidingLayout;
    private boolean mPagerMode = false;
    private TestFactory mFactory;
    private TextView mProgress;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        mFactory = new TestFactory(this);
        mFactory.setBgBitmap(R.mipmap.ic_bg_blue);
        try {
            mFactory.openbook(Environment.getExternalStorageDirectory().getAbsolutePath()+"/book.txt");
        } catch (IOException e){
            e.printStackTrace();
        }
        setContentView(R.layout.activity_main);
        mProgress = (TextView) findViewById(R.id.tv_progress);
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
        MySlidingAdapter mAdapter = new MySlidingAdapter(this,mFactory,0);
        mAdapter.setOnPageChangedListener(new MySlidingAdapter.OnPageChangedListener() {
            @Override
            public void onProgress(float progress) {
                DecimalFormat format = new DecimalFormat("0.00%");
                mProgress.setText(format.format(progress));
            }
        });
        if (mPagerMode){
            mSlidingLayout.setAdapter(mAdapter);
            mSlidingLayout.setSlider(new OverlappedSlider());
            Toast.makeText(this, "已切换为左右覆盖模式", Toast.LENGTH_SHORT).show();
        } else {
            mSlidingLayout.setAdapter(mAdapter);
            mSlidingLayout.setSlider(new PageSlider());
            Toast.makeText(this, "已切换为左右平移模式", Toast.LENGTH_SHORT).show();
        }
        mPagerMode = !mPagerMode;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item){
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
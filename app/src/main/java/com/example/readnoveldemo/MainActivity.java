package com.example.readnoveldemo;
import android.os.Environment;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.readnoveldemo.adapter.MySlidingAdapter;
import com.example.readnoveldemo.util.NovelFactory;
import com.example.readnoveldemo.util.PopupWinUtil;
import com.example.readnoveldemo.util.SpUtil;
import com.martian.libsliding.SlidingLayout;
import com.martian.libsliding.slider.OverlappedSlider;
import com.martian.libsliding.slider.PageSlider;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private SlidingLayout mSlidingLayout;
    private boolean mPagerMode = false;
    private NovelFactory mFactory;
    private TextView mProgress,mTimer;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        mFactory = new NovelFactory(this);
        mFactory.setBgBitmap(R.mipmap.ic_bg_blue);
        try {
            mFactory.openbook(Environment.getExternalStorageDirectory()
                    .getAbsolutePath()+"/book.txt");
        } catch (IOException e){
            e.printStackTrace();
        }
        setContentView(R.layout.activity_main);
        mProgress = (TextView) findViewById(R.id.tv_progress);
        mTimer = (TextView) findViewById(R.id.tv_time);
        mSlidingLayout = (SlidingLayout) findViewById(R.id.sliding_container);
        mSlidingLayout.setOnTapListener(new SlidingLayout.OnTapListener() {

            @Override
            public void onSingleTap(MotionEvent event){
                int screenWidth = getResources().getDisplayMetrics().widthPixels;
                int x = (int) event.getX();
                if (x > screenWidth*0.7) {
                    mSlidingLayout.slideNext();
                } else if (x <= screenWidth*0.3) {
                    mSlidingLayout.slidePrevious();
                }else{
                    showPopupWin();
                }
            }
        });
        // 默认为左右平移模式
        switchSlidingMode();
    }

    private void showPopupWin() {
        final PopupWindow win = PopupWinUtil.createPopupWindow(this,R.layout.v_popup);
        PopupWinUtil.show(this,win);
        View content = win.getContentView();
        Button btn_switch = (Button) content.findViewById(R.id.btn_switch);
        Button btn_catalog = (Button) content.findViewById(R.id.btn_catalog);
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId()==R.id.btn_switch){
                    switchSlidingMode();
                }
                win.dismiss();
            }
        };
        btn_catalog.setOnClickListener(listener);
        btn_switch.setOnClickListener(listener);

    }

    private void switchSlidingMode(){
        final SpUtil spUtil = new SpUtil(this);
        int pos = spUtil.getHistoryPos();
        MySlidingAdapter mAdapter = new MySlidingAdapter(this,mFactory,pos);
        mAdapter.setOnPageChangedListener(new MySlidingAdapter.OnPageChangedListener(){
            @Override
            public void onProgress(float progress,int pos){
                DecimalFormat format = new DecimalFormat("0.00%");
                Date date = new Date();
                SimpleDateFormat ft = new SimpleDateFormat("HH:mm");
                mTimer.setText(ft.format(date));
                mProgress.setText(format.format(progress));
                spUtil.setHistoryPos(pos);
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
        if (item.getItemId() == R.id.action_switch){
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
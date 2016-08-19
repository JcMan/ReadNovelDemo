package com.example.readnoveldemo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.readnoveldemo.adapter.CommonAdapter;
import com.example.readnoveldemo.adapter.ViewHolder;
import com.example.readnoveldemo.util.DialogUtil;
import com.example.readnoveldemo.util.NovelFactory;
import com.example.readnoveldemo.util.StringUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jcman on 16-8-19.
 */
public class ChapterActivity extends AppCompatActivity{

    private ListView mListView;
    private MyAdapter mAdapter;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chapter);
        mListView = (ListView) findViewById(R.id.listview);
        setTitle("目录");
        NovelFactory factory = new NovelFactory(this);
        try {
            factory.openbook(Environment.getExternalStorageDirectory().getAbsolutePath()+"/book.txt");
            factory.getChapters(new NovelFactory.OnChapterListener() {
                @Override
                public void onStart() {
                    DialogUtil.show(ChapterActivity.this);
                }

                @Override
                public void onLoading(Chapter chapter) {

                }

                @Override
                public void onFinished(final List<Chapter> list){
                    DialogUtil.dimiss(ChapterActivity.this);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run(){
                            List<Chapter> mList = new ArrayList<>();
                            for(int i=0;i<list.size();i++){
                                if(i+1<list.size()){
                                    if (StringUtil.contains(list.get(i).mTitle,list.get(i+1).mTitle)){
                                        mList.add(list.get(i));
                                        i++;
                                    }else{
                                        mList.add(list.get(i));
                                    }
                                }else
                                    mList.add(list.get(i));
                            }
                            mAdapter = new MyAdapter(ChapterActivity.this,mList,R.layout.item_chapter);
                            mListView.setAdapter(mAdapter);
                        }
                    });

                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int pos = ((Chapter)mListView.getItemAtPosition(position)).mPos;
                Intent intent = new Intent();
                intent.putExtra("pos",pos);
                setResult(102,intent);
                finish();
            }
        });
    }

    private class MyAdapter extends CommonAdapter<Chapter>{

        public MyAdapter(Context context, List<Chapter> list, int layoutId) {
            super(context, list, layoutId);
        }

        @Override
        public void convert(ViewHolder holder, Chapter chapter, int pos) {
            holder.setText(R.id.tv_chapter_name,chapter.mTitle);
        }
    }
}

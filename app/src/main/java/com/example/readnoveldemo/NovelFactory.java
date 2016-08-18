package com.example.readnoveldemo;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Vector;

/**
 * Created by jcman on 16-8-18.
 */
public class NovelFactory {

    private MappedByteBuffer mBuf = null;
    private int mBufLength = 0;
    private int mCurrentPos = 0;
    private String mCharsetName = "GBK";
    private Bitmap mPageBg = null;
    private Vector<String> mLines = new Vector<>();

    private int mFontSize = 23; //字体大小
    private int mLineSpace; //行距
    private int mTextColor = Color.parseColor("#333333");  //字体颜色
    private int mBackColor = Color.TRANSPARENT;
    private int marginWidth = 15; // 左右与边缘的距离
    private int marginHeight = 20; // 上下与边缘的距离
    private int mLineCount; // 每页可以显示的行数
    private float mVisibleHeight; // 绘制内容的宽
    private float mVisibleWidth; // 绘制内容的宽
    private boolean mIsfirstPage,mIslastPage;
    private int mWidth,mHeight;
    private Paint mPaint;
    private Context mContext;

    public NovelFactory(Activity activity) {
        mContext = activity;
        WindowManager manager = activity.getWindowManager();
        Display display = manager.getDefaultDisplay();
        mWidth  =  display.getWidth();
        mHeight =  display.getHeight();
        marginWidth = (int) (mWidth*0.04);
        mFontSize = (int) (mWidth*0.045);
        marginHeight = (int) (mHeight*0.03);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setTextAlign(Paint.Align.LEFT);
        mPaint.setTextSize(mFontSize);
        mPaint.setColor(mTextColor);
        mVisibleWidth = mWidth - marginWidth * 2;
        mLineSpace = (int) (mFontSize*0.7);
        mVisibleHeight = (float) (mHeight - marginHeight * 2-mLineSpace*0.73);
        mLineCount = (int) (mVisibleHeight / (1.0*(mFontSize+mLineSpace)));
    }

    public void openbook(String strFilePath) throws IOException {
        File file = new File(strFilePath);
        long lLen = file.length();
        mBufLength = (int) lLen;
        mBuf = new RandomAccessFile(file, "r").getChannel().map(
                FileChannel.MapMode.READ_ONLY, 0, lLen);
    }

    //向前读取一个段落
    protected byte[] readParagraphBack(int pos){
        int nEnd = pos-1;
        int i;
        byte b0;
        i = nEnd - 1;
        while (i > 0){
            b0 = mBuf.get(i);
            if (b0 == 0x0a && i != nEnd - 1){
                i++;
                break;
            }
            i--;
        }
        if (i < 0)
            i = 0;
        int nParaSize = nEnd - i;
        int j;
        byte[] buf = new byte[nParaSize];
        for (j = 0; j < nParaSize; j++) {
            buf[j] = mBuf.get(i + j);
        }
        return buf;
    }

    //向后读取一个段落
    protected byte[] readParagraphForward(int pos){
        int nStart = pos;
        int i = nStart;
        byte b0;
        while (i < mBufLength){
            b0 = mBuf.get(i++);
            if (b0 == 0x0a){
                break;
            }
        }
        int nParaSize = i - nStart;
        byte[] buf = new byte[nParaSize];
        for (i = 0; i < nParaSize; i++){
            buf[i] = mBuf.get(pos + i);
        }
        return buf;
    }

    /**
     * 得到下一页的内容
     * @return
     */
    protected Vector<String> pageDown(){
        String strParagraph = "";
        Vector<String> lines = new Vector<>();
        while (lines.size() < mLineCount && mCurrentPos < mBufLength){
            byte[] paraBuf = readParagraphForward(mCurrentPos); // 读取一个段落
            mCurrentPos += paraBuf.length;
            try {
                strParagraph = new String(paraBuf, mCharsetName);
            } catch (UnsupportedEncodingException e){
                e.printStackTrace();
            }
            if (strParagraph.indexOf("\r\n") != -1){
                strParagraph = strParagraph.replaceAll("\r\n", "");
            } else if (strParagraph.indexOf("\n") != -1){
                strParagraph = strParagraph.replaceAll("\n", "");
            }
            if(strParagraph.startsWith(" ")){
                strParagraph = strParagraph.replaceAll(" ","");
                strParagraph = "        "+strParagraph;
            }
            while (strParagraph.length() > 0){
                int nSize = mPaint.breakText(strParagraph, true, mVisibleWidth,
                        null);
                lines.add(strParagraph.substring(0, nSize));
                strParagraph = strParagraph.substring(nSize);
                if (lines.size() >= mLineCount) {
                    break;
                }
            }
            if (strParagraph.length() != 0){
                try {
                    mCurrentPos -= (strParagraph)
                            .getBytes(mCharsetName).length;
                } catch (UnsupportedEncodingException e){
                    e.printStackTrace();
                }
            }
        }
        return lines;
    }

    protected Vector<String> pageUp(){
        if (mCurrentPos<0)
            mCurrentPos = 0;
        Vector<String> lines = new Vector<>();
        String strParagraph = "";
        while (lines.size() < mLineCount && mCurrentPos > 0){
            Vector<String> paraLines = new Vector<>();
            byte[] paraBuf = readParagraphBack(mCurrentPos);
            mCurrentPos -= paraBuf.length;
            try {
                strParagraph = new String(paraBuf, mCharsetName);
            } catch (UnsupportedEncodingException e){
                e.printStackTrace();
            }
            strParagraph = strParagraph.replaceAll("\r\n", "");
            if (strParagraph.length() == 0){
                paraLines.add(strParagraph);
            }
            while (strParagraph.length() > 0){
                int nSize = mPaint.breakText(strParagraph, true, mVisibleWidth,
                        null);
                paraLines.add(strParagraph.substring(0, nSize));
                strParagraph = strParagraph.substring(nSize);
            }
            lines.addAll(0, paraLines);
        }
        while (lines.size() > mLineCount) {
            try {
                mCurrentPos += lines.get(0).getBytes(mCharsetName).length;
                lines.remove(0);
            } catch (UnsupportedEncodingException e){
                e.printStackTrace();
            }
        }
        return lines;
    }

    public Bitmap drawCancas(Vector<String> lines){
        Bitmap bitmap = Bitmap.createBitmap(mWidth,mHeight,Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        if (lines.size() > 0){
            if (mPageBg == null)
                canvas.drawColor(mBackColor);
            else
                canvas.drawBitmap(mPageBg, 0, 0, null);
            int y = marginHeight;
            for (String strLine : lines){
                y += (mFontSize+mLineSpace);
                canvas.drawText(strLine, marginWidth, y, mPaint);
            }
        }
        return bitmap;
    }

    public void setBgBitmap(int resId){
        mPageBg = BitmapFactory.decodeResource(mContext.getResources(),resId);
    }

    public long getCurrentPos(){
        return mCurrentPos;
    }

    public Bitmap getNextPageBitmap(){
        return drawCancas(pageDown());
    }

    public Bitmap getPrePageBitmap(){
        return drawCancas(pageUp());
    }

    public boolean IsLastPage(){
        return mCurrentPos>=mBufLength;
    }

    public boolean IsFirstPage(){
        return mCurrentPos<=0;
    }
}

/**
 *  Author :  hmg25
 *  Description :
 */
package com.example.readnoveldemo;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.view.Display;
import android.view.WindowManager;

public class BookPageFactory {

	private File book_file = null;
	private MappedByteBuffer m_mbBuf = null;
	private int m_mbBufLen = 0;
	private int mStartPos = 0;
	private String m_strCharsetName = "GBK";
	private Bitmap m_book_bg = null;
	private int mWidth;
	private int mHeight;

	private Vector<String> m_lines = new Vector<String>();


	private int m_fontSize = 23; //字体大小
	private int mLineSpace;//行距
	private int m_textColor = Color.parseColor("#333333");  //字体颜色
	private int m_backColor = Color.TRANSPARENT;
	private int marginWidth = 15; // 左右与边缘的距离
	private int marginHeight = 20; // 上下与边缘的距离
	private int mLineCount; // 每页可以显示的行数
	private float mVisibleHeight; // 绘制内容的宽
	private float mVisibleWidth; // 绘制内容的宽
	private boolean m_isfirstPage,m_islastPage;
	private Paint mPaint;
	private Context mContext;


	public BookPageFactory(Activity activity) {
		mContext = activity;
		WindowManager manager = activity.getWindowManager();
		Display display = manager.getDefaultDisplay();
		mWidth  =  display.getWidth();
		mHeight =  display.getHeight();
		marginWidth = (int) (mWidth*0.04);
		m_fontSize = (int) (mWidth*0.045);
		marginHeight = (int) (mHeight*0.03);
		init();
	}
	private void init() {
		mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaint.setTextAlign(Align.LEFT);
		mPaint.setTextSize(m_fontSize);
		mPaint.setColor(m_textColor);
		mVisibleWidth = mWidth - marginWidth * 2;
		mLineSpace = (int) (m_fontSize*0.7);
		mVisibleHeight = (float) (mHeight - marginHeight * 2-mLineSpace*0.73);
		mLineCount = (int) (mVisibleHeight / (m_fontSize+mLineSpace));
	}
	@SuppressWarnings("resource")
	public void openbook(String strFilePath) throws IOException {
		book_file = new File(strFilePath);
		long lLen = book_file.length();
		m_mbBufLen = (int) lLen;
		m_mbBuf = new RandomAccessFile(book_file, "r").getChannel().map(
				FileChannel.MapMode.READ_ONLY, 0, lLen);
	}


	protected byte[] readParagraphBack(int nFromPos) {
		int nEnd = nFromPos;
		int i;
		byte b0, b1;
		i = nEnd - 1;
		while (i > 0) {
			b0 = m_mbBuf.get(i);
			if (b0 == 0x0a && i != nEnd - 1) {
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
			buf[j] = m_mbBuf.get(i + j);
		}
		return buf;
	}

	// 读取上一段落
	protected byte[] readParagraphForward(int nFromPos) {
		int nStart = nFromPos;
		int i = nStart;
		byte b0, b1;
		// 根据编码格式判断换行
		if (m_strCharsetName.equals("UTF-16LE")){
			while (i < m_mbBufLen - 1){
				b0 = m_mbBuf.get(i++);
				b1 = m_mbBuf.get(i++);
				if (b0 == 0x0a && b1 == 0x00) {
					break;
				}
			}
		} else if (m_strCharsetName.equals("UTF-16BE")){
			while (i < m_mbBufLen - 1){
				b0 = m_mbBuf.get(i++);
				b1 = m_mbBuf.get(i++);
				if (b0 == 0x00 && b1 == 0x0a){
					break;
				}
			}
		} else {
			while (i < m_mbBufLen){
				b0 = m_mbBuf.get(i++);
				if (b0 == 0x0a){
					break;
				}
			}
		}
		int nParaSize = i - nStart;
		byte[] buf = new byte[nParaSize];
		for (i = 0; i < nParaSize; i++){
			buf[i] = m_mbBuf.get(nFromPos + i);
		}
		return buf;
	}

	protected Vector<String> pageDown(){
		String strParagraph = "";
		Vector<String> lines = new Vector<String>();
		while (lines.size() < mLineCount && mStartPos < m_mbBufLen){
			byte[] paraBuf = readParagraphForward(mStartPos); // 读取一个段落
			mStartPos += paraBuf.length;
			try {
				strParagraph = new String(paraBuf, m_strCharsetName);
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
					mStartPos -= (strParagraph)
							.getBytes(m_strCharsetName).length;
				} catch (UnsupportedEncodingException e){
					e.printStackTrace();
				}
			}
		}
		return lines;
	}

	protected Vector<String> pageUp(){
		if (mStartPos < 0)
			mStartPos = 0;
		Vector<String> lines = new Vector<>();
		String strParagraph = "";
		while (lines.size() < mLineCount && mStartPos > 0){
			Vector<String> paraLines = new Vector<>();
			byte[] paraBuf = readParagraphBack(mStartPos);
			mStartPos -= paraBuf.length;
			try {
				strParagraph = new String(paraBuf, m_strCharsetName);
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
				mStartPos += lines.get(0).getBytes(m_strCharsetName).length;
				lines.remove(0);
			} catch (UnsupportedEncodingException e){
				e.printStackTrace();
			}
		}
		return lines;
	}

	protected void prePage() throws IOException {
		if (mStartPos <= 0){
			mStartPos = 0;
			m_isfirstPage=true;
			return;
		}else m_isfirstPage = false;
		m_lines.clear();
		pageUp();
		m_lines = pageDown();
	}

	public void nextPage() throws IOException{
		if (mStartPos >= m_mbBufLen){
			m_islastPage=true;
			return;
		}else
			m_islastPage=false;
		m_lines.clear();
		m_lines = pageDown();
	}

	public void onDraw(Canvas c){
		if (m_lines.size() == 0)
			m_lines = pageDown();
		if (m_lines.size() > 0){
			if (m_book_bg == null)
				c.drawColor(m_backColor);
			else
				c.drawBitmap(m_book_bg, 0, 0, null);
			int y = marginHeight;
			for (String strLine : m_lines){
				y += (m_fontSize+mLineSpace);
				c.drawText(strLine, marginWidth, y, mPaint);
			}
		}
	}

	public void setBgBitmap(Bitmap BG){
		m_book_bg = BG;
	}

	public void setBgBitmap(int resId){
		m_book_bg = BitmapFactory.decodeResource(mContext.getResources(),resId);
	}
	public boolean isfirstPage(){
		return m_isfirstPage;
	}
	public boolean islastPage(){
		return m_islastPage;
	}

	public void setBeginPosition(int pos){
		mStartPos = pos;
	}

	private String getNextString() {
		byte[] buf = readParagraphForward(mStartPos);
		mStartPos+=buf.length;
		String s = "";
		try {
			s = new String(buf,m_strCharsetName);
		} catch (UnsupportedEncodingException e){
			e.printStackTrace();
		}
		return s;
	}

	/**
	 * 得到每一章的对应的十几页的Bitmap
	 */
	public List<Bitmap> getChapterContentBitmaps(){
		List<Bitmap> _List = new ArrayList<Bitmap>();
		boolean flag = true;
		while(flag){
			try {
				nextPage();
				if (islastPage()){
					flag = false;
				}else{
					Bitmap bitmap = drawCancas();
					_List.add(bitmap);
				}
			} catch (IOException e) {}
		}
		return _List;
	}

	public Bitmap getNextPageBitmap(){
		Bitmap bitmap = null;
		if (islastPage()){
			return bitmap;
		}
		try {
			nextPage();
			bitmap = drawCancas();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return bitmap;
	}

	public Bitmap getPrePageBitmap(){
		Bitmap bitmap = null;
		try {
			prePage();
			bitmap = drawCancas();
		} catch (IOException e){
			e.printStackTrace();
		}
		return bitmap;
	}

	public Bitmap drawCancas(){
		Bitmap bitmap = Bitmap.createBitmap(mWidth,mHeight,Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(bitmap);
		if (m_lines.size() == 0)
			m_lines = pageDown();
		if (m_lines.size() > 0){
			if (m_book_bg == null)
				canvas.drawColor(m_backColor);
			else
				canvas.drawBitmap(m_book_bg, 0, 0, null);
			int y = marginHeight;
			for (String strLine : m_lines){
				y += (m_fontSize+mLineSpace);
				canvas.drawText(strLine, marginWidth, y, mPaint);
			}
		}
		return bitmap;
	}

	public Bitmap drawCancas(Vector<String> lines){
		Bitmap bitmap = Bitmap.createBitmap(mWidth,mHeight,Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(bitmap);
		if (lines.size() > 0){
			if (m_book_bg == null)
				canvas.drawColor(m_backColor);
			else
				canvas.drawBitmap(m_book_bg, 0, 0, null);
			int y = marginHeight;
			for (String strLine : lines){
				y += (m_fontSize+mLineSpace);
				canvas.drawText(strLine, marginWidth, y, mPaint);
			}
		}
		return bitmap;
	}
}
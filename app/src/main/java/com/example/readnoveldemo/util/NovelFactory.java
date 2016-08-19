package com.example.readnoveldemo.util;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;
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
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import com.example.readnoveldemo.Chapter;

public class NovelFactory {

	private File book_file = null;
	private MappedByteBuffer m_mbBuf = null;//MappedByteBuffer 将文件直接映射到内存
	private int m_mbBufLen = 0;
	private int m_mbBufBegin = 0;
	private int m_mbBufEnd = 0;
	private String m_strCharsetName = "gbk";//文本格式
	private Bitmap m_book_bg = null;//文本图像
	private int mWidth;
	private int mHeight;

	private Vector<String> m_lines = new Vector<String>();
	//用于一行一行显示
	private int m_fontSize = 24;
	private int m_textColor = Color.BLACK;//字体颜色
	private int m_backColor = Color.WHITE; // 背景颜色
	private int marginWidth = 15; // 左右与边缘的距离
	private int marginHeight = 20; // 上下与边缘的距离

	private int mLineCount; // 每页可以显示的行数
	private float mVisibleHeight; // 绘制内容的宽
	private float mVisibleWidth; // 绘制内容的宽
	private boolean m_isfirstPage,m_islastPage;

	 private int m_nLineSpaceing = 5;

	private Paint mPaint;
	private Context mContext;

	//设置阅读界面，包括字体，显示多少行
	public NovelFactory(Activity activity){
		WindowManager manager = activity.getWindowManager();
		Display display = manager.getDefaultDisplay();
		mWidth = display.getWidth();
		mHeight = display.getHeight();//获得宽和高
		mContext = activity;
		marginWidth = (int) (mWidth*0.04);
		marginHeight = (int) (mHeight*0.038);
		mVisibleWidth = mWidth - marginWidth * 2;
		mVisibleHeight = mHeight - marginHeight * 2;//绘制的内容宽和高
		m_fontSize = (int) (mWidth*0.045);
		m_nLineSpaceing = (int) (m_fontSize*0.7);
		mLineCount = (int) (mVisibleHeight / (m_fontSize+m_nLineSpaceing)); // 可显示的行数
		mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaint.setTextAlign(Align.LEFT);//设置文本对齐方式
		mPaint.setTextSize(m_fontSize);//设置字体大小
		mPaint.setColor(m_textColor);//设置颜色

	}

	//获得文件，并映射到内存
	public void openbook(String strFilePath) throws IOException{
		book_file = new File(strFilePath);
		SpUtil spUtil = new SpUtil(mContext);
		if (spUtil.getEncodingName(strFilePath).length()==0){
			m_strCharsetName = EncodingDetect.getJavaEncode(book_file.getAbsolutePath());
			spUtil.setEncodingName(strFilePath,m_strCharsetName);
		}else{
			m_strCharsetName = spUtil.getEncodingName(strFilePath);
		}
		long lLen = book_file.length();
		m_mbBufLen = (int) lLen;
		m_mbBuf = new RandomAccessFile(book_file, "r").getChannel().map(
				FileChannel.MapMode.READ_ONLY, 0, lLen);
	}


	//读一段
	protected byte[] readParagraphBack(int nFromPos) {
		int nEnd = nFromPos;//字符缓存开始的位置
		int i;
		byte b0, b1;
		if (m_strCharsetName.equals("UTF-16LE")) {
			i = nEnd - 2;//？
			while (i > 0) {
				b0 = m_mbBuf.get(i);
				b1 = m_mbBuf.get(i + 1);
				if (b0 == 0x0a && b1 == 0x00 && i != nEnd - 2) {
					i += 2;
					break;
				}
				i--;
			}

		} else if (m_strCharsetName.equals("UTF-16BE")) {
			i = nEnd - 2;
			while (i > 0) {
				b0 = m_mbBuf.get(i);//返回指定索引
				b1 = m_mbBuf.get(i + 1);
				if (b0 == 0x00 && b1 == 0x0a && i != nEnd - 2) {
					i += 2;
					break;
				}//过段时
				i--;
			}
		} else {
			i = nEnd - 1;//？
			while (i > 0) {
				b0 = m_mbBuf.get(i);
				if (b0 == 0x0a && i != nEnd - 1) {
					i++;
					break;
				}//过段时
				i--;
			}
		}
		if (i < 0)
			i = 0;
		//i是过段的索引位置
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
		int nStart = nFromPos;//字符缓存开始的位置
		int i = nStart;
		byte b0;
		// 根据编码格式判断换行
		while (i < m_mbBufLen) {
			b0 = m_mbBuf.get(i++);
			if (b0 == 0x0a) {
				break;
			}
		}

		//这使i在换行的索引位置
		int nParaSize = i - nStart;
		byte[] buf = new byte[nParaSize];
		for (i = 0; i < nParaSize; i++) {
			buf[i] = m_mbBuf.get(nFromPos + i);
		}//把读到的段输入字节流中
		return buf;
	}

	protected Vector<String> pageDown() {
		String strParagraph = "";
		Vector<String> lines = new Vector<String>();
		while (lines.size() < mLineCount && m_mbBufEnd < m_mbBufLen) {
			//不能大于给定的最多行数
			byte[] paraBuf = readParagraphForward(m_mbBufEnd); // 读取一个段落
			m_mbBufEnd += paraBuf.length;
			//减去读到的长度，作为下一个结束的地步
			try {
				strParagraph = new String(paraBuf, m_strCharsetName);
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			String strReturn = "";
			if (strParagraph.indexOf("\r\n") != -1) {
				//"\r\n"在字符串中则，下同
				strReturn = "\r\n";
				strParagraph = strParagraph.replaceAll("\r\n", "");
			} else if (strParagraph.indexOf("\n") != -1){
				strReturn = "\n";
				strParagraph = strParagraph.replaceAll("\n", "");
			}
			while (strParagraph.length() > 0) {
				int nSize = mPaint.breakText(strParagraph, true, mVisibleWidth,
						null);
				//返回刚好要超过规定长度mVisibleWidth的值
				lines.add(strParagraph.substring(0, nSize));
				strParagraph = strParagraph.substring(nSize);
				if (lines.size() >= mLineCount) {//超过规定的行数
					break;
				}
			}
			if (strParagraph.length() != 0) {
				try {
					m_mbBufEnd -= (strParagraph + strReturn)
							.getBytes(m_strCharsetName).length;
					//即返回字符串在GBK、UTF-8和ISO8859-1编码下的byte数组表示
					//目的在于把m_mbBufEnd改成指向下一行
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return lines;
	}
	//设置文本显示
	//预防文本超过界面规定的范围
	protected Vector<String> pageUp() {
		if (m_mbBufBegin < 0)
			m_mbBufBegin = 0;
		Vector<String> lines = new Vector<String>();
		String strParagraph = "";
		while (lines.size() < mLineCount && m_mbBufEnd > 0) {
			//不能大于给定的最多行数
			Vector<String> paraLines = new Vector<String>();
			byte[] paraBuf = readParagraphBack(m_mbBufEnd);
			//读到一段
			//从头开始读
			m_mbBufEnd -= paraBuf.length;
			//减去读到的长度，作为下一个开始要读的标志
			try {
				strParagraph = new String(paraBuf, m_strCharsetName);
				//第一个参数读到的字符串，第二个参数是文本格式
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			strParagraph = strParagraph.replaceAll("\r\n", "");
			//用第二个参数的字符串替换第一个
			strParagraph = strParagraph.replaceAll("\n", "");
			//替换掉任何过行的记录

			while (strParagraph.length() > 0) {
				int nSize = mPaint.breakText(strParagraph, true, mVisibleWidth,
						null);
				//测量第一个字符串，与第三个参数即宽度相比
				paraLines.add(strParagraph.substring(0, nSize));
				//获得0到nSize的字符串
				strParagraph = strParagraph.substring(nSize);
				//字符串变成从nSize开始
			}//在于把字符串变成不会超过规定长度mVisibleWidth的字符串
			lines.addAll(0, paraLines);//加入所有Vector字符串列
		}
		while (lines.size() > mLineCount) {//超过规定行数时
			try {
				m_mbBufEnd += lines.get(0).getBytes(m_strCharsetName).length;
				//.get()表示返回指定位置的元素
				//String的getBytes()方法是得到一个操作系统默认的编码格式的字节数组
				//即返回字符串在GBK、UTF-8和ISO8859-1编码下的byte数组表示
				//目的在于把m_mbBufBegin改成指向下一行
				lines.remove(0);
				//删除指定位置的元素
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		//m_mbBufEnd = m_mbBufBegin;
		return lines;
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
				y += (m_fontSize+m_nLineSpaceing);
				canvas.drawText(strLine, marginWidth, y, mPaint);
			}
		}
		return bitmap;
	}

	private String getLineString(){
		byte[] buf = readParagraphForward(m_mbBufEnd);
		m_mbBufEnd+=buf.length;
		String s = "";
		try {
			s = new String(buf,m_strCharsetName);
		} catch (UnsupportedEncodingException e){
			e.printStackTrace();
		}
		return s;
	}

	private int getByteLength(String str){
		byte[] bytes = null;
		try {
			bytes = str.getBytes(m_strCharsetName);
		} catch (UnsupportedEncodingException e){
			e.printStackTrace();
		}
		return bytes.length;
	}

	public void getChapters(final OnChapterListener listener){
		m_mbBufEnd = 0;
		final List<Chapter> chapters = new ArrayList<>();
		try {
			listener.onStart();
			new Thread(new Runnable() {
				@Override
				public void run() {
					while(m_mbBufEnd<m_mbBufLen-1){
						String regex = "第.{1,8}章.{0,}\r\n";
						Pattern pattern = Pattern.compile(regex);
						Matcher matcher = null;
						String strLine = getLineString();
						matcher = pattern.matcher(strLine);
						if (matcher.find()){
							int pos = m_mbBufEnd - getByteLength(strLine);
							String title = matcher.group();
							Chapter chapter = new Chapter();
							chapter.mPos = pos;
							chapter.mTitle = title.replaceAll("\r\n","");
							chapters.add(chapter);
							listener.onLoading(chapter);
						}
					}
					listener.onFinished(chapters);
				}
			}).start();
		}catch (Exception e){

		}
	}

	public interface OnChapterListener{
		void onStart();
		void onLoading(Chapter chapter);
		void onFinished(List<Chapter> list);
	}

	public Bitmap getNextPageBitmap(){
		return drawCancas(pageDown());
	}

	public Bitmap getPrePageBitmap(){
		return drawCancas(pageUp());
	}

	/***********************************************/

	protected void prePage() throws IOException {
		if (m_mbBufBegin <= 0) {
			m_mbBufBegin = 0;
			m_isfirstPage=true;
			return;
		}else
			m_isfirstPage=false;
		m_lines.clear();
		//删除所有元素
		pageUp();
		m_lines = pageDown();
		//把收集到的文本放到m_lines中
	}

	public void nextPage() throws IOException {
		if (m_mbBufEnd >= m_mbBufLen) {
			m_islastPage=true;
			return;
		}else m_islastPage=false;
		m_lines.clear();
		m_mbBufBegin = m_mbBufEnd;
		m_lines = pageDown();
	}

	public void setEndPos(int pos){
		m_mbBufEnd = pos;
	}

	public int getEndPos(){
		return m_mbBufEnd;
	}

	public int getBufLength(){
		return m_mbBufLen;
	}

	public void onDraw(Canvas c) {
		if (m_lines.size() == 0)
			m_lines = pageDown();
		//现在m_lines的格式是按照
		//界面阅读规定的，即有多宽的，有多行
		if (m_lines.size() > 0) {
			if (m_book_bg == null)
				c.drawColor(m_backColor);//设置背景颜色
			else
				c.drawBitmap(m_book_bg, 0, 0, null);
			int y = marginHeight;
			for (String strLine : m_lines) {
				y += (m_fontSize+m_nLineSpaceing);
				c.drawText(strLine, marginWidth, y, mPaint);
				//给界面的每一行绘制
			}
		}
		float fPercent = (float) (m_mbBufBegin * 1.0 / m_mbBufLen);
		DecimalFormat df = new DecimalFormat("#0.0");//用于格式化十进制数字
		//即按照参数的格式输出
		String strPercent = df.format(fPercent * 100) + "%";
		int nPercentWidth = (int) mPaint.measureText("999.9%") + 1;
		//返回字符串的宽度
		c.drawText(strPercent, mWidth - nPercentWidth, mHeight - 5, mPaint);
	}

	//绘制图像
	public void setBgBitmap(Bitmap BG) {
		m_book_bg = BG;
	}

	public void setBgBitmap(int resId) {
		m_book_bg = BitmapFactory.decodeResource(mContext.getResources(),resId);
	}

	//返回第一页
	public boolean isfirstPage(){
		return m_mbBufEnd<=0;
	}
	//返回最后一页
	public boolean islastPage() {
		return m_mbBufEnd>=m_mbBufLen;
	}
}
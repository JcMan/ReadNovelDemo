/**
 *  Author :  hmg25
 *  Description :
 */
package com.example.readnoveldemo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

/**
 * hmg25's android Type
 *
 *@author Administrator
 *
 */
public class ReadView  extends View{

	private Bitmap bitmap;
	
	/**
	 * @param context
	 */
	public ReadView(Context context) {
		super(context);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		canvas.drawBitmap(bitmap, 0, 0, new Paint());
		super.onDraw(canvas);
	}
	
	public void setBitmap(Bitmap bitmap) {
		this.bitmap = bitmap;
		invalidate();
		
	}
	
	

}

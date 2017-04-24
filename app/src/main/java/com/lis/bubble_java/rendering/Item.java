package com.lis.bubble_java.rendering;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;

import com.lis.bubble_java.model.BubbleGradient;
import com.lis.bubble_java.model.PickerItem;
import com.lis.bubble_java.physics.CircleBody;

import org.jbox2d.common.Vec2;

import static android.opengl.GLES20.GL_LINEAR;
import static android.opengl.GLES20.GL_TEXTURE;
import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_TEXTURE_MAG_FILTER;
import static android.opengl.GLES20.GL_TEXTURE_MIN_FILTER;
import static android.opengl.GLES20.GL_TRIANGLE_STRIP;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glGenTextures;
import static android.opengl.GLES20.glTexParameteri;
import static com.lis.bubble_java.rendering.BubbleShader.U_MATRIX;

/**
 * Created by lis on 2017/4/19.
 */

public class Item {
	PickerItem pickerItem;
	CircleBody circleBody;
	Float x, y, radius;
	Vec2 initialPosition;
	Vec2 currentPosition;

	public Item(PickerItem pickerItem, CircleBody circleBody) {
		this.pickerItem = pickerItem;
		this.circleBody = circleBody;
		x = circleBody.getPhysicalBody().getPosition().x;
		y = circleBody.getPhysicalBody().getPosition().y;
		radius = circleBody.radius;
		initialPosition = circleBody.position;
		currentPosition = circleBody.getPhysicalBody().getPosition();
	}

	public float getX() {
		return  x = circleBody.getPhysicalBody().getPosition().x;
	}
	public float getY() {
		return  y = circleBody.getPhysicalBody().getPosition().y;
	}
	public float getRadius(){return  radius= circleBody.radius;}
	private int texture, imageTexture = 0;

	public void bindTextures(int[] ints, int index) {
		texture = bindTexture(ints, index * 2, false);
		imageTexture = bindTexture(ints, index * 2 + 1, true);
	}

	private int bindTexture(int[] textureIds, int index, boolean withImage) {
		glGenTextures(1, textureIds, index);
		Bitmap bitmap = createBitmap(withImage);
		glActiveTexture(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_2D, textureIds[index]);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		GLUtils.texImage2D(GL_TEXTURE_2D, 0, bitmap, 0);
		bitmap.recycle();
		glBindTexture(GL_TEXTURE_2D, 0);
		return textureIds[index];
	}

	private float bitmapSize = 300f;

	private Bitmap createBitmap(boolean isSelected) {
		Bitmap bitmap = Bitmap.createBitmap((int) bitmapSize, (int) bitmapSize, Bitmap.Config.ARGB_4444);
		Bitmap.Config bitmapConfig =
				bitmap.getConfig() == null ? Bitmap.Config.ARGB_8888 : bitmap.getConfig();
		bitmap = bitmap.copy(bitmapConfig, true);
		Canvas canvas = new Canvas(bitmap);
		if (isSelected) {
			drawImage(canvas);
		}
		drawBackground(canvas, isSelected);
		drawIcon(canvas);
		drawText(canvas);

		return bitmap;
	}

	private void drawText(Canvas canvas) {
		if (pickerItem.title == null || pickerItem.textColor == null) return;
		TextPaint paint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
		paint.setColor(pickerItem.textColor);
		paint.setTextSize(pickerItem.textSize);
		paint.setTypeface(pickerItem.typeface);
		float maxTextHeight = pickerItem.icon == null ? bitmapSize / 2f : bitmapSize / 2.7f;
		StaticLayout textLayout = placeText(paint);

		while (textLayout.getHeight() > maxTextHeight) {
			float size = paint.getTextSize();
			paint.setTextSize(size--);
			textLayout = placeText(paint);
		}

		if (pickerItem.icon == null) {
			canvas.translate(
					(bitmapSize - textLayout.getWidth()) / 2f,
					(bitmapSize - textLayout.getHeight()) / 2f);
		} else if (pickerItem.iconOnTop) {
			canvas.translate((bitmapSize - textLayout.getWidth()) / 2f, bitmapSize / 2f);
		} else {
			canvas.translate(
					(bitmapSize - textLayout.getWidth()) / 2f,
					bitmapSize / 2 - textLayout.getHeight());
		}

		textLayout.draw(canvas);
	}

	private StaticLayout placeText(TextPaint paint) {
		return new StaticLayout(pickerItem.title, paint, (int) (bitmapSize * 0.9),
				                       Layout.Alignment.ALIGN_CENTER, 1.0f, 0.0f, false);
	}

	private void drawIcon(Canvas canvas) {
		if (pickerItem.icon != null) {
			int width = pickerItem.icon.getIntrinsicWidth();
			int height = pickerItem.icon.getIntrinsicHeight();
			int left = (int) (bitmapSize / 2 - width / 2);
			int right = (int) (bitmapSize / 2 + width / 2);
			if (pickerItem.title == null) {
				pickerItem.icon.setBounds(new Rect(left, (int) (bitmapSize / 2 -
				                                                height /
				                                                2), right, (int) (bitmapSize /
				                                                                  2 +
				                                                                  height /
				                                                                  2)));
			} else if (pickerItem.iconOnTop) {
				pickerItem.icon.setBounds(new Rect(left, (int) (bitmapSize / 2 -
				                                                height), right, (int) (bitmapSize /
				                                                                       2)));
			} else {
				pickerItem.icon.setBounds(new Rect(left, (int) (bitmapSize /
				                                                2), right, (int) (bitmapSize / 2 +
				                                                                  height)));
			}
			pickerItem.icon.draw(canvas);
		}
	}


	LinearGradient getGradient(BubbleGradient bubble) {
		boolean horizontal = bubble.direction == BubbleGradient.HORIZONTAL;
		return new LinearGradient(horizontal ? 0f : bitmapSize / 2f,
				                         horizontal ? bitmapSize / 2f : 0f,
				                         horizontal ? bitmapSize : bitmapSize / 2f,
				                         horizontal ? bitmapSize / 2f : bitmapSize,
				                         bubble.startColor, bubble.endColor, Shader.TileMode.CLAMP);
	}

	private void drawBackground(Canvas canvas, boolean withImage) {
		Paint bgPaint = new Paint();
		bgPaint.setStyle(Paint.Style.FILL);
		if (pickerItem.color != 0) {
			bgPaint.setColor(pickerItem.color);
		}
		if (pickerItem.gradient != null) {
			bgPaint.setShader(getGradient(pickerItem.gradient));
		}
		//pickerItem.gradient ?.let { bgPaint.shader = gradient }
		if (withImage) bgPaint.setAlpha((int) (pickerItem.overlayAlpha * 255));
		canvas.drawRect(0f, 0f, bitmapSize, bitmapSize, bgPaint);
	}

	private void drawImage(Canvas canvas) {
		float height = ((BitmapDrawable) pickerItem.backgroundImage).getBitmap().getHeight();
		float width = ((BitmapDrawable) pickerItem.backgroundImage).getBitmap().getWidth();
		float ratio = Math.max(height, width) / Math.min(height, width);
		float bitmapHeight = height < width ? bitmapSize : bitmapSize * ratio;
		float bitmapWidth = height < width ? bitmapSize * ratio : bitmapSize;
		pickerItem.backgroundImage.setBounds(new Rect(0, 0, (int) bitmapWidth, (int) bitmapHeight));
		pickerItem.backgroundImage.draw(canvas);
	}

	public void drawItself(int programId, int index, float scaleX, float scaleY) {
		glActiveTexture(GL_TEXTURE);
		glBindTexture(GL_TEXTURE_2D, getCurrentTexture());
		GLES20.glUniform1i(GLES20.glGetUniformLocation(programId, BubbleShader.U_TEXT), 0);
		GLES20.glUniformMatrix4fv(GLES20.glGetUniformLocation(programId, U_MATRIX), 1, false, calculateMatrix(scaleX, scaleY), 0);
		GLES20.glDrawArrays(GL_TRIANGLE_STRIP, index * 4, 4);

	}

	private float[] calculateMatrix(float scaleX, float scaleY) {
		float[] floatList = new float[16];
		Matrix.setIdentityM(floatList, 0);
		Matrix.translateM(floatList, 0, currentPosition.x * scaleX - initialPosition.x,
				currentPosition.y * scaleY - initialPosition.y, 0f);
		return floatList;
	}

	public int getCurrentTexture() {
		return (circleBody.increased || circleBody.isIncreasing) ? imageTexture : texture;
	}
}

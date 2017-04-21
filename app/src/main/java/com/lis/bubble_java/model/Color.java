package com.lis.bubble_java.model;

/**
 * Created by lis on 2017/4/19.
 */

public class Color {
	public Float red;
	public Float green;
	public Float blue;
	public Float alpha;

	public Color(int background) {
		red = android.graphics.Color.red(background) / 256f;
		green = android.graphics.Color.green(background) / 256f;
		blue = android.graphics.Color.blue(background) / 256f;
		alpha = android.graphics.Color.alpha(background) / 256f;
	}
}

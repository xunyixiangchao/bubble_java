package com.lis.bubble_java.model;

import android.graphics.Typeface;
import android.graphics.drawable.Drawable;

/**
 * Created by lis on 2017/4/19.
 */

public class PickerItem {
	public String title;
	public Drawable icon;
	public boolean iconOnTop;
	public int color;
	public BubbleGradient gradient;
	public Float overlayAlpha = 0.5f;
	public Typeface typeface = Typeface.DEFAULT;
	public Integer textColor;
	public Float textSize;
	public Drawable backgroundImage;
	public Boolean isSelected=false;

	public PickerItem(String title, Drawable icon, Boolean iconOnTop, int color, BubbleGradient gradient,
	                  Float overlayAlpha, Typeface typeface, int textColor, Float textSize, Drawable backgroundImage,
	                  Boolean isSelected) {
		this.title = title;
		this.icon = icon;
		this.iconOnTop = iconOnTop;
		this.color = color;
		this.gradient = gradient;
		this.overlayAlpha = overlayAlpha;
		this.typeface = typeface;
		this.textColor = textColor;
		this.textSize = textSize;
		this.backgroundImage = backgroundImage;
		this.isSelected = isSelected;
	}
}

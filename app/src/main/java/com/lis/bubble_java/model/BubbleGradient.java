package com.lis.bubble_java.model;

/**
 * Created by lis on 2017/4/19.
 */

public class BubbleGradient {
	public static final int HORIZONTAL = 0;
	public static final int VERTICAL = 1;
	public int startColor;
	public int endColor;
	public int direction = HORIZONTAL;

	public BubbleGradient(int startColor, int endColor, int direction) {
		this.startColor = startColor;
		this.endColor = endColor;
		this.direction = direction;
	}
}

package com.lis.bubble_java.rendering;

import android.content.Context;
import android.content.res.TypedArray;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.lis.bubble_java.BubblePickerListener;
import com.lis.bubble_java.R;
import com.lis.bubble_java.model.Color;
import com.lis.bubble_java.model.PickerItem;

import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.List;

/**
 * Created by lis on 2017/4/19.
 */

public class BubblePicker extends GLSurfaceView {
	private int background = 0;
	private int maxSelectedCount;
	PickerRenderer renderer = new PickerRenderer(this);
	private Float startX = 0f;
	private Float startY = 0f;
	private Float previousX = 0f;
	private Float previousY = 0f;
	public ArrayList<PickerItem> items;
	int bubbleSize = 50;

	public List<PickerItem> getSelectedItems() {
		return renderer.getSelectedItems();
	}

	public void setBubbleSize(int bubbleSize) {
		this.bubbleSize = bubbleSize;
		if (0 < bubbleSize && bubbleSize < 101) {
			renderer.setBubbleSize(bubbleSize);
		}
	}


	public void setBackground(int background) {
		this.background = background;
		renderer.backgroundColor = new Color(background);
	}

	public void setItems(ArrayList<PickerItem> items) {
		if (items != null) {
			this.items = items;
		} else {
			this.items = new ArrayList<>();
		}
		renderer.items = this.items;
	}

	public void setMaxSelectedCount(int maxSelectedCount) {
		renderer.setMaxSelectedCount(maxSelectedCount);
	}

	public void setListener(BubblePickerListener listener) {
		renderer.listener = listener;
	}

	public BubblePicker(Context context) {
		super(context);
	}

	public BubblePicker(Context context, AttributeSet attrs) {
		super(context, attrs);
		setEGLContextClientVersion(2);
		setRenderer(renderer);
		//主动渲染模式
		setRenderMode(RENDERMODE_CONTINUOUSLY);
		TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.BubblePicker);
		if (array.hasValue(R.styleable.BubblePicker_maxSelectedCount)) {
			maxSelectedCount = array.getInt(R.styleable.BubblePicker_maxSelectedCount, -1);
			setMaxSelectedCount(maxSelectedCount);
		}

		if (array.hasValue(R.styleable.BubblePicker_backgroundColor)) {
			background = array.getColor(R.styleable.BubblePicker_backgroundColor, -1);
			setBackground(background);
		}

		array.recycle();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				startX = event.getX();
				startY = event.getY();
				previousX = event.getX();
				previousY = event.getY();
				break;
			case MotionEvent.ACTION_UP:
				if (isClick(event)) {
					renderer.resize(event.getX(), event.getY());
				}
				renderer.release();
				break;
			case MotionEvent.ACTION_MOVE:
				if (isSwipe(event)) {
					renderer.swipe(previousX - event.getX(), previousY - event.getY());
					previousX = event.getX();
					previousY = event.getY();
				} else {
					release();
				}
				break;
		}
		release();
		return true;
	}

	private void release() {
		postDelayed(new Runnable() {
			@Override
			public void run() {
				renderer.release();
			}
		}, 0);
	}

	private boolean isSwipe(MotionEvent event) {
		return (Math.abs(event.getX() - previousX) > 20 && Math.abs(event.getY() - previousY) > 20);
	}

	private boolean isClick(MotionEvent event) {
		return (Math.abs(event.getX() - startX) < 20 && Math.abs(event.getY() - startY) < 20);
	}

	@Override
	public void onResume() {
		super.onResume();
		if (items.isEmpty()) {
			throw new EmptyStackException();
		}
	}
}

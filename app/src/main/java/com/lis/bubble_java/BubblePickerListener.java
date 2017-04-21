package com.lis.bubble_java;

import com.lis.bubble_java.model.PickerItem;

/**
 * Created by lis on 2017/4/19.
 */

public interface BubblePickerListener {
	void onBubbleSelected(PickerItem item);

	void onBubbleDeselected(PickerItem item);
}

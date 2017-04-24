package com.lis.bubble_java;

import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.lis.bubble_java.model.BubbleGradient;
import com.lis.bubble_java.model.PickerItem;
import com.lis.bubble_java.rendering.BubblePicker;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
	BubblePicker picker;

	@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		picker = (BubblePicker) findViewById(R.id.picker);
		String[] titles = getResources().getStringArray(R.array.countries);
		TypedArray colors = getResources().obtainTypedArray(R.array.colors);
		TypedArray images = getResources().obtainTypedArray(R.array.images);

		ArrayList<PickerItem> items = new ArrayList<>();
		for (int i = 0; i <titles.length; i++) {
			items.add(new PickerItem(titles[i], null, true, 0, new BubbleGradient(colors.getColor(
					(i * 2) % 8, 0), colors.getColor((i * 2) % 8 + 1, 0), BubbleGradient.VERTICAL),
					                               0.5f, Typeface.DEFAULT, ContextCompat.getColor(this, android.R.color.white), 40f,
					                               ContextCompat.getDrawable(this, images.getResourceId(i, 0)), false));
		}
		picker.setItems(items);
		colors.recycle();
		images.recycle();
		picker.setBubbleSize(20);
		picker.setListener(new BubblePickerListener() {
			@Override
			public void onBubbleSelected(PickerItem item) {
				Toast.makeText(MainActivity.this,
						item.title + " selected", Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onBubbleDeselected(PickerItem item) {
				Toast.makeText(MainActivity.this,
						item.title + " deselected", Toast.LENGTH_SHORT).show();
			}
		});

	}

	@Override
	protected void onResume() {
		super.onResume();
		if (picker != null) {
			picker.onResume();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (picker != null) {
			picker.onPause();
		}
	}
}

package com.lis.bubble_java.rendering;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.view.View;

import com.lis.bubble_java.BubblePickerListener;
import com.lis.bubble_java.model.Color;
import com.lis.bubble_java.model.PickerItem;
import com.lis.bubble_java.physics.CircleBody;

import org.jbox2d.common.Vec2;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_FRAGMENT_SHADER;
import static android.opengl.GLES20.GL_VERTEX_SHADER;
import static android.opengl.GLES20.glAttachShader;
import static android.opengl.GLES20.glBlendFunc;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glCompileShader;
import static android.opengl.GLES20.glCreateProgram;
import static android.opengl.GLES20.glCreateShader;
import static android.opengl.GLES20.glEnable;
import static android.opengl.GLES20.glLinkProgram;
import static android.opengl.GLES20.glShaderSource;
import static android.opengl.GLES20.glUniform4f;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glViewport;
import static com.lis.bubble_java.rendering.BubbleShader.A_POSITION;
import static com.lis.bubble_java.rendering.BubbleShader.A_UV;
import static com.lis.bubble_java.rendering.BubbleShader.fragmentShader;
import static com.lis.bubble_java.rendering.BubbleShader.vertexShader;

/**
 * Created by lis on 2017/4/19.
 */

public class PickerRenderer implements GLSurfaceView.Renderer {
	private View glView;

	public Color backgroundColor;
	BubblePickerListener listener;
	ArrayList<PickerItem> items = new ArrayList<>();
	int bubbleSize;
	Engine engine = new Engine();
	private ArrayList<Item> circles = new ArrayList<Item>();

	public List<PickerItem> getSelectedItems() {
		for (Item item : circles) {
			if (item.circleBody == engine.getSelectedBodies()) {
				selectedItems.add(item.pickerItem);
			}
		}
		return selectedItems;
	}

	public List<PickerItem> selectedItems = new ArrayList<>();

	public void setMaxSelectedCount(int maxSelectedCount) {
		engine.maxSelectedCount = maxSelectedCount;
	}


	public PickerRenderer(BubblePicker bubblePicker) {
		glView = bubblePicker;
	}


	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		//设置屏幕背景色RGBA
		GLES20.glClearColor(
				backgroundColor.red != 0f ? backgroundColor.red : 1f,
				backgroundColor.green != 0f ? backgroundColor.green : 1f,
				backgroundColor.blue != 0f ? backgroundColor.blue : 1f,
				backgroundColor.alpha != 0f ? backgroundColor.alpha : 1f);
		enableTransparency();
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		glViewport(0, 0, width, height);
		initialize();
	}

	@Override
	public void onDrawFrame(GL10 gl) {
		calculateVertices();
		engine.move();
		drawFrame();

	}

	public FloatBuffer bufferUtil(float[] arr) {
		FloatBuffer mBuffer;
		//先初始化buffer,数组的长度*4,因为一个int占4个字节
		ByteBuffer qbb = ByteBuffer.allocateDirect(arr.length * 4);
		//数组排列用nativeOrder
		qbb.order(ByteOrder.nativeOrder());

		mBuffer = qbb.asFloatBuffer();
		mBuffer.put(arr);
		mBuffer.position(0);

		return mBuffer;
	}

	private void drawFrame() {
		glClear(GL_COLOR_BUFFER_BIT);
		glUniform4f(GLES20.glGetUniformLocation(programId, BubbleShader.U_BACKGROUND), 1f, 1f, 1f, 0f);

		GLES20.glVertexAttribPointer(
				GLES20.glGetAttribLocation(programId, A_POSITION), 2, GL_FLOAT, false,
				2 * 4, verticesBuffer.position(0));
		GLES20.glEnableVertexAttribArray(GLES20.glGetAttribLocation(programId, A_POSITION));

		GLES20.glVertexAttribPointer(GLES20.glGetAttribLocation(programId, A_UV), 2, GL_FLOAT, false,
				2 * 4, uvBuffer.position(0));
		GLES20.glEnableVertexAttribArray(GLES20.glGetAttribLocation(programId, A_UV));
		for (int i = 0; i < circles.size(); i++) {
			circles.get(i).drawItself(programId, i, getScaleX(), getScaleY());
		}
	}


	private void calculateVertices() {
		for (int i = 0; i < circles.size(); i++) {
			initializeVertices(circles.get(i), i);
		}
		for (int j = 0; j < vertices.length; j++) {
			verticesBuffer.put(j, vertices[j]);
		}
	}

	private void enableTransparency() {
		glEnable(GLES20.GL_BLEND);
		glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
		attachShaders();
	}

	private int programId = 0;

	private void attachShaders() {
		programId = createProgram(createShader(GL_VERTEX_SHADER, vertexShader),
				createShader(GL_FRAGMENT_SHADER, fragmentShader));
		glUseProgram(programId);
	}

	private int createProgram(int vertexShader, int fragmentShader) {
		int mCreateProgram = glCreateProgram();
		glAttachShader(mCreateProgram, vertexShader);
		glAttachShader(mCreateProgram, fragmentShader);
		glLinkProgram(mCreateProgram);
		return mCreateProgram;
	}

	private int createShader(int type, String shader) {
		int shaderId = glCreateShader(type);
		glShaderSource(shaderId, shader);
		glCompileShader(shaderId);
		return shaderId;
	}


	int[] textureIds = null;

	private void initialize() {
		clear();
		List<CircleBody> circleBodys = engine.build(items.size(), getScaleX(), getScaleY());
		for (int i = 0; i < circleBodys.size(); i++) {
			circles.add(new Item(items.get(i), circleBodys.get(i)));
		}
		for (PickerItem p : items) {
			if (p.isSelected) {
				for (Item item : circles) {
					if (item.pickerItem == p) {
						engine.resize(item);
						break;
					}
				}
			}
		}
		if (textureIds == null) {
			textureIds = new int[circles.size() * 2];
		}
		initializeArrays();
	}

	private float[] vertices = null;
	private float[] textureVertices = null;
	private FloatBuffer verticesBuffer = null;
	private FloatBuffer uvBuffer = null;

	private void initializeArrays() {
		vertices = new float[circles.size() * 8];
		textureVertices = new float[circles.size() * 8];
		for (int i = 0; i < circles.size(); i++) {
			initializeItem(circles.get(i), i);
		}
		verticesBuffer = bufferUtil(vertices);
		uvBuffer = bufferUtil(textureVertices);
	}

	float[] textureFloat = new float[]{0f, 0f, 0f, 1f, 1f, 0f, 1f, 1f};

	private void initializeItem(Item item, int index) {
		initializeVertices(item, index);
		for (int i = 0; i < 8; i++) {
			textureVertices[index * 8 + i] = textureFloat[i];
		}
		item.bindTextures(textureIds == null ? new int[0] : textureIds, index);
	}

	private void initializeVertices(Item body, int index) {
		//这块的radius其实是circleBody中的radius，这里一定要取最新的radius.
		float radius = body.getRadius();
		float radiusX = radius * getScaleX();
		float radiusY = radius * getScaleY();
		int size = index * 8;
		vertices[size] = body.initialPosition.x - radiusX;
		vertices[size + 1] = body.initialPosition.y + radiusY;
		vertices[size + 2] = body.initialPosition.x - radiusX;
		vertices[size + 3] = body.initialPosition.y - radiusY;
		vertices[size + 4] = body.initialPosition.x + radiusX;
		vertices[size + 5] = body.initialPosition.y + radiusY;
		vertices[size + 6] = body.initialPosition.x + radiusX;
		vertices[size + 7] = body.initialPosition.y - radiusY;

	}

	public void clear() {
		circles.clear();
		engine.clear();
	}


	public void resize(float x, float y) {
		Item item = getItem(new Vec2(x, glView.getHeight() - y));
		if (item != null) {
			if (engine.resize(item)) {
				if (listener != null) {
					if (item.circleBody.increased) {
						listener.onBubbleDeselected(item.pickerItem);
					} else {
						listener.onBubbleSelected(item.pickerItem);
					}
				}
			}
		}
	}

	private float getScaleX() {
		if (glView.getWidth() < glView.getHeight()) {
			return (float) glView.getHeight() / (float) glView.getWidth();
		} else {
			return 1;
		}
	}

	private float getScaleY() {
		if (glView.getWidth() < glView.getHeight()) {
			return 1;
		} else {
			return (float) glView.getWidth() / (float) glView.getHeight();
		}
	}

	public void swipe(float x, float y) {
		engine.swipe(convertValue(x / glView.getWidth(), getScaleX()),
				convertValue(y / glView.getHeight(), getScaleY()), getScaleY());
	}

	public float convertValue(float x, float y) {
		return (2f * x / y);
	}

	public float convertPoint(float x, float y) {
		return (2f * x - 1f) / y;
	}

	private Item getItem(Vec2 position) {
		float x = convertPoint(position.x / glView.getWidth(), getScaleX());
		float y = convertPoint(position.y / glView.getHeight(), getScaleY());
		for (Item item : circles) {
			if ((Math.sqrt(Math.pow(x - item.getX(), 2) + Math.pow(y - item.getY(), 2))) <=
			    item.radius) {
				return item;
			}
		}
		return null;
	}

	public void release() {
		engine.release();
	}

	public void setBubbleSize(int bubbleSize) {
		this.bubbleSize = bubbleSize;
		engine.setRadius(bubbleSize);
	}

}

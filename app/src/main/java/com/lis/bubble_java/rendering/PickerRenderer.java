package com.lis.bubble_java.rendering;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.view.View;

import com.lis.bubble_java.BubblePickerListener;
import com.lis.bubble_java.model.Color;
import com.lis.bubble_java.model.PickerItem;
import com.lis.bubble_java.physics.Border;
import com.lis.bubble_java.physics.CircleBody;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.World;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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

	private ArrayList<Item> circles = new ArrayList<Item>();

	public List<PickerItem> getSelectedItems() {
		for (Item item : circles) {
			if (item.circleBody == Engine.getSelectedBodies()) {
				selectedItems.add(item.pickerItem);
			}
		}
		return selectedItems;
	}

	public List<PickerItem> selectedItems = new ArrayList<>();

	public void setMaxSelectedCount(int maxSelectedCount) {
		Engine.maxSelectedCount = maxSelectedCount;
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
		Engine.move();
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
		glAttachShader(glCreateProgram(), vertexShader);
		glAttachShader(glCreateProgram(), fragmentShader);
		glLinkProgram(glCreateProgram());
		return vertexShader;
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
		List<CircleBody> circleBodys = Engine.build(items.size(), getScaleX(), getScaleY());
		for (int i = 0; i < circleBodys.size(); i++) {
			circles.add(new Item(items.get(i), circleBodys.get(i)));
		}
		for (PickerItem p : items) {
			if (p.isSelected) {
				for (Item item : circles) {
					if (item.pickerItem == p) {
						Engine.resize(item);
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
		float radius = body.radius;
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
		Engine.clear();
	}


	public void resize(float x, float y) {
		Item item = getItem(new Vec2(x, glView.getHeight() - y));
		if (item != null) {
			if (Engine.resize(item)) {
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

		Engine.swipe(convertValue(x / glView.getWidth(), getScaleX()),
				convertValue(y / glView.getHeight(), getScaleY()), getScaleY());
	}

	public float convertValue(float x, float y) {
		return (2f * x / y);
	}

	private Item getItem(Vec2 position) {
		// TODO: 2017/4/19 不知道是不是正确
		float x = convertValue(position.x / glView.getWidth() - 1f, getScaleX());
		//float x = position.x.convertPoint(glView.getWidth(), getScaleX());
		//float y = position.y.convertPoint(glView.getHeight(), getScaleY());
		float y = convertValue(position.y / glView.getHeight() - 1f, getScaleY());
		for (Item item : circles) {
			if ((Math.sqrt(Math.pow(x - item.x, 2)) + Math.pow(y - item.y, 2)) <= item.radius) {
				return item;
			}
		}
		return null;
		//circles.find { Math.sqrt(((x - position.x).sqr() + (y - position.y).sqr()).toDouble()) <= position.radius }

	}

	public void release() {
		Engine.release();
	}

	public void setBubbleSize(int bubbleSize) {
		this.bubbleSize = bubbleSize;
		Engine.setRadius(bubbleSize);
	}


	public static class Engine {
		public static int maxSelectedCount;
		private static ArrayList<CircleBody> bodies = new ArrayList();
		private static ArrayList<Item> toBeResized = new ArrayList<Item>();
		private static Vec2 gravityCenter = new Vec2(0f, 0f);
		private static boolean touch = false;
		private static float increasedGravity = 55f;
		private static float standardIncreasedGravity = interpolate(500f, 800f, 0.5f);
		private static ArrayList<Border> borders = new ArrayList();
		private static World world = new World(new Vec2(0f, 0f), false);
		static int radius = 50;

		private static float interpolate(float start, float end, float f) {
			return start + f * (end - start);
		}

		public static List<CircleBody> getSelectedBodies() {
			for (CircleBody cBody : bodies) {
				if (cBody.increased || cBody.toBeIncreased || cBody.isIncreasing) {
					selectedBodies.add(cBody);
				}
			}
			return selectedBodies;
		}

		static boolean resize(Item item) {
			if (selectedBodies.size() >= maxSelectedCount && !item.circleBody.increased) {
				return false;
			}

			if (item.circleBody.isBusy()) {
				return false;
			}

			item.circleBody.defineState();

			toBeResized.add(item);
			return true;
		}

		//List<PickerItem> selectedBodies;
		static List<CircleBody> selectedBodies = new ArrayList<>();

		public static boolean resize(PickerRenderer pickerRenderer) {
			return false;
		}

		private static void release() {
			gravityCenter.setZero();
			touch = false;
			increasedGravity = standardIncreasedGravity;
		}

		public static void swipe(float x, float y, float scaleY) {
			if (Math.abs(gravityCenter.x) < 2) {
				gravityCenter.x += -x;
			}
			if (Math.abs(gravityCenter.y) < 0.5f / scaleY) {gravityCenter.y += y;}
			increasedGravity = standardIncreasedGravity * Math.abs(x * 13) * Math.abs(y * 13);
			touch = true;

		}

		public static void clear() {
			for (Border border : borders) {
				world.destroyBody(border.itemBody);
			}
			for (CircleBody circle : bodies) {
				world.destroyBody(circle.physicalBody);
			}
			borders.clear();
			bodies.clear();
		}

		private static float scaleX = 0f;
		private static float scaleY = 0f;

		public static List<CircleBody> build(int size, float getScaleX, float getScaleY) {
			float density = interpolate(0.8f, 0.2f, radius / 100f);
			for (int i = 0; i < size; i++) {
				float x = new Random().nextInt(2) % 2 == 0 ? -2.2f : 2.2f;
				float y =
						new Random().nextInt(2) % 2 == 0 ? (-0.5f / getScaleY) : (0.5f / getScaleY);
				bodies.add(new CircleBody(world, new Vec2(x, y),
						                         bubbleRadius * getScaleX,
						                         (bubbleRadius * getScaleX) * 1.3f, density));
			}
			getSelectedBodies();
			scaleX = getScaleX;
			scaleY = getScaleY;
			createBorders();

			return bodies;

		}

		private static void createBorders() {
			borders.add(new Border(world, new Vec2(0f, 0.5f / scaleY), Border.HORIZONTAL));
			borders.add(new Border(world, new Vec2(0f, -0.5f / scaleY), Border.HORIZONTAL));
		}

		private static float bubbleRadius = 0.17f;
		private static float gravity = 6f;

		public static void setRadius(int radius) {
			Engine.radius = radius;
			bubbleRadius = interpolate(0.1f, 0.25f, radius / 100f);
			gravity = interpolate(20f, 80f, radius / 100f);
			standardIncreasedGravity = interpolate(500f, 800f, radius / 100f);
		}

		private static float resizeStep = 0.005f;
		private static float step = 0.0005f;

		public static void move() {
			for (Item item : toBeResized) {
				item.circleBody.resize(resizeStep);
			}

			world.step(step, 11, 11);
			for (CircleBody circle : bodies) {
				move(circle);
			}
			for (Item item : toBeResized) {
				if (item.circleBody.getFinished()) {
					toBeResized.remove(item);
				}
			}
		}

		static float getCurrentGravity() {
			return touch ? increasedGravity : gravity;
		}

		public static void move(CircleBody body) {
			Vec2 direction = gravityCenter.sub(body.physicalBody.getPosition());
			float distance = direction.length();
			float gravity = body.increased ? 1.3f * getCurrentGravity() : getCurrentGravity();
			if (distance > step * 200) {
				body.physicalBody.applyForce(direction.mul(
						gravity / (distance * distance)), body.physicalBody.getPosition());
			}
		}

	}
}

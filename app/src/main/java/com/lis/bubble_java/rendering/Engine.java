package com.lis.bubble_java.rendering;

import com.lis.bubble_java.physics.Border;
import com.lis.bubble_java.physics.CircleBody;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by lis on 2017/4/24.
 */

public class Engine {
	public int maxSelectedCount = -1;
	private ArrayList<CircleBody> bodies = new ArrayList();
	private ArrayList<Item> toBeResized = new ArrayList<Item>();
	private Vec2 gravityCenter = new Vec2(0f, 0f);
	private boolean touch = false;
	private float increasedGravity = 55f;
	private float standardIncreasedGravity = interpolate(500f, 800f, 0.5f);
	private ArrayList<Border> borders = new ArrayList();
	private World world = new World(new Vec2(0f, 0f), false);
	int radius = 50;

	private float interpolate(float start, float end, float f) {
		return start + f * (end - start);
	}

	public List<CircleBody> getSelectedBodies() {
		for (CircleBody cBody : bodies) {
			if (cBody.increased || cBody.toBeIncreased || cBody.isIncreasing) {
				selectedBodies.add(cBody);
			}
		}
		return selectedBodies;
	}

	boolean resize(Item item) {
		if (selectedBodies.size() >=
		    (maxSelectedCount == -1 ? bodies.size() : maxSelectedCount) &&
		    !item.circleBody.increased) {
			return false;
		}

		if (item.circleBody.isBusy()) {
			return false;
		}

		item.circleBody.defineState();

		toBeResized.add(item);
		return true;
	}

	List<CircleBody> selectedBodies = new ArrayList<>();

	public boolean resize(PickerRenderer pickerRenderer) {
		return false;
	}

	public void release() {
		gravityCenter.setZero();
		touch = false;
		increasedGravity = standardIncreasedGravity;
	}

	public void swipe(float x, float y, float scaleY) {
		if (Math.abs(gravityCenter.x) < 2) {
			gravityCenter.x += -x;
		}
		if (Math.abs(gravityCenter.y) < 0.5f / scaleY) {gravityCenter.y += y;}
		increasedGravity = standardIncreasedGravity * Math.abs(x * 13) * Math.abs(y * 13);
		touch = true;

	}

	public void clear() {
		for (Border border : borders) {
			world.destroyBody(border.itemBody);
		}
		for (CircleBody circle : bodies) {
			world.destroyBody(circle.physicalBody);
		}
		borders.clear();
		bodies.clear();
	}

	private float scaleX = 0f;
	private float scaleY = 0f;

	public List<CircleBody> build(int size, float getScaleX, float getScaleY) {
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

	private void createBorders() {
		borders.add(new Border(world, new Vec2(0f, 0.5f / scaleY), Border.HORIZONTAL));
		borders.add(new Border(world, new Vec2(0f, -0.5f / scaleY), Border.HORIZONTAL));
	}

	private float bubbleRadius = 0.17f;
	private float gravity = 6f;

	public void setRadius(int radius) {
		this.radius = radius;
		bubbleRadius = interpolate(0.1f, 0.25f, radius / 100f);
		gravity = interpolate(20f, 80f, radius / 100f);
		standardIncreasedGravity = interpolate(500f, 800f, radius / 100f);
	}

	private float resizeStep = 0.005f;
	private float step = 0.0005f;

	public void move() {
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

	float getCurrentGravity() {
		return touch ? increasedGravity : gravity;
	}

	public void move(CircleBody body) {
		Vec2 direction = gravityCenter.sub(body.physicalBody.getPosition());
		float distance = direction.length();
		float gravity = body.increased ? 1.3f * getCurrentGravity() : getCurrentGravity();
		if (distance > step * 200) {
			body.physicalBody.applyForce(direction.mul(
					gravity / (distance * distance)), body.physicalBody.getPosition());
		}
	}

}

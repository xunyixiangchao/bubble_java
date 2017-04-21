package com.lis.bubble_java.physics;

import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.World;

/**
 * Created by lis on 2017/4/19.
 */

public class CircleBody {
	World world;
	public Vec2 position;
	public Float radius;
	Float increasedRadius;
	Float density;

	public CircleBody(World world, Vec2 position, Float radius, Float increasedRadius, Float density) {
		this.world = world;
		this.density = density;
		this.position = position;
		this.radius = radius;
		this.increasedRadius = increasedRadius;
		init();
	}

	private void init() {
		while (true) {
			if (!world.isLocked()) {
				initializeBody();
				break;
			}
		}
	}

	public BodyDef getBodyDef() {
		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyType.DYNAMIC;
		bodyDef.position = position;
		return bodyDef;
	}

	private Float margin = 0.01f;
	private Float damping = 25f;

	public CircleShape getCircleShape() {
		CircleShape shape = new CircleShape();
		shape.m_radius = radius + margin;
		shape.m_p.setZero();
		return shape;
	}

	public FixtureDef getFixture() {
		FixtureDef fixture = new FixtureDef();
		fixture.shape = getCircleShape();
		fixture.density = density;
		return fixture;
	}

	public Body getPhysicalBody() {
		return physicalBody;
	}

	public Body physicalBody;

	private void initializeBody() {
		physicalBody = world.createBody(getBodyDef());
		physicalBody.createFixture(getFixture());
		physicalBody.setLinearDamping(damping);
	}

	public void resize(Float step) {
		if (increased) {
			decrease(step);
		} else {
			increase(step);
		}
	}

	private void increase(Float step) {
		isIncreasing = true;
		radius += step;
		reset();
		if (Math.abs(radius - increasedRadius) < step) {
			increased = true;
			clear();
		}
	}

	Float decreasedRadius = radius;

	private void decrease(Float step) {
		isDecreasing = true;
		radius -= step;
		reset();
		if (Math.abs(radius - decreasedRadius) < step) {
			increased = false;
			clear();
		}
	}

	private void clear() {
		toBeIncreased = false;
		toBeDecreased = false;
		isIncreasing = false;
		isDecreasing = false;
	}

	public boolean getFinished() {
		return !toBeIncreased && !toBeDecreased && !isIncreasing && !isDecreasing;
	}

	public void reset() {
		physicalBody.getFixtureList().getShape().m_radius = radius + margin;
	}

	public boolean increased = false;
	public boolean toBeIncreased = false;
	public boolean isIncreasing = false;
	boolean isDecreasing = false;
	boolean toBeDecreased = false;

	public boolean isBusy() {
		return isIncreasing || isDecreasing;
	}

	public void defineState() {
		toBeIncreased = !increased;
		toBeDecreased = increased;
	}
}

package com.lis.bubble_java.physics;

import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.World;

/**
 * Created by lis on 2017/4/20.
 */

public class Border {
	public static final int HORIZONTAL = 0;
	public static final int VERTICAL = 1;
	World world;
	Vec2 position;
	int view;
	public Body itemBody;

	public Border(World world, Vec2 position, int view) {
		this.world = world;
		this.position = position;
		this.view = view;
		init();
	}

	private BodyDef getBodyDef() {
		BodyDef  bodyDef = new BodyDef();
		bodyDef.type = BodyType.STATIC;
		bodyDef.position = position;
		return bodyDef;
	}

	private void init() {
		itemBody = world.createBody(getBodyDef());
		itemBody.createFixture(getFixture());

	}

	public FixtureDef getFixture() {
		FixtureDef fixture = new FixtureDef();
		fixture.shape = getShape();
		fixture.density = 50f;
		return fixture;
	}

	public Shape getShape() {
		PolygonShape shape = new PolygonShape();
		if(view==HORIZONTAL){
			shape.setAsEdge(new Vec2(-100f,position.y),new Vec2(100f,position.y));
		}else {
			shape.setAsEdge(new Vec2(position.x, -100f),new Vec2(position.x, 100f));
		}
		return shape;
	}
}

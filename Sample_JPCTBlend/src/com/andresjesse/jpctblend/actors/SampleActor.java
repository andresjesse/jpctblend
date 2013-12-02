package com.andresjesse.jpctblend.actors;

import com.andresjesse.jpctblend.IActor;
import com.threed.jpct.Loader;
import com.threed.jpct.Object3D;
import com.threed.jpct.SimpleVector;
import com.threed.jpct.World;

/**
 * Sampĺe Actor, this class shows how to create a custom Actor. Note that this
 * is the sample "Green Human" used in Blender. You can define an Actor to be
 * whatever you want (3D Objects, Billboards, Shader Based Object, etc). An
 * instance of your class will be positioned exactly in the same point as
 * defined in Blender. You can also define an "Act" behavior, that will be
 * executed at each frame of your gameplay.
 * 
 * @author andres
 * 
 */
public class SampleActor implements IActor {

	// create Actor fields here
	private Object3D obj;
	private World world;

	/**
	 * Constructor: Here you can define your Actor mesh, texture, props, etc.
	 */
	public SampleActor() {
		obj = Loader.load3DS("media/actors/SampleActor.3ds", 1.0f)[0];
	}

	/**
	 * Create here your logic to add your actor to the world.
	 * 
	 * @param world
	 *            World to add the actor
	 */
	@Override
	public void addToWorld(World world) {
		this.world = world;

		world.addObject(obj);
	}

	/**
	 * Create here your logic to remove actor from world.
	 */
	@Override
	public void removeFromWorld() {
		world.removeObject(obj);
	}

	/**
	 * Set position to your actor (make sure to configure all objects if you
	 * have more than one). This method is called by JPCTBlend when loading
	 * scene
	 * 
	 * @param pos
	 *            Actor position
	 */
	@Override
	public void setPosition(SimpleVector pos) {
		obj.clearTranslation();
		obj.translate(pos);
	}

	/**
	 * Set rotation to your actor (make sure to configure all objects if you
	 * have more than one). This method is called by JPCTBlend when loading
	 * scene
	 * 
	 * @param pos
	 *            Actor position
	 */
	@Override
	public void setRotation(SimpleVector rot) {
		obj.clearRotation();
		obj.rotateX(rot.x);
		obj.rotateY(rot.y);
		obj.rotateZ(rot.z);
	}

	/**
	 * Create here your behavior, act is called at each frame, so you don't need
	 * to create a loop here. More info in the JPCTBlend Manual.
	 */
	@Override
	public void act() {
		obj.rotateY(0.01f);
	}
}

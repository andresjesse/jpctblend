package com.andresjesse.jpctblendae;

import java.lang.reflect.InvocationTargetException;

import android.content.res.AssetManager;

/**
 * Class designed to provide default builders for Actors
 * 
 * @author andres
 * 
 */
public class ActorFactory {

	private static final String actorsPackage = "com.andresjesse.jpctblendae.actors";

	private static ActorFactory instance;

	private AssetManager assets;

	/**
	 * Private constructor, to avoid the use of "new" operator
	 */
	private ActorFactory() {
	}

	public static ActorFactory getInstance() {
		if (instance == null)
			instance = new ActorFactory();
		return instance;
	}

	/**
	 * Create an Actor based on his java class name.
	 * 
	 * @param actorClass
	 *            java class that implements IActor
	 * @return the actor
	 */
	public IActor createFromString(String actorClass) {

		try {
			Object unknownActor = null;
			//try to invoke constructor using "assets" param
			try {
				unknownActor = Class.forName(actorsPackage + "." + actorClass)
						.getDeclaredConstructor(AssetManager.class)
						.newInstance(assets);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			}

			if (unknownActor instanceof IActor) {
				System.out.println("JPCTBlend: create actor: " + actorsPackage
						+ "." + actorClass);
				return (IActor) unknownActor;
			} else {
				throw new RuntimeException(
						"JPCTBlend error: Can't create actor, class "
								+ actorsPackage + "." + actorClass
								+ " must implement IActor interface!");
			}
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(
					"JPCTBlend error: Can't create actor, class ''"
							+ actorsPackage + "." + actorClass
							+ "'' not found!\n Did you forgot to create it?");
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}

		return null;
	}

	public void setAssetsManager(AssetManager assets) {
		this.assets = assets;
	}
}

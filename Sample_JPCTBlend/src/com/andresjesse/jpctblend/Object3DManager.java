package com.andresjesse.jpctblend;

import java.util.HashMap;

import com.threed.jpct.Object3D;

/**
 * Object3DManager class. This is a loader helper to improve load performance.
 * It avoids to load more than one time the same 3D Object from disk by
 * performing clone. Similar to JPCT TextureManager.
 * 
 * @author andres
 * 
 */
public class Object3DManager {

	private HashMap<String, Object3D> loadedObjects;

	private static Object3DManager instance = null;

	public static Object3DManager getInstance() {
		if (instance == null)
			instance = new Object3DManager();
		return instance;
	}

	private Object3DManager() {
		loadedObjects = new HashMap<>();
	}

	public boolean containsObject3D(String key) {
		return loadedObjects.containsKey(key);
	}

	public Object3D getObject3D(String key) {
		if (!loadedObjects.containsKey(key))
			throw new RuntimeException(
					"Error: Object3DManager does not contains " + key);
		return loadedObjects.get(key);
	}

	public void putObject3D(String key, Object3D value) {
		if (!loadedObjects.containsKey(key))
			loadedObjects.put(key, value);
	}

	public int size() {
		return loadedObjects.size();
	}
}

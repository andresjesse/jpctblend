package com.andresjesse.jpctblend;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.threed.jpct.Loader;
import com.threed.jpct.Object3D;
import com.threed.jpct.SimpleVector;
import com.threed.jpct.Texture;
import com.threed.jpct.TextureManager;
import com.threed.jpct.World;
import com.threed.jpct.util.Light;

/**
 * JPCTBlendScene Class. This class is the JPCTBlend Core, it loads and manages
 * an exported scene.
 * 
 * @author andres
 * 
 */
public class JPCTBlendScene {
	private static final int IMPORTER_VERSION = 2;

	private boolean active = false;
	
	//rotation pivots are reseted in build() method,
	//they are corrected in first "JPCTBlend.update()"
	//need to find a better solution for this..
	private boolean pivotsFixed = false;

	// Scene path (contains scene xml's, "textures" and "meshs" subfolders)
	private String sceneBasePath;

	// Data loaded from XML
	private ExporterInfo exporterInfo;
	private ArrayList<String> textures;
	private ArrayList<Object3D> instances;
	private ArrayList<Light> lights;
	private ArrayList<CameraInfo> cameras;
	private ArrayList<IActor> actors;
	private SimpleVector ambientLight;

	// Scene World
	private World world;

	/**
	 * JPCTScene constructor, loads an scene based on the main xml file
	 * (exported by blender)
	 * 
	 * @param sceneFilename
	 *            scene xml filename
	 */
	public JPCTBlendScene(String sceneFilename, World world) {

		this.world = world;

		File file = new File(sceneFilename);
		sceneBasePath = file.getParentFile().getAbsolutePath() + File.separator;

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder builder;
		Document doc = null;

		try {
			builder = factory.newDocumentBuilder();
			doc = builder.parse(sceneFilename);

			parseRoot(doc.getFirstChild());

		} catch (ParserConfigurationException | SAXException | IOException e) {
			e.printStackTrace();
		}

		addSceneToWorld();
	}

	/**
	 * Adds everything to the world.
	 */
	public void addSceneToWorld() {
		if (active)
			throw new RuntimeException(
					"Cannot load Scene! it  already has been loaded!");

		for (Object3D instance : instances) {
			world.addObject(instance);
		}

		for (IActor actor : actors) {
			actor.addToWorld(world);
		}

		for (Light light : lights) {
			light.enable();
		}

		if (cameras.size() > 0) {
			CameraInfo currentCameraInfo = cameras.get(0);// For now '0' is the
															// default camera.

			world.getCamera().setPosition(currentCameraInfo.getPosition());
			world.getCamera().lookAt(currentCameraInfo.getLookAt());

			// FOV tip, by juan from JPCT forum.
			// http://www.jpct.net/forum2/index.php/topic,3711.0.html
			world.getCamera().setFOV(0.914f);
		}

		active = true;
	}
	
	/**
	 * According to JPCT forum, the build() method resets pivots,
	 * i don't know why this causes error in some objects, so you
	 * need to fix pivots after use a build() or world.buildAllObjects().
	 */
	private void fixPivots() {
		for(Object3D obj : instances)
			obj.setRotationPivot(new SimpleVector());
	}

	/**
	 * Removes everything to the world. Reset camera pos/lookAt.
	 */
	public void removeSceneFromWorld() {
		if (!active)
			throw new RuntimeException(
					"Cannot remove scene! It has already been removed!");

		for (Object3D instance : instances) {
			world.removeObject(instance);
		}

		for (IActor actor : actors) {
			actor.removeFromWorld();
		}

		for (Light light : lights) {
			light.dispose();
		}

		world.getCamera().setPosition(0, 0, 0);
		world.getCamera().lookAt(new SimpleVector(0, 0, 1));

		active = false;
	}

	// =================================================================Parsers

	/**
	 * Parse all actors and return as list. All actors must be defined as Java
	 * classes.
	 * 
	 * @param xmlInfo
	 *            actors xml root
	 * @return all actors as list
	 */
	private ArrayList<IActor> parseActors(Node xmlActors) {
		ArrayList<IActor> actorsList = new ArrayList<IActor>();

		NodeList childs = xmlActors.getChildNodes();

		for (int i = 0, len = childs.getLength(); i < len; i++) {
			Node node = childs.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE
					&& node.getNodeName().equals("actor")) {

				IActor actor = ActorFactory.getInstance().createFromString(
						getAttrValue("javaclass", node));

				if (actor != null) {
					actor.setPosition(getAttrValueSimpleVector("position", node));
					actor.setRotation(getAttrValueSimpleVector("rotation", node));

					actorsList.add(actor);
				}
			}
		}

		return actorsList;
	}

	/**
	 * Parse all cameras and return as list. For new blender exports just active
	 * camera, in future the plan is to add support for several cameras, so will
	 * be possible to add all of them in blender and "switch" or create paths
	 * (for cutscenes) here in jpct.
	 * 
	 * @param xmlInfo
	 *            cameras xml root
	 * @return all cameras info as list
	 */
	private ArrayList<CameraInfo> parseCamera(Node xmlCameras) {
		ArrayList<CameraInfo> cameras = new ArrayList<CameraInfo>();

		CameraInfo cinfo = new CameraInfo(getAttrValueSimpleVector("lookat",
				xmlCameras), getAttrValueSimpleVector("position", xmlCameras));

		cameras.add(cinfo);

		return cameras;
	}

	/**
	 * Parses basic information about the exporter
	 * 
	 * @param xmlInfo
	 *            xml root
	 * @return ExporterInfo with basic exporter data
	 */
	private static ExporterInfo parseExporterInfo(Node xmlInfo) {
		return new ExporterInfo(getAttrValue("author", xmlInfo), getAttrValue(
				"contact", xmlInfo), getAttrValueDate("date", xmlInfo),
				getAttrValue("script", xmlInfo), getAttrValueInteger("version",
						xmlInfo));
	}

	/**
	 * Loads all meshs and add them to the MeshsManager. Also returns a a list
	 * of configured (pos/rot/scale/texture) Object3D's.
	 * 
	 * @param xmlInstances
	 *            instances root xml node
	 * @return all loaded instances as a strings list
	 */
	private ArrayList<Object3D> parseInstances(Node xmlInstances) {
		ArrayList<Object3D> instancesList = new ArrayList<Object3D>();

		NodeList childs = xmlInstances.getChildNodes();

		for (int i = 0, len = childs.getLength(); i < len; i++) {
			Node node = childs.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE
					&& node.getNodeName().equals("instance")) {

				String meshFile = getAttrValue("mesh_name", node) + ".3ds";

				Object3D obj = null;
				
				// Configured blender to export one object per file, so
				// loads just index [0]
				obj = Loader.load3DS(sceneBasePath + "meshs" + File.separator
						+ meshFile, 1.0f)[0];
				
				Object3DManager.getInstance().putObject3D(meshFile, obj);

				obj.translate(getAttrValueSimpleVector("position", node));

				SimpleVector rot = getAttrValueSimpleVector("rotation", node);

				obj.rotateX(rot.x);
				obj.rotateY(rot.y);
				obj.rotateZ(rot.z);

				// TODO: create scale method
				// JPCT does not support 3 axis scale? :(
				// disabled for now..
				// obj.scale(getAttrValueSimpleVector("scale", node).x);

				String textureName = getAttrValue("texture", node);
				obj.setTexture(textureName);

				//alpha to "png" textures
				if (textureName.length() > 3) {

					String ext = getAttrValue("texture", node).substring(
							textureName.length() - 3, textureName.length());

					if (ext.equals("png"))
						obj.setTransparency(10);
				}

				instancesList.add(obj);
			}
		}

		return instancesList;
	}

	/**
	 * Loads and add all lights to world
	 * 
	 * @param xmlLights
	 *            xml lights root
	 * @return
	 */
	private ArrayList<Light> parseLights(Node xmlLights) {
		ArrayList<Light> listLights = new ArrayList<Light>();

		NodeList childs = xmlLights.getChildNodes();

		for (int i = 0, len = childs.getLength(); i < len; i++) {
			Node node = childs.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE
					&& node.getNodeName().equals("pointlight")) {

				Light light = new Light(world);

				light.setPosition(getAttrValueSimpleVector("position", node));

				SimpleVector intensity = getAttrValueSimpleVector("rgbcolor",
						node);
				intensity.scalarMul(getAttrValueFloat("distance", node) * 2.5f);// experimental
																				// param

				light.setIntensity(intensity);
				light.setAttenuation(getAttrValueFloat("distance", node) * 0.2f);// experimental
																					// param
				light.setDiscardDistance(getAttrValueFloat("distance", node) * 1.5f);// experimental
																						// param

				listLights.add(light);
			}

			if (node.getNodeType() == Node.ELEMENT_NODE
					&& node.getNodeName().equals("ambient")) {

				ambientLight = getAttrValueSimpleVector("rgbcolor", node);

				world.setAmbientLight((int) (ambientLight.x * 255),
						(int) (ambientLight.y * 255),
						(int) (ambientLight.z * 255));
			}
		}

		return listLights;
	}

	/**
	 * Loads all textures and add them to the JPCT TextureManager. Also returns
	 * a a list of textures (can be used to separe textures loaded by the user
	 * and textures loaded by the JPCTBlend importer.
	 * 
	 * @param xmlTextures
	 *            textures root xml node
	 * @return all loaded textures as a strings list
	 */
	private ArrayList<String> parseTextures(Node xmlTextures) {
		ArrayList<String> listTextures = new ArrayList<String>();

		NodeList childs = xmlTextures.getChildNodes();

		for (int i = 0, len = childs.getLength(); i < len; i++) {
			Node node = childs.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {

				String txName = node.getNodeName();
				Texture newTx = new Texture(sceneBasePath + "textures"
						+ File.separator + txName);

				TextureManager.getInstance().addTexture(txName, newTx);
				listTextures.add(txName);
			}
		}

		return listTextures;
	}

	/**
	 * Given a xml node (root), parses all (importing meshs, textures,
	 * instances, etc)
	 * 
	 * @param xmlRoot
	 *            document's root
	 */
	private void parseRoot(Node xmlRoot) {
		exporterInfo = parseExporterInfo(findSubNode("exporter_info", xmlRoot));

		if (exporterInfo.getVersion() != IMPORTER_VERSION)
			throw new RuntimeException(
					"JPCTBlend: unsupported jpctblend file!\n		Your file was exported from Blender using exporter version '"
							+ exporterInfo.getVersion()
							+ "', this importer can only load files of version '"
							+ IMPORTER_VERSION + "'.");

		textures = parseTextures(findSubNode("textures", xmlRoot));
		lights = parseLights(findSubNode("lights", xmlRoot));
		instances = parseInstances(findSubNode("instances", xmlRoot));
		cameras = parseCamera(findSubNode("camera", xmlRoot));
		actors = parseActors(findSubNode("actors", xmlRoot));

		System.out.println(exporterInfo);
		System.out.println("JPCTBlend: Loaded " + textures.size()
				+ " textures.");
		System.out.println("JPCTBlend: Loaded " + instances.size()
				+ " instances using " + Object3DManager.getInstance().size()
				+ " meshs.");
		System.out.println("JPCTBlend: Loaded " + actors.size() + " actors.");
	}

	/**
	 * Update all JPCTBlend objects and fixes rotationPivot after build(); 
	 * Actors are updated in the scene by calling the "act" method for each one.
	 */
	public void update() {
		if(!pivotsFixed)
			fixPivots();
			
		for (IActor actor : actors) {
			actor.act();
		}
	}

	// =================================================================XML
	// Helpers

	/**
	 * 
	 * @param name
	 *            Attribute name
	 * @param node
	 *            Container node
	 * @return String with the value, null otherwise
	 */
	public static String getAttrValue(String name, Node node) {
		Node attr = findAttribute(name, node);
		if (attr != null)
			return attr.getNodeValue();
		else
			return null;
	}

	public static Date getAttrValueDate(String name, Node node) {
		Node attr = findAttribute(name, node);
		try {
			if (attr != null)
				return new SimpleDateFormat("yyyy/MM/dd").parse(attr
						.getNodeValue());
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 
	 * @param name
	 *            Attribute name
	 * @param node
	 *            Container node
	 * @return Float with the value, null otherwise
	 */
	public static Float getAttrValueFloat(String name, Node node) {
		Node attr = findAttribute(name, node);
		if (attr != null)
			return Float.parseFloat(attr.getNodeValue());
		else
			return null;
	}

	/**
	 * 
	 * @param name
	 *            Attribute name
	 * @param node
	 *            Container node
	 * @return Integer with the value, null otherwise
	 */
	public static Integer getAttrValueInteger(String name, Node node) {
		Node attr = findAttribute(name, node);
		if (attr != null)
			return Integer.parseInt(attr.getNodeValue());
		else
			return null;
	}

	/**
	 * 
	 * @param name
	 *            Attribute name
	 * @param node
	 *            Container node
	 * @return SimpleVector with the value, null otherwise
	 */
	public static SimpleVector getAttrValueSimpleVector(String name, Node node) {
		Node attr = findAttribute(name, node);
		if (attr != null) {
			String tk[] = attr.getNodeValue().split(",");
			SimpleVector vec = new SimpleVector(Float.parseFloat(tk[0]),
					Float.parseFloat(tk[1]), Float.parseFloat(tk[2]));
			return vec;
		} else
			return null;
	}

	/**
	 * Search for a particular attribute in a node.
	 * 
	 * @param name
	 *            Attribute name
	 * @param node
	 *            Container node
	 * @return the Attribute found
	 */
	public static Node findAttribute(String name, Node node) {
		if (node.getNodeType() != Node.ELEMENT_NODE) {
			System.err.println("Error: Search node not of element type");
			System.exit(22);
		}

		NamedNodeMap attMap = node.getAttributes();

		for (int i = 0; i < attMap.getLength(); i++) {
			Node attr = attMap.item(i);
			if (attr.getNodeType() == Node.ATTRIBUTE_NODE) {
				if (attr.getNodeName().equals(name))
					return attr;
			}
		}
		return null;
	}

	/**
	 * Method from: http://docs.oracle.com/javaee/1.4/tutorial/doc/JAXPDOM7.html
	 * Find the named subnode in a node's sublist. <li>Ignores comments and
	 * processing instructions. <li>Ignores TEXT nodes (likely to exist and
	 * contain ignorable whitespace, if not validating. <li>Ignores CDATA nodes
	 * and EntityRef nodes. <li>Examines element nodes to find one with the
	 * specified name. </ul>
	 * 
	 * @param name
	 *            the tag name for the element to find
	 * @param node
	 *            the element node to start searching from
	 * @return the Node found
	 */
	public static Node findSubNode(String name, Node node) {
		if (node.getNodeType() != Node.ELEMENT_NODE) {
			System.err.println("Error: Search node not of element type");
			System.exit(22);
		}

		if (!node.hasChildNodes())
			return null;

		NodeList list = node.getChildNodes();
		for (int i = 0; i < list.getLength(); i++) {
			Node subnode = list.item(i);
			if (subnode.getNodeType() == Node.ELEMENT_NODE) {
				if (subnode.getNodeName().equals(name))
					return subnode;
			}
		}
		return null;
	}
}

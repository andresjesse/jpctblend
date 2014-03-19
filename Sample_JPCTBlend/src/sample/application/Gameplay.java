package sample.application;

import com.andresjesse.jpctblend.JPCTBlendScene;
import com.threed.jpct.FrameBuffer;
import com.threed.jpct.IRenderer;
import com.threed.jpct.World;

/**
 * Gameplay class, this is a small sample of how to startup the JPCT Engine and
 * load/dispose a JPCTBlend Scene.
 * 
 * @author andres
 * 
 */
public class Gameplay {
	private World world;

	private FrameBuffer fb;

	private JPCTBlendScene scn;

	/**
	 * Constructor, just splits the engine lifetime in a few methods.
	 */
	public Gameplay() {
		setupEngine();
		setupObjects();
		run();
		clean();
	}

	/**
	 * Setup the engine. Create the world.
	 */
	private void setupEngine() {
		fb = new FrameBuffer(1024, 768, FrameBuffer.SAMPLINGMODE_NORMAL);
		fb.disableRenderer(IRenderer.RENDERER_SOFTWARE);
		fb.enableRenderer(IRenderer.RENDERER_OPENGL);

		world = new World();
	}

	/**
	 * Method used to setup the objects, in this case just loads the scene
	 * exported from blender.
	 */
	private void setupObjects() {
		scn = new JPCTBlendScene("media/scenes/sample_scene/sample_scene.xml", world);
		
		world.buildAllObjects();
	}

	/**
	 * JPCT LWJGL main loop.
	 */
	private void run() {
		//fps control
		long startTime = 0, elapsedTime = 0;

		while (!org.lwjgl.opengl.Display.isCloseRequested()) {

			startTime = System.currentTimeMillis();//fps control

			//Update JPCTBlend Scene
			scn.update();

			fb.clear(java.awt.Color.BLACK);

			world.renderScene(fb);
			world.draw(fb);

			fb.update();
			fb.displayGLOnly();
			
			//fps control
			elapsedTime = System.currentTimeMillis() - startTime;

			if (elapsedTime < 17) {
				try {
					Thread.sleep(17 - elapsedTime);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			//end of fps control
		}
	}

	/**
	 * Dispose fb and scene.
	 */
	private void clean() {
		scn.removeSceneFromWorld();

		fb.disableRenderer(IRenderer.RENDERER_OPENGL);
		fb.dispose();
	}
}

The glfacade.jar is a small extension to jPCT that enables it to use JOGL instead of LWJGL. To do so, follow these steps:

- remove all LWJGL related jars from the classpath
- add all JOGL related jars to the classpath
- add the glfacade.jar to the classpath
- call enableGLCanvasRenderer() from FrameBuffer

You can't switch renderers on the fly with this and you can't use OpenGL in a native GL window like LWJGL can.
But you can use JOGL if you have/want to...

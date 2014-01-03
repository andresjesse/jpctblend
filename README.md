JPCTBlend
=========

![ScreenShot](/web_images/jpctblend.png)


Looking for Help? Take a Look in the [Wiki](https://github.com/andresjesse/jpctblend/wiki) and [Javadoc](http://htmlpreview.github.io/?https://github.com/andresjesse/jpctblend/blob/master/Sample_JPCTBlend/doc/index.html)


JPCTBlend: A Simple JPCT (and AE) Blender Integration Layer. This is a simple project that aims to provide an easy way to use blender as a SandBox to create 3D worlds for JPCT Engine. E.g:

given a blender scene:
![ScreenShot](/web_images/screen_blender.jpg)


load it in a jpct world:
```java
scn = new JPCTBlendScene("media/scene_test.xml", world);

```

result:
![ScreenShot](/web_images/screen_jpct.jpg)

Main Goals:

- [x] Create initial working python script exporter for Blender;
- [x] Create initial working java importer for JPCT;
- [x] Create a java documented Sample Project;
- [x] Add Android  (JPCT-AE) support;
- [ ] Convert python to a real Blender exporter plugin;
- [ ] Add Support to Packed Textures (Blender Exporter);
- [ ] Export Blender Texture Flags (E.g: Alpha);

What is Working:

* 3D Objects Export/Import
* Textures Export/Import (just unpacked!)
* Instances Export/Import (xml)
* Camera Export/Import (just one)
* Point Lights Export/Import (position, distance and color)
* Ambient Light Export/Import
* Actors Export/Import

 

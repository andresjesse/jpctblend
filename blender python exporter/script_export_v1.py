#TODO: Add as plugin

import bpy,os,shutil
import xml.etree.ElementTree as ET
from mathutils import Vector

class JPCTSceneExporter:
    #Adapt Blender to JPCT Coordinates
    class Coord:
        x = -1
        y = -1
        z = -1
    
    def __init__(self):
        self.coord = self.Coord()
        
        #Exporter Info
        self.__exporter_version = 1
        self.__exporter_info = 'JPCT Blender Exporter'
        self.__exporter_author = 'Andres Jesse Porfirio'
        self.__exporter_author_contact = 'www.andresjesse.com'
        self.__exporter_last_modification = '2013/11/12'
        
        #Meshs that should not be exported..
        self.special_mesh_names = ['Actor']
        
        print('JPCTBlend: exporting:'+os.path.basename(bpy.data.filepath))
        
        #create dirs and export media
        self.create_export_dir()
        self.export_meshs() 
        self.export_textures()
        
        #write jpctblend file
        self.export_jpctblend_file()
        
        print('Done!')
    
    #This function creates the export dir, with meshs and textures subfolders
    def create_export_dir(self):
        current_blend = os.path.basename(bpy.data.filepath)
        current_dir = bpy.data.filepath[:-len(current_blend)]
        
        self.export_dir = current_dir+'jpctblend_'+current_blend[:-6]+'/'
        self.meshs_dir = self.export_dir+'meshs/'
        self.textures_dir = self.export_dir+'textures/'
        
        if os.path.isdir(self.export_dir):
            shutil.rmtree(self.export_dir)
        
        os.makedirs(self.export_dir)
        os.makedirs(self.meshs_dir)
        os.makedirs(self.textures_dir)
        
    #Given a mesh, this is a little shortcut to: select, clean params, export 3ds, restore params
    def export_3ds(self, mesh):
        mesh_unique_name = mesh.name.split('.')[0]
        if mesh_unique_name in self.exported_meshs:
            return
        
        bpy.ops.object.select_all(action='DESELECT')
        mesh.select = True
        
        loc = Vector(mesh.location)
        mesh.location = Vector((0,0,0))
        
        #TODO: 3-dimension scale not available in JPCT.
        #scl = Vector((mesh.scale))
        #mesh.scale = Vector((1,1,1))
        
        bpy.ops.export_scene.autodesk_3ds(filepath=self.meshs_dir + mesh_unique_name +'.3ds',check_existing=False,filter_glob="*.3ds",use_selection=True, axis_forward='-Z',axis_up='-Y')
        
        mesh.location = loc
        #mesh.scale = scl
        
        mesh.select = False
        
        self.exported_meshs.append(mesh_unique_name)
        
    #This function writes a .xml file (that can be loaded in java)
    def export_jpctblend_file(self):
        
        xml_root = ET.Element('jpctblend_scene')
        
        #add xml elements
        self.xml_add_exporter_info(xml_root)
        self.xml_add_textures(xml_root)
        self.xml_add_instances(xml_root)
        self.xml_add_lights(xml_root)
        self.xml_add_camera(xml_root)
        self.xml_add_actors(xml_root)
        
        #write final xml document
        tree = ET.ElementTree(xml_root)
        tree.write(self.export_dir+os.path.basename(bpy.data.filepath)[:-6]+'.xml')
        
    #Writes all meshs in the specified folder (uses 3ds native exporter)
    def export_meshs(self):
        meshs_list = self.generate_meshs_list()
        self.exported_meshs = []
        for mesh in meshs_list:
            self.export_3ds(mesh)
            
    #Collects and copy all textures to the specified folder
    def export_textures(self):
        textures_list = self.generate_textures_list()
        for texture in bpy.data.images:
            if texture.name in textures_list:
                src = bpy.path.abspath(texture.filepath)
                dst = self.textures_dir+os.path.basename(texture.filepath)
                shutil.copy(src,dst)
    
    #Functions used to collect information from blender scene
    def generate_actors_list(self):
        objs_data = list(bpy.data.objects)
        return [m for m in objs_data if m.type == 'MESH' if m.name.split('.')[0] == 'Actor']
    
    def generate_lights_list(self):
        return [m for m in list(bpy.data.objects) if m.type == 'LAMP']
                
    def generate_meshs_list(self):
        objs_data = list(bpy.data.objects)
        return [m for m in objs_data if m.type == 'MESH' if m.name.split('.')[0] not in self.special_mesh_names]
    
    def generate_textures_list(self):
        return set([self.get_mesh_texture(mesh) for mesh in self.generate_meshs_list()])
    
    def get_mesh_texture(self, mesh):
        img_name = mesh.data.uv_textures[0].data[0].image.name
        return img_name
    
    #Functions used to generate and add xml elementos to root
    def xml_add_actors(self,xml_root):
        xml_actors = ET.SubElement(xml_root, 'actors')
        
        for actor in self.generate_actors_list():
            actor_unique_name = actor.name.split('.')[1]
            
            xml_current_actor = ET.SubElement(xml_actors, 'actor')
        
            xml_current_actor.set('javaclass',actor_unique_name)
            xml_current_actor.set('position',str(self.coord.x*actor.location[0])+','+str(self.coord.y*actor.location[2])+','+str(self.coord.z*actor.location[1]))
            xml_current_actor.set('rotation',str(self.coord.x*actor.rotation_euler[0])+','+str(self.coord.y*actor.rotation_euler[2])+','+str(self.coord.z*actor.rotation_euler[1]))
    
    def xml_add_camera(self,xml_root):
        cam = bpy.context.scene.camera
        
        xml_camera = ET.SubElement(xml_root, 'camera')
        
        xml_camera.set('position',str(self.coord.x*cam.location[0])+','+str(self.coord.y*cam.location[2])+','+str(self.coord.z*cam.location[1]))
        
        cam_lookat = cam.matrix_world * Vector((0, 0, -1))
        xml_camera.set('lookat',str(self.coord.x*cam_lookat[0])+','+str(self.coord.y*cam_lookat[2])+','+str(self.coord.z*cam_lookat[1]))
    
    def xml_add_exporter_info(self,xml_root):
        xml_header = ET.SubElement(xml_root, 'exporter_info')
        xml_header.set('version',str(self.__exporter_version))
        xml_header.set('script',self.__exporter_info)
        xml_header.set('author',self.__exporter_author)
        xml_header.set('contact',self.__exporter_author_contact)
        xml_header.set('date',self.__exporter_last_modification)

    def xml_add_instances(self,xml_root):
        xml_instances = ET.SubElement(xml_root, 'instances')
        
        for mesh in self.generate_meshs_list():
            xml_inst = ET.SubElement(xml_instances, 'instance')
            
            xml_inst.set('mesh_name',mesh.name.split('.')[0])
            xml_inst.set('texture',self.get_mesh_texture(mesh))
            xml_inst.set('position',str(self.coord.x*mesh.location[0])+','+str(self.coord.y*mesh.location[2])+','+str(self.coord.z*mesh.location[1]))
            xml_inst.set('scale',str(mesh.scale[0])+','+str(mesh.scale[1])+','+str(mesh.scale[2]))
            xml_inst.set('rotation',str(self.coord.x*mesh.rotation_euler[0])+','+str(self.coord.y*mesh.rotation_euler[2])+','+str(self.coord.z*mesh.rotation_euler[1]))

    def xml_add_lights(self,xml_root):
        xml_lights = ET.SubElement(xml_root, 'lights')
        
        for light in self.generate_lights_list():
            #export only point lights for now
            if light.data.type != 'POINT':
                continue
            
            xml_current_light = ET.SubElement(xml_lights, 'pointlight')
            xml_current_light.set('position',str(self.coord.x*light.location[0])+','+str(self.coord.y*light.location[2])+','+str(self.coord.z*light.location[1]))
            xml_current_light.set('distance',str(light.data.distance))
            xml_current_light.set('rgbcolor',str(light.data.color[0])+','+str(light.data.color[1])+','+str(light.data.color[2]))
            #TODO:check light props.
        
        xml_ambient_light = ET.SubElement(xml_lights,'ambient')
        xml_ambient_light.set('rgbcolor',str(bpy.context.scene.world.ambient_color[0])+','+str(bpy.context.scene.world.ambient_color[1])+','+str(bpy.context.scene.world.ambient_color[2]))

    def xml_add_textures(self,xml_root):
        textures_list = self.generate_textures_list()
        
        xml_textures = ET.SubElement(xml_root, 'textures')
        
        for texture in bpy.data.images:
            if texture.name in textures_list:
                xml_current_texture = ET.SubElement(xml_textures, os.path.basename(texture.filepath))
                #TODO: xml_current_texture.set('alpha',..) #write texture props..
                
if __name__ == '__main__':
    JPCTSceneExporter()
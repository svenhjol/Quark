package vazkii.quark.content.tools.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Function;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import vazkii.quark.base.Quark;

@Environment(EnvType.CLIENT)
public class GlintRenderType {
	
    public static List<RenderLayer> glintColor = newRenderList(GlintRenderType::buildGlintRenderType);
    public static List<RenderLayer> entityGlintColor = newRenderList(GlintRenderType::buildEntityGlintRenderType);
    public static List<RenderLayer> glintDirectColor = newRenderList(GlintRenderType::buildGlintDirectRenderType);
    public static List<RenderLayer> entityGlintDirectColor = newRenderList(GlintRenderType::buildEntityGlintDriectRenderType);
    
    public static List<RenderLayer> armorGlintColor = newRenderList(GlintRenderType::buildArmorGlintRenderType);
    public static List<RenderLayer> armorEntityGlintColor = newRenderList(GlintRenderType::buildArmorEntityGlintRenderType);

    public static void addGlintTypes(Object2ObjectLinkedOpenHashMap<RenderLayer, BufferBuilder> map) {
    	addGlintTypes(map, glintColor);
    	addGlintTypes(map, entityGlintColor);
    	addGlintTypes(map, glintDirectColor);
    	addGlintTypes(map, entityGlintDirectColor);
    	addGlintTypes(map, armorGlintColor);
    	addGlintTypes(map, armorEntityGlintColor);
    }
    
    private static List<RenderLayer> newRenderList(Function<String, RenderLayer> func) {
    	ArrayList<RenderLayer> list = new ArrayList<>(17);
    	
        for (DyeColor color : DyeColor.values())
        	list.add(func.apply(color.getName()));
        list.add(func.apply("rainbow"));
        
        return list;
    }
    
    private static void addGlintTypes(Object2ObjectLinkedOpenHashMap<RenderLayer, BufferBuilder> map, List<RenderLayer> typeList) {
    	for(RenderLayer renderType : typeList)
    		if (!map.containsKey(renderType))
    			map.put(renderType, new BufferBuilder(renderType.getExpectedBufferSize()));
    }

    private static RenderLayer buildGlintRenderType(String name) {
        final Identifier res = new Identifier(Quark.MOD_ID, "textures/glint/enchanted_item_glint_" + name + ".png");

        return RenderLayer.of("glint_" + name, VertexFormats.POSITION_TEXTURE, 7, 256, RenderLayer.MultiPhaseParameters.builder()
            .texture(new RenderPhase.Texture(res, true, false))
            .writeMaskState(RenderPhase.COLOR_MASK)
            .cull(RenderPhase.DISABLE_CULLING)
            .depthTest(RenderPhase.EQUAL_DEPTH_TEST)
            .transparency(RenderPhase.GLINT_TRANSPARENCY)
            .target(RenderPhase.ITEM_TARGET)
            .texturing(RenderPhase.GLINT_TEXTURING)
            .build(false));
    }

    private static RenderLayer buildEntityGlintRenderType(String name) {
        final Identifier res = new Identifier(Quark.MOD_ID, "textures/glint/enchanted_item_glint_" + name + ".png");

        return RenderLayer.of("entity_glint_" + name, VertexFormats.POSITION_TEXTURE, 7, 256, RenderLayer.MultiPhaseParameters.builder()
            .texture(new RenderPhase.Texture(res, true, false))
            .writeMaskState(RenderPhase.COLOR_MASK)
            .cull(RenderPhase.DISABLE_CULLING)
            .depthTest(RenderPhase.EQUAL_DEPTH_TEST)
            .transparency(RenderPhase.GLINT_TRANSPARENCY)
            .target(RenderPhase.ITEM_TARGET)
            .texturing(RenderPhase.ENTITY_GLINT_TEXTURING)
            .build(false));
    }

 
    private static RenderLayer buildGlintDirectRenderType(String name) {
        final Identifier res = new Identifier(Quark.MOD_ID, "textures/glint/enchanted_item_glint_" + name + ".png");

        return RenderLayer.of("glint_direct_" + name, VertexFormats.POSITION_TEXTURE, 7, 256, RenderLayer.MultiPhaseParameters.builder()
            .texture(new RenderPhase.Texture(res, true, false))
            .writeMaskState(RenderPhase.COLOR_MASK)
            .cull(RenderPhase.DISABLE_CULLING)
            .depthTest(RenderPhase.EQUAL_DEPTH_TEST)
            .transparency(RenderPhase.GLINT_TRANSPARENCY)
            .texturing(RenderPhase.GLINT_TEXTURING)
            .build(false));
    }

    
    private static RenderLayer buildEntityGlintDriectRenderType(String name) {
        final Identifier res = new Identifier(Quark.MOD_ID, "textures/glint/enchanted_item_glint_" + name + ".png");

        return RenderLayer.of("entity_glint_direct_" + name, VertexFormats.POSITION_TEXTURE, 7, 256, RenderLayer.MultiPhaseParameters.builder()
            .texture(new RenderPhase.Texture(res, true, false))
            .writeMaskState(RenderPhase.COLOR_MASK)
            .cull(RenderPhase.DISABLE_CULLING)
            .depthTest(RenderPhase.EQUAL_DEPTH_TEST)
            .transparency(RenderPhase.GLINT_TRANSPARENCY)
            .texturing(RenderPhase.ENTITY_GLINT_TEXTURING)
            .build(false));
    }
    
    private static RenderLayer buildArmorGlintRenderType(String name) {
        final Identifier res = new Identifier(Quark.MOD_ID, "textures/glint/enchanted_item_glint_" + name + ".png");
        
        return RenderLayer.of("entity_glint_direct_" + name, VertexFormats.POSITION_TEXTURE, 7, 256, RenderLayer.MultiPhaseParameters.builder()
            .texture(new RenderPhase.Texture(res, true, false))
            .writeMaskState(RenderPhase.COLOR_MASK)
            .cull(RenderPhase.DISABLE_CULLING)
            .depthTest(RenderPhase.EQUAL_DEPTH_TEST)
            .transparency(RenderPhase.GLINT_TRANSPARENCY)
            .texturing(RenderPhase.ENTITY_GLINT_TEXTURING)
            .layering(RenderPhase.VIEW_OFFSET_Z_LAYERING)
            .build(false));
    }
    
    private static RenderLayer buildArmorEntityGlintRenderType(String name) {
        final Identifier res = new Identifier(Quark.MOD_ID, "textures/glint/enchanted_item_glint_" + name + ".png");

        return RenderLayer.of("entity_glint_direct_" + name, VertexFormats.POSITION_TEXTURE, 7, 256, RenderLayer.MultiPhaseParameters.builder()
            .texture(new RenderPhase.Texture(res, true, false))
            .writeMaskState(RenderPhase.COLOR_MASK)
            .cull(RenderPhase.DISABLE_CULLING)
            .depthTest(RenderPhase.EQUAL_DEPTH_TEST)
            .transparency(RenderPhase.GLINT_TRANSPARENCY)
            .texturing(RenderPhase.ENTITY_GLINT_TEXTURING)
            .layering(RenderPhase.VIEW_OFFSET_Z_LAYERING)
            .build(false));
    }
}

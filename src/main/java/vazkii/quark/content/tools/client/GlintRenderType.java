package vazkii.quark.content.tools.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Function;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.DyeColor;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import vazkii.quark.base.Quark;

@OnlyIn(Dist.CLIENT)
public class GlintRenderType {
	
    public static List<RenderType> glintColor = newRenderList(GlintRenderType::buildGlintRenderType);
    public static List<RenderType> entityGlintColor = newRenderList(GlintRenderType::buildEntityGlintRenderType);
    public static List<RenderType> glintDirectColor = newRenderList(GlintRenderType::buildGlintDirectRenderType);
    public static List<RenderType> entityGlintDirectColor = newRenderList(GlintRenderType::buildEntityGlintDriectRenderType);
    
    public static List<RenderType> armorGlintColor = newRenderList(GlintRenderType::buildArmorGlintRenderType);
    public static List<RenderType> armorEntityGlintColor = newRenderList(GlintRenderType::buildArmorEntityGlintRenderType);

    public static void addGlintTypes(Object2ObjectLinkedOpenHashMap<RenderType, BufferBuilder> map) {
    	addGlintTypes(map, glintColor);
    	addGlintTypes(map, entityGlintColor);
    	addGlintTypes(map, glintDirectColor);
    	addGlintTypes(map, entityGlintDirectColor);
    	addGlintTypes(map, armorGlintColor);
    	addGlintTypes(map, armorEntityGlintColor);
    }
    
    private static List<RenderType> newRenderList(Function<String, RenderType> func) {
    	ArrayList<RenderType> list = new ArrayList<>(17);
    	
        for (DyeColor color : DyeColor.values())
        	list.add(func.apply(color.getTranslationKey()));
        list.add(func.apply("rainbow"));
        
        return list;
    }
    
    private static void addGlintTypes(Object2ObjectLinkedOpenHashMap<RenderType, BufferBuilder> map, List<RenderType> typeList) {
    	for(RenderType renderType : typeList)
    		if (!map.containsKey(renderType))
    			map.put(renderType, new BufferBuilder(renderType.getBufferSize()));
    }

    private static RenderType buildGlintRenderType(String name) {
        final ResourceLocation res = new ResourceLocation(Quark.MOD_ID, "textures/glint/enchanted_item_glint_" + name + ".png");

        return RenderType.makeType("glint_" + name, DefaultVertexFormats.POSITION_TEX, 7, 256, RenderType.State.getBuilder()
            .texture(new RenderState.TextureState(res, true, false))
            .writeMask(RenderState.COLOR_WRITE)
            .cull(RenderState.CULL_DISABLED)
            .depthTest(RenderState.DEPTH_EQUAL)
            .transparency(RenderState.GLINT_TRANSPARENCY)
            .target(RenderState.field_241712_U_)
            .texturing(RenderState.GLINT_TEXTURING)
            .build(false));
    }

    private static RenderType buildEntityGlintRenderType(String name) {
        final ResourceLocation res = new ResourceLocation(Quark.MOD_ID, "textures/glint/enchanted_item_glint_" + name + ".png");

        return RenderType.makeType("entity_glint_" + name, DefaultVertexFormats.POSITION_TEX, 7, 256, RenderType.State.getBuilder()
            .texture(new RenderState.TextureState(res, true, false))
            .writeMask(RenderState.COLOR_WRITE)
            .cull(RenderState.CULL_DISABLED)
            .depthTest(RenderState.DEPTH_EQUAL)
            .transparency(RenderState.GLINT_TRANSPARENCY)
            .target(RenderState.field_241712_U_)
            .texturing(RenderState.ENTITY_GLINT_TEXTURING)
            .build(false));
    }

 
    private static RenderType buildGlintDirectRenderType(String name) {
        final ResourceLocation res = new ResourceLocation(Quark.MOD_ID, "textures/glint/enchanted_item_glint_" + name + ".png");

        return RenderType.makeType("glint_direct_" + name, DefaultVertexFormats.POSITION_TEX, 7, 256, RenderType.State.getBuilder()
            .texture(new RenderState.TextureState(res, true, false))
            .writeMask(RenderState.COLOR_WRITE)
            .cull(RenderState.CULL_DISABLED)
            .depthTest(RenderState.DEPTH_EQUAL)
            .transparency(RenderState.GLINT_TRANSPARENCY)
            .texturing(RenderState.GLINT_TEXTURING)
            .build(false));
    }

    
    private static RenderType buildEntityGlintDriectRenderType(String name) {
        final ResourceLocation res = new ResourceLocation(Quark.MOD_ID, "textures/glint/enchanted_item_glint_" + name + ".png");

        return RenderType.makeType("entity_glint_direct_" + name, DefaultVertexFormats.POSITION_TEX, 7, 256, RenderType.State.getBuilder()
            .texture(new RenderState.TextureState(res, true, false))
            .writeMask(RenderState.COLOR_WRITE)
            .cull(RenderState.CULL_DISABLED)
            .depthTest(RenderState.DEPTH_EQUAL)
            .transparency(RenderState.GLINT_TRANSPARENCY)
            .texturing(RenderState.ENTITY_GLINT_TEXTURING)
            .build(false));
    }
    
    private static RenderType buildArmorGlintRenderType(String name) {
        final ResourceLocation res = new ResourceLocation(Quark.MOD_ID, "textures/glint/enchanted_item_glint_" + name + ".png");
        
        return RenderType.makeType("entity_glint_direct_" + name, DefaultVertexFormats.POSITION_TEX, 7, 256, RenderType.State.getBuilder()
            .texture(new RenderState.TextureState(res, true, false))
            .writeMask(RenderState.COLOR_WRITE)
            .cull(RenderState.CULL_DISABLED)
            .depthTest(RenderState.DEPTH_EQUAL)
            .transparency(RenderState.GLINT_TRANSPARENCY)
            .texturing(RenderState.ENTITY_GLINT_TEXTURING)
            .layer(RenderState.field_239235_M_)
            .build(false));
    }
    
    private static RenderType buildArmorEntityGlintRenderType(String name) {
        final ResourceLocation res = new ResourceLocation(Quark.MOD_ID, "textures/glint/enchanted_item_glint_" + name + ".png");

        return RenderType.makeType("entity_glint_direct_" + name, DefaultVertexFormats.POSITION_TEX, 7, 256, RenderType.State.getBuilder()
            .texture(new RenderState.TextureState(res, true, false))
            .writeMask(RenderState.COLOR_WRITE)
            .cull(RenderState.CULL_DISABLED)
            .depthTest(RenderState.DEPTH_EQUAL)
            .transparency(RenderState.GLINT_TRANSPARENCY)
            .texturing(RenderState.ENTITY_GLINT_TEXTURING)
            .layer(RenderState.field_239235_M_)
            .build(false));
    }
}

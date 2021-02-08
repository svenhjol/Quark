package vazkii.quark.base.handler;

import java.util.HashMap;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;

public class RenderLayerHandler {
	
	private static final Map<Block, RenderTypeSkeleton> mapping = new HashMap<>();
	private static final Map<Block, Block> inheritances = new HashMap<>();
	
	@Environment(EnvType.CLIENT)
	private static Map<RenderTypeSkeleton, RenderLayer> renderTypes;

	public static void setRenderType(Block block, RenderTypeSkeleton skeleton) {
		DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
			setRenderTypeClient(block, skeleton);
		});
	}
	
	public static void setInherited(Block block, Block parent) {
		DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
			setInheritedClient(block, parent);
		});
	}
	
	@Environment(EnvType.CLIENT)
	public static void init() {
		for(Block b : inheritances.keySet()) {
			Block inherit = inheritances.get(b);
			if(mapping.containsKey(inherit))
				mapping.put(b, mapping.get(inherit));
		}
		
		for(Block b : mapping.keySet())
			RenderLayers.setRenderLayer(b, renderTypes.get(mapping.get(b)));
		
		inheritances.clear();
		mapping.clear();
	}
	
	@Environment(EnvType.CLIENT)
	private static void setRenderTypeClient(Block block, RenderTypeSkeleton skeleton) {
		resolveRenderTypes();
		mapping.put(block, skeleton);
	}
	
	@Environment(EnvType.CLIENT)
	private static void setInheritedClient(Block block, Block parent) {
		resolveRenderTypes();
		inheritances.put(block, parent);
		
	}

	@Environment(EnvType.CLIENT)
	private static void resolveRenderTypes() {
		if(renderTypes == null) {
			renderTypes = new HashMap<>();
			
			renderTypes.put(RenderTypeSkeleton.SOLID, RenderLayer.getSolid());
			renderTypes.put(RenderTypeSkeleton.CUTOUT, RenderLayer.getCutout());
			renderTypes.put(RenderTypeSkeleton.CUTOUT_MIPPED, RenderLayer.getCutoutMipped());
			renderTypes.put(RenderTypeSkeleton.TRANSLUCENT, RenderLayer.getTranslucent());
		}
	}
	
	public static enum RenderTypeSkeleton {
		
		SOLID,
		CUTOUT,
		CUTOUT_MIPPED,
		TRANSLUCENT;
		
	}
	
}

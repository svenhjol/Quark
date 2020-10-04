package vazkii.quark.oddities.module;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import vazkii.arl.util.RegistryHelper;
import vazkii.quark.base.Quark;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.Module;
import vazkii.quark.base.module.ModuleCategory;
import vazkii.quark.base.module.config.Config;
import vazkii.quark.oddities.block.PipeBlock;
import vazkii.quark.oddities.client.render.PipeTileEntityRenderer;
import vazkii.quark.oddities.tile.PipeTileEntity;

@LoadModule(category = ModuleCategory.ODDITIES, requiredMod = Quark.ODDITIES_ID)
public class PipesModule extends Module {

    public static BlockEntityType<PipeTileEntity> tileEntityType;

	@Config(description = "How long it takes for an item to cross a pipe. Bigger = slower.") 
	public static int pipeSpeed = 5;
	
	@Config(description = "Set to 0 if you don't want pipes to have a max amount of items")
	public static int maxPipeItems = 16;
	
	@Config(description = "When items eject or are absorbed by pipes, should they make sounds?")
	public static boolean doPipesWhoosh = true;
    
    @Override
    public void construct() {
    	Block pipe = new PipeBlock(this);
    	
    	tileEntityType = BlockEntityType.Builder.create(PipeTileEntity::new, pipe).build(null);
		RegistryHelper.register(tileEntityType, "pipe");
    }
    
    @Override
    public void configChanged() {
    	pipeSpeed = pipeSpeed * 2;
    }

	@Override
	@Environment(EnvType.CLIENT)
	public void clientSetup() {
		ClientRegistry.bindTileEntityRenderer(tileEntityType, PipeTileEntityRenderer::new);
	}
	
}

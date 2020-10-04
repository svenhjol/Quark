package vazkii.quark.automation.module;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.render.entity.FallingBlockEntityRenderer;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import vazkii.arl.util.RegistryHelper;
import vazkii.quark.automation.block.GravisandBlock;
import vazkii.quark.automation.entity.GravisandEntity;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.Module;
import vazkii.quark.base.module.ModuleCategory;

@LoadModule(category = ModuleCategory.AUTOMATION)
public class GravisandModule extends Module {

	public static EntityType<GravisandEntity> gravisandType;

	public static Block gravisand;

	@Override
	public void construct() {
		gravisand = new GravisandBlock("gravisand", this, ItemGroup.REDSTONE, Block.Properties.copy(Blocks.SAND));

		gravisandType = EntityType.Builder.<GravisandEntity>create(GravisandEntity::new, SpawnGroup.MISC)
				.setDimensions(0.98F, 0.98F)
				.setTrackingRange(160)
				.setUpdateInterval(20)
				.setShouldReceiveVelocityUpdates(true)
				.setCustomClientFactory((spawnEntity, world) -> new GravisandEntity(gravisandType, world))
				.build("gravisand");
		RegistryHelper.register(gravisandType, "gravisand");
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void clientSetup() {
		RenderingRegistry.registerEntityRenderingHandler(gravisandType, FallingBlockEntityRenderer::new);
	}
}

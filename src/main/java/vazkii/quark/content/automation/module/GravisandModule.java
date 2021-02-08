package vazkii.quark.content.automation.module;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.renderer.entity.FallingBlockRenderer;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import vazkii.arl.util.RegistryHelper;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.content.automation.block.GravisandBlock;
import vazkii.quark.content.automation.entity.GravisandEntity;
import vazkii.quark.base.module.ModuleCategory;

@LoadModule(category = ModuleCategory.AUTOMATION)
public class GravisandModule extends QuarkModule {

	public static EntityType<GravisandEntity> gravisandType;

	public static Block gravisand;

	@Override
	public void construct() {
		gravisand = new GravisandBlock("gravisand", this, ItemGroup.REDSTONE, Block.Properties.from(Blocks.SAND));

		gravisandType = EntityType.Builder.<GravisandEntity>create(GravisandEntity::new, EntityClassification.MISC)
				.size(0.98F, 0.98F)
				.setTrackingRange(160)
				.setUpdateInterval(20)
				.setShouldReceiveVelocityUpdates(true)
				.setCustomClientFactory((spawnEntity, world) -> new GravisandEntity(gravisandType, world))
				.build("gravisand");
		RegistryHelper.register(gravisandType, "gravisand");
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void clientSetup() {
		RenderingRegistry.registerEntityRenderingHandler(gravisandType, FallingBlockRenderer::new);
	}
}

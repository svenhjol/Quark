package vazkii.quark.content.building.block;

import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.block.MaterialColor;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemGroup;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;
import vazkii.quark.base.block.QuarkFlammableBlock;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.content.building.module.ThatchModule;

public class ThatchBlock extends QuarkFlammableBlock {

	public ThatchBlock(QuarkModule module) {
		super("thatch", module, ItemGroup.BUILDING_BLOCKS, 300,
				Block.Properties.of(Material.SOLID_ORGANIC, MaterialColor.YELLOW)
				.harvestTool(ToolType.HOE)
				.strength(0.5F)
				.sounds(BlockSoundGroup.GRASS));
	}
	
	@Override
	public void onLandedUpon(World worldIn, BlockPos pos, Entity entityIn, float fallDistance) {
		entityIn.handleFallDamage(fallDistance, (float) ThatchModule.fallDamageMultiplier);
	}

}

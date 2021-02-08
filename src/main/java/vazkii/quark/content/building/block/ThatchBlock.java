package vazkii.quark.content.building.block;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;
import vazkii.quark.base.block.QuarkFlammableBlock;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.content.building.module.ThatchModule;

public class ThatchBlock extends QuarkFlammableBlock {

	public ThatchBlock(QuarkModule module) {
		super("thatch", module, ItemGroup.BUILDING_BLOCKS, 300,
				Block.Properties.create(Material.ORGANIC, MaterialColor.YELLOW)
				.harvestTool(ToolType.HOE)
				.hardnessAndResistance(0.5F)
				.sound(SoundType.PLANT));
	}
	
	@Override
	public void onFallenUpon(World worldIn, BlockPos pos, Entity entityIn, float fallDistance) {
		entityIn.onLivingFall(fallDistance, (float) ThatchModule.fallDamageMultiplier);
	}

}

package vazkii.quark.building.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnRestriction.Location;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldView;
import vazkii.quark.base.block.QuarkBlock;
import vazkii.quark.base.handler.VariantHandler.IVariantsShouldBeEmissive;
import vazkii.quark.base.module.Module;

public class MagmaBrickBlock extends QuarkBlock implements IVariantsShouldBeEmissive {

	public MagmaBrickBlock(Module module) {
		super("magma_bricks", module, ItemGroup.BUILDING_BLOCKS, 
				Block.Properties.copy(Blocks.MAGMA_BLOCK)
				.strength(1.5F, 10F)
				.emissiveLighting((s, r, p) -> true)); // emissive rendering
	}
	
	@Override
	public boolean isFireSource(BlockState state, WorldView world, BlockPos pos, Direction side) {
		return true;
	}
	
	@Override
	public boolean canCreatureSpawn(BlockState state, BlockView world, BlockPos pos, Location type, EntityType<?> entityType) {
		return entityType.isFireImmune();
	}

}
 
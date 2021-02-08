package vazkii.quark.content.world.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import vazkii.quark.base.block.QuarkBlock;
import vazkii.quark.base.handler.RenderLayerHandler;
import vazkii.quark.base.handler.RenderLayerHandler.RenderTypeSkeleton;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.content.world.tile.MonsterBoxTileEntity;

public class MonsterBoxBlock extends QuarkBlock {

	public MonsterBoxBlock(QuarkModule module) {
		super("monster_box", module, null,
				Block.Properties.of(Material.METAL)
				.strength(25F)
				.sounds(BlockSoundGroup.METAL)
				.nonOpaque());
		
		RenderLayerHandler.setRenderType(this, RenderTypeSkeleton.CUTOUT);
	}
	
	@Override
	public ItemStack getPickBlock(BlockState state, HitResult target, BlockView world, BlockPos pos, PlayerEntity player) {
		return ItemStack.EMPTY;
	}
	
	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}
	
	@Override
	public boolean canPathfindThrough(BlockState state, BlockView worldIn, BlockPos pos, NavigationType type) {
		return false;
	}
	
	@Override
	public BlockEntity createTileEntity(BlockState state, BlockView world) {
		return new MonsterBoxTileEntity();
	}

}

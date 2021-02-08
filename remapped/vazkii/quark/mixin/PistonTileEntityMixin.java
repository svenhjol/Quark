package vazkii.quark.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.PistonBlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import vazkii.quark.content.automation.module.PistonsMoveTileEntitiesModule;

@Mixin(PistonBlockEntity.class)
public class PistonTileEntityMixin {

	@Redirect(method = {"tick", "finish"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z"))
	private boolean setPistonBlock(World world, BlockPos pos, BlockState newState, int flags) {
		return PistonsMoveTileEntitiesModule.setPistonBlock(world, pos, newState, flags);
	}
}

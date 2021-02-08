package vazkii.quark.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.BlockState;
import net.minecraft.block.TrapdoorBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;
import vazkii.quark.content.building.module.VariantLaddersModule;

@Mixin(TrapdoorBlock.class)
public class TrapDoorBlockMixin {

	@Inject(method = "isLadder", at = @At("RETURN"), cancellable = true, remap = false)
	private void isTrapdoorLadder(BlockState state, WorldView world, BlockPos pos, LivingEntity entity, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
		callbackInfoReturnable.setReturnValue(VariantLaddersModule.isTrapdoorLadder(callbackInfoReturnable.getReturnValue(), world, pos));
	}
}

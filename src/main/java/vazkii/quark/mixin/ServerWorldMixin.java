package vazkii.quark.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import vazkii.quark.content.mobs.entity.CrabEntity;
import vazkii.quark.content.tweaks.module.ImprovedSleepingModule;

@Mixin(ServerWorld.class)
public class ServerWorldMixin {

	@Shadow
	private boolean allPlayersSleeping;

	@Inject(method = "updateAllPlayersSleepingFlag", at = @At(value = "FIELD", target = "Lnet/minecraft/world/server/ServerWorld;allPlayersSleeping:Z", ordinal = 1, shift = At.Shift.AFTER))
	private void updateAllPlayersSleepingFlag(CallbackInfo callbackInfo) {
		allPlayersSleeping = ImprovedSleepingModule.isEveryoneAsleep(allPlayersSleeping);
	}

	@Inject(method = "playEvent", at = @At("HEAD"))
	private void rave(PlayerEntity player, int type, BlockPos pos, int data, CallbackInfo callbackInfo) {
		if(type == 1010)
			CrabEntity.rave((ServerWorld) (Object) this, pos, data != 0);
	}
}

package vazkii.quark.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.entity.ai.goal.TemptGoal;
import net.minecraft.entity.player.PlayerEntity;
import vazkii.quark.content.automation.module.FeedingTroughModule;

@Mixin(TemptGoal.class)
public class TemptGoalMixin {

	@Shadow
	protected PlayerEntity closestPlayer;

	@Inject(method = "shouldExecute", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/ai/goal/TemptGoal;closestPlayer:Lnet/minecraft/entity/player/PlayerEntity;", ordinal = 0, shift = At.Shift.AFTER))
	private void findTroughs(CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
		closestPlayer = FeedingTroughModule.temptWithTroughs((TemptGoal) (Object) this, closestPlayer);
	}
}

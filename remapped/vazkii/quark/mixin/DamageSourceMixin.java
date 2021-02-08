package vazkii.quark.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import vazkii.quark.content.tools.module.PickarangModule;

@Mixin(DamageSource.class)
public class DamageSourceMixin {

	@Inject(method = "player", at = @At("HEAD"), cancellable = true)
	private static void causePlayerDamage(PlayerEntity player, CallbackInfoReturnable<DamageSource> callbackInfoReturnable) {
		DamageSource damage = PickarangModule.createDamageSource(player);

		if(damage != null)
			callbackInfoReturnable.setReturnValue(damage);
	}
}

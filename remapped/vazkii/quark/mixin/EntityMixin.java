package vazkii.quark.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.entity.Entity;
import net.minecraft.entity.MovementType;
import net.minecraft.util.math.Vec3d;
import vazkii.quark.content.automation.module.ChainLinkageModule;
import vazkii.quark.content.tools.module.PickarangModule;
import vazkii.quark.content.tweaks.module.SpringySlimeModule;

@Mixin(Entity.class)
public class EntityMixin {

	@Inject(method = "tick", at = @At("HEAD"))
	private void tick(CallbackInfo callbackInfo) {
		ChainLinkageModule.onEntityUpdate	((Entity) (Object) this);
	}

	@Inject(method = "isFireImmune", at = @At("RETURN"), cancellable = true)
	private void isImmuneToFire(CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
		callbackInfoReturnable.setReturnValue(PickarangModule.getIsFireResistant(callbackInfoReturnable.getReturnValue(), (Entity) (Object) this));
	}

	@Inject(method = "move", at = @At("HEAD"))
	private void recordMotion(CallbackInfo callbackInfo) {
		SpringySlimeModule.recordMotion((Entity) (Object) this);
	}

	@Inject(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;checkBlockCollision()V"), locals = LocalCapture.CAPTURE_FAILHARD)
	private void applyCollisionLogic(MovementType typeIn, Vec3d pos, CallbackInfo callbackInfo, Vec3d vector3d) {
		SpringySlimeModule.onEntityCollision((Entity) (Object) this, pos, vector3d);
	}
}

package vazkii.quark.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import net.minecraft.block.BlockState;
import net.minecraft.structure.Structure;
import vazkii.quark.content.building.module.VariantChestsModule;

@Mixin(Structure.class)
public class TemplateMixin {

	@ModifyVariable(method = "place(Lnet/minecraft/world/ServerWorldAccess;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/structure/StructurePlacementData;Ljava/util/Random;I)Z",
			at = @At(value = "FIELD", target = "Lnet/minecraft/structure/Structure$StructureBlockInfo;tag:Lnet/minecraft/nbt/CompoundTag;", ordinal = 0),
			index = 21)
	private BlockState captureLocalBlockstate(BlockState state) {
		return VariantChestsModule.getGenerationChestBlockState(state);
	}
}
	
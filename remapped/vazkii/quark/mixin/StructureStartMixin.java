package vazkii.quark.mixin;

import java.util.List;
import java.util.Random;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.StructureFeature;
import vazkii.quark.content.building.module.VariantChestsModule;

@Mixin(StructureStart.class)
public class StructureStartMixin {

	@Shadow
	@Final
	protected List<StructurePiece> children;
	
	@Shadow
	@Final
	private StructureFeature<?> feature;
	
	@Inject(method = "generateStructure", at = @At("HEAD"))
	public void injectReference(StructureWorldAccess p_230366_1_, StructureAccessor p_230366_2_, ChunkGenerator p_230366_3_, Random p_230366_4_, BlockBox p_230366_5_, ChunkPos p_230366_6_, CallbackInfo callback) {
		VariantChestsModule.setActiveStructure(feature, children);
	}
	
	@Inject(method = "generateStructure", at = @At("RETURN"))
	public void resetReference(StructureWorldAccess p_230366_1_, StructureAccessor p_230366_2_, ChunkGenerator p_230366_3_, Random p_230366_4_, BlockBox p_230366_5_, ChunkPos p_230366_6_, CallbackInfo callback) {
		VariantChestsModule.setActiveStructure(null, null);
	}
	
}
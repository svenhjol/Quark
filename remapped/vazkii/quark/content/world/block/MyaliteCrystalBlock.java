package vazkii.quark.content.world.block;

import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.block.MaterialColor;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemGroup;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldView;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ToolType;
import vazkii.quark.base.block.QuarkGlassBlock;
import vazkii.quark.base.handler.RenderLayerHandler;
import vazkii.quark.base.handler.RenderLayerHandler.RenderTypeSkeleton;
import vazkii.quark.base.module.QuarkModule;

public class MyaliteCrystalBlock extends QuarkGlassBlock implements IMyaliteColorProvider {

	public MyaliteCrystalBlock(QuarkModule module) {
		super("myalite_crystal", module, ItemGroup.DECORATIONS,
				Block.Properties.of(Material.GLASS, MaterialColor.PURPLE)
				.strength(0.5F, 1200F)
				.sounds(BlockSoundGroup.GLASS)
				.luminance(b -> 14)
				.harvestTool(ToolType.PICKAXE)
				.requiresTool()
				.harvestLevel(3)
				.ticksRandomly()
				.nonOpaque());

		RenderLayerHandler.setRenderType(this, RenderTypeSkeleton.TRANSLUCENT);
	}
    
    private static float[] decompColor(int color) {
        int r = (color & 0xFF0000) >> 16;
        int g = (color & 0xFF00) >> 8;
        int b = color & 0xFF;
        return new float[] { (float) r / 255.0F, (float) g / 255.0F, (float) b / 255.0F };
    }
    
	@Nullable
	@Override
	public float[] getBeaconColorMultiplier(BlockState state, WorldView world, BlockPos pos, BlockPos beaconPos) {
		return decompColor(IMyaliteColorProvider.getColor(pos, myaliteS(), myaliteB()));
	}

	@Override
	@Environment(EnvType.CLIENT)
	public Vec3d getFogColor(BlockState state, WorldView world, BlockPos pos, Entity entity, Vec3d originalColor, float partialTicks) {
		float[] color = decompColor(IMyaliteColorProvider.getColor(pos, myaliteS(), myaliteB()));
		return new Vec3d(color[0], color[1], color[2]);
	}
	
}

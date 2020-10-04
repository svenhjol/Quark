package vazkii.quark.world.block;

import java.util.Random;

import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.block.MaterialColor;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemGroup;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ToolType;
import vazkii.quark.base.block.QuarkGlassBlock;
import vazkii.quark.base.handler.RenderLayerHandler;
import vazkii.quark.base.handler.RenderLayerHandler.RenderTypeSkeleton;
import vazkii.quark.base.module.Module;
import vazkii.quark.world.module.underground.CaveCrystalUndergroundBiomeModule;

/**
 * @author WireSegal
 * Created at 12:31 PM on 9/19/19.
 */
public class CaveCrystalBlock extends QuarkGlassBlock {

	private final float[] colorComponents;
	private final Vec3d colorVector;

	public CaveCrystalBlock(String regname, int color, Module module, MaterialColor materialColor) {
		super(regname, module, ItemGroup.DECORATIONS,
				Block.Properties.of(Material.GLASS, materialColor)
				.strength(0.3F, 0F)
				.sounds(BlockSoundGroup.GLASS)
				.lightLevel(b -> 11) // lightValue
				.harvestTool(ToolType.PICKAXE)
				.requiresTool() // needs tool
				.harvestLevel(0)
				.ticksRandomly()
				.nonOpaque());

		float r = ((color >> 16) & 0xff) / 255f;
		float g = ((color >> 8) & 0xff) / 255f;
		float b = (color & 0xff) / 255f;
		colorComponents = new float[]{r, g, b};
		colorVector = new Vec3d(r, g, b);
		
		RenderLayerHandler.setRenderType(this, RenderTypeSkeleton.TRANSLUCENT);
	}

	private boolean canGrow(World world, BlockPos pos) {
		if(CaveCrystalUndergroundBiomeModule.caveCrystalGrowthChance >= 1 && pos.getY() < 24 && world.isAir(pos.up())) {
			int i;
			for(i = 1; world.getBlockState(pos.down(i)).getBlock() == this; ++i);

			return i < 4;
		}
		return false;
	}

	@Override
	public void scheduledTick(BlockState state, ServerWorld worldIn, BlockPos pos, Random random) {
		if(canGrow(worldIn, pos) && random.nextInt(CaveCrystalUndergroundBiomeModule.caveCrystalGrowthChance) == 0)
			worldIn.setBlockState(pos.up(), state);
	}

	@Override
	public void randomDisplayTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {
		if(canGrow(worldIn, pos)) {
			double d0 = (double)pos.getX() + rand.nextDouble();
			double d1 = (double)pos.getY() + rand.nextDouble();
			double d2 = (double)pos.getZ() + rand.nextDouble();
			worldIn.addParticle(ParticleTypes.AMBIENT_ENTITY_EFFECT, d0, d1, d2, colorComponents[0], colorComponents[1], colorComponents[2]);
		}
	}

	@Nullable
	@Override
	public float[] getBeaconColorMultiplier(BlockState state, WorldView world, BlockPos pos, BlockPos beaconPos) {
		return colorComponents;
	}

	@Override
	@Environment(EnvType.CLIENT)
	public Vec3d getFogColor(BlockState state, WorldView world, BlockPos pos, Entity entity, Vec3d originalColor, float partialTicks) {
		return colorVector;
	}

}

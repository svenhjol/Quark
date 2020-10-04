package vazkii.quark.tweaks.client.item;

import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Blocks;
import net.minecraft.client.item.ModelPredicateProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.CompassItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import vazkii.arl.util.ItemNBTHelper;
import vazkii.quark.tweaks.module.CompassesWorkEverywhereModule;

public class CompassAngleGetter {

	private static final String TAG_CALCULATED = "quark:compass_calculated";
	private static final String TAG_WAS_IN_NETHER = "quark:compass_in_nether";
	private static final String TAG_POSITION_SET = "quark:compass_position_set";
	private static final String TAG_NETHER_TARGET_X = "quark:nether_x";
	private static final String TAG_NETHER_TARGET_Z = "quark:nether_z";

	public static void tickCompass(PlayerEntity player, ItemStack stack) {
		boolean calculated = isCalculated(stack);
		boolean nether = player.world.getDimensionRegistryKey().getValue().equals(DimensionOptions.NETHER.getValue()); // getDimensionType().resourceLocation, THE_NETHER_KEY.resourceLocation()
		
		if(calculated) {
			boolean wasInNether = ItemNBTHelper.getBoolean(stack, TAG_WAS_IN_NETHER, false);
			BlockPos pos = player.getBlockPos(); // getPosition
			boolean isInPortal = player.world.getBlockState(pos).getBlock() == Blocks.NETHER_PORTAL;
			
			if(nether && !wasInNether && isInPortal) {
				ItemNBTHelper.setInt(stack, TAG_NETHER_TARGET_X, pos.getX());
				ItemNBTHelper.setInt(stack, TAG_NETHER_TARGET_Z, pos.getZ());
				ItemNBTHelper.setBoolean(stack, TAG_WAS_IN_NETHER, true);
				ItemNBTHelper.setBoolean(stack, TAG_POSITION_SET, true);
			} else if(!nether && wasInNether) {
				ItemNBTHelper.setBoolean(stack, TAG_WAS_IN_NETHER, false);
				ItemNBTHelper.setBoolean(stack, TAG_POSITION_SET, false);
			}
		} else {
			ItemNBTHelper.setBoolean(stack, TAG_CALCULATED, true);
			ItemNBTHelper.setBoolean(stack, TAG_WAS_IN_NETHER, nether);
		}
	}

	static boolean isCalculated(ItemStack stack) {
		return stack.hasTag() && ItemNBTHelper.getBoolean(stack, TAG_CALCULATED, false);
	}

	@Environment(EnvType.CLIENT)
	public static class Impl implements ModelPredicateProvider {
		
		private final Angle normalAngle = new Angle();
		private final Angle unknownAngle = new Angle();
		
		@Override
		@Environment(EnvType.CLIENT)
		public float call(@Nonnull ItemStack stack, @Nullable ClientWorld worldIn, @Nullable LivingEntity entityIn) {
			if(entityIn == null && !stack.isInFrame())
				return 0F;

			if(CompassesWorkEverywhereModule.enableCompassNerf && (!stack.hasTag() || !ItemNBTHelper.getBoolean(stack, TAG_CALCULATED, false)))
				return 0F;

			boolean carried = entityIn != null;
			Entity entity = carried ? entityIn : stack.getFrame();

			if (entity == null)
				return 0;

			if(worldIn == null && entity != null && entity.world instanceof ClientWorld)
				worldIn = (ClientWorld) entity.world;

			double angle;

			boolean calculate = false;
			BlockPos target = new BlockPos(0, 0, 0);

			Identifier dimension = worldIn.getDimensionRegistryKey().getValue();
			BlockPos lodestonePos = CompassItem.hasLodestone(stack) ? this.getLodestonePosition(worldIn, stack.getOrCreateTag()) : null;
			
			if(lodestonePos != null) {
				calculate = true;
				target = lodestonePos;
			} else if(dimension.equals(DimensionOptions.END.getValue()) && CompassesWorkEverywhereModule.enableEnd) // resourceLocation, THE_END_KEY.getResourceLocation()
				calculate = true;
			else if(dimension.equals(DimensionOptions.NETHER.getValue()) && isCalculated(stack) && CompassesWorkEverywhereModule.enableNether) { // resourceLocation, THE_END_KEY.getResourceLocation()
				boolean set = ItemNBTHelper.getBoolean(stack, TAG_POSITION_SET, false);
				if(set) {
					int x = ItemNBTHelper.getInt(stack, TAG_NETHER_TARGET_X, 0);
					int z = ItemNBTHelper.getInt(stack, TAG_NETHER_TARGET_Z, 0);
					calculate = true;
					target = new BlockPos(x, 0, z);
				}
			} else if(worldIn.getDimension().isNatural()) { // isSurfaceWorld
				calculate = true;
				target = getWorldSpawn(worldIn);
			}

			long gameTime = worldIn.getTime();
			if(calculate && target != null) {
				double d1 = carried ? entity.yaw : getFrameRotation((ItemFrameEntity)entity);
				d1 = MathHelper.floorMod(d1 / 360.0D, 1.0D);
				double d2 = getAngleToPosition(entity, target) / (Math.PI * 2D);

				if(carried) {
					if(normalAngle.needsUpdate(gameTime))
						normalAngle.wobble(gameTime, 0.5D - (d1 - 0.25D));
						angle = d2 + normalAngle.rotation;
				} else angle = 0.5D - (d1 - 0.25D - d2);
			} else {
				if(unknownAngle.needsUpdate(gameTime));
					unknownAngle.wobble(gameTime, Math.random());
					
				angle = unknownAngle.rotation + ((double) worldIn.hashCode() / Math.PI);
			}
			
			return MathHelper.floorMod((float) angle, 1.0F);
		}
		

		private double getFrameRotation(ItemFrameEntity frame) {
			return MathHelper.wrapDegrees(180 + frame.getHorizontalFacing().asRotation());
		}

		private double getAngleToPosition(Entity entity, BlockPos blockpos) {
			Vec3d pos = entity.getPos();
			return Math.atan2(blockpos.getZ() - pos.z, blockpos.getX() - pos.x);
		}

		// vanilla copy from here on out

		@Nullable 
		private BlockPos getLodestonePosition(World p_239442_1_, CompoundTag p_239442_2_) {
			boolean flag = p_239442_2_.contains("LodestonePos");
			boolean flag1 = p_239442_2_.contains("LodestoneDimension");
			if (flag && flag1) {
				Optional<RegistryKey<World>> optional = CompassItem.getLodestoneDimension(p_239442_2_);
				if (optional.isPresent() && p_239442_1_.getRegistryKey() == optional.get()) {
					return NbtHelper.toBlockPos(p_239442_2_.getCompound("LodestonePos"));
				}
			}

			return null;
		}
		
		@Nullable
		private BlockPos getWorldSpawn(ClientWorld p_239444_1_) {
			return p_239444_1_.getDimension().isNatural() ? p_239444_1_.getSpawnPos() : null;
		}
	
		@Environment(EnvType.CLIENT)
		private static class Angle {
			private double rotation;
			private double rota;
			private long lastUpdateTick;

			private boolean needsUpdate(long p_239448_1_) {
				return lastUpdateTick != p_239448_1_;
			}

			private void wobble(long gameTime, double angle) {
				lastUpdateTick = gameTime;
				double d0 = angle - rotation;
				d0 = MathHelper.floorMod(d0 + 0.5D, 1.0D) - 0.5D;
				rota += d0 * 0.1D;
				rota *= 0.8D;
				rotation = MathHelper.floorMod(rotation + rota, 1.0D);
			}
		}
		
	}
	



}

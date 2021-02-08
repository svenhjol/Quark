package vazkii.quark.content.tweaks.client.item;

import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.block.Blocks;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemFrameEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.CompassItem;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.Dimension;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import vazkii.arl.util.ItemNBTHelper;
import vazkii.quark.content.tweaks.module.CompassesWorkEverywhereModule;

public class CompassAngleGetter {

	private static final String TAG_CALCULATED = "quark:compass_calculated";
	private static final String TAG_WAS_IN_NETHER = "quark:compass_in_nether";
	private static final String TAG_POSITION_SET = "quark:compass_position_set";
	private static final String TAG_NETHER_TARGET_X = "quark:nether_x";
	private static final String TAG_NETHER_TARGET_Z = "quark:nether_z";

	public static void tickCompass(PlayerEntity player, ItemStack stack) {
		boolean calculated = isCalculated(stack);
		boolean nether = player.world.getDimensionKey().getLocation().equals(Dimension.THE_NETHER.getLocation());
		
		if(calculated) {
			boolean wasInNether = ItemNBTHelper.getBoolean(stack, TAG_WAS_IN_NETHER, false);
			BlockPos pos = player.getPosition(); 
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

	@OnlyIn(Dist.CLIENT)
	public static class Impl implements IItemPropertyGetter {
		
		private final Angle normalAngle = new Angle();
		private final Angle unknownAngle = new Angle();
		
		@Override
		@OnlyIn(Dist.CLIENT)
		public float call(@Nonnull ItemStack stack, @Nullable ClientWorld worldIn, @Nullable LivingEntity entityIn) {
			if(entityIn == null && !stack.isOnItemFrame())
				return 0F;

			if(CompassesWorkEverywhereModule.enableCompassNerf && (!stack.hasTag() || !ItemNBTHelper.getBoolean(stack, TAG_CALCULATED, false)))
				return 0F;

			boolean carried = entityIn != null;
			Entity entity = carried ? entityIn : stack.getItemFrame();

			if (entity == null)
				return 0;

			if(worldIn == null && entity != null && entity.world instanceof ClientWorld)
				worldIn = (ClientWorld) entity.world;

			double angle;

			boolean calculate = false;
			BlockPos target = new BlockPos(0, 0, 0);

			ResourceLocation dimension = worldIn.getDimensionKey().getLocation();
			BlockPos lodestonePos = CompassItem.func_234670_d_(stack) ? this.getLodestonePosition(worldIn, stack.getOrCreateTag()) : null;
			
			if(lodestonePos != null) {
				calculate = true;
				target = lodestonePos;
			} else if(dimension.equals(Dimension.THE_END.getLocation()) && CompassesWorkEverywhereModule.enableEnd) 
				calculate = true;
			else if(dimension.equals(Dimension.THE_END.getLocation()) && isCalculated(stack) && CompassesWorkEverywhereModule.enableNether) {
				boolean set = ItemNBTHelper.getBoolean(stack, TAG_POSITION_SET, false);
				if(set) {
					int x = ItemNBTHelper.getInt(stack, TAG_NETHER_TARGET_X, 0);
					int z = ItemNBTHelper.getInt(stack, TAG_NETHER_TARGET_Z, 0);
					calculate = true;
					target = new BlockPos(x, 0, z);
				}
			} else if(worldIn.getDimensionType().isNatural()) {
				calculate = true;
				target = getWorldSpawn(worldIn);
			}

			long gameTime = worldIn.getGameTime();
			if(calculate && target != null) {
				double d1 = carried ? entity.rotationYaw : getFrameRotation((ItemFrameEntity)entity);
				d1 = MathHelper.positiveModulo(d1 / 360.0D, 1.0D);
				double d2 = getAngleToPosition(entity, target) / (Math.PI * 2D);

				if(carried) {
					if(normalAngle.needsUpdate(gameTime))
						normalAngle.wobble(gameTime, 0.5D - (d1 - 0.25D));
						angle = d2 + normalAngle.rotation;
				} else angle = 0.5D - (d1 - 0.25D - d2);
			} else {
				if(unknownAngle.needsUpdate(gameTime))
					unknownAngle.wobble(gameTime, Math.random());
					
				angle = unknownAngle.rotation + ((double) worldIn.hashCode() / Math.PI);
			}
			
			return MathHelper.positiveModulo((float) angle, 1.0F);
		}
		

		private double getFrameRotation(ItemFrameEntity frame) {
			return MathHelper.wrapDegrees(180 + frame.getHorizontalFacing().getHorizontalAngle());
		}

		private double getAngleToPosition(Entity entity, BlockPos blockpos) {
			Vector3d pos = entity.getPositionVec();
			return Math.atan2(blockpos.getZ() - pos.z, blockpos.getX() - pos.x);
		}

		// vanilla copy from here on out

		@Nullable 
		private BlockPos getLodestonePosition(World p_239442_1_, CompoundNBT p_239442_2_) {
			boolean flag = p_239442_2_.contains("LodestonePos");
			boolean flag1 = p_239442_2_.contains("LodestoneDimension");
			if (flag && flag1) {
				Optional<RegistryKey<World>> optional = CompassItem.func_234667_a_(p_239442_2_);
				if (optional.isPresent() && p_239442_1_.getDimensionKey().equals(optional.get())) {
					return NBTUtil.readBlockPos(p_239442_2_.getCompound("LodestonePos"));
				}
			}

			return null;
		}
		
		@Nullable
		private BlockPos getWorldSpawn(ClientWorld p_239444_1_) {
			return p_239444_1_.getDimensionType().isNatural() ? p_239444_1_.func_239140_u_() : null;
		}
	
		@OnlyIn(Dist.CLIENT)
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
				d0 = MathHelper.positiveModulo(d0 + 0.5D, 1.0D) - 0.5D;
				rota += d0 * 0.1D;
				rota *= 0.8D;
				rotation = MathHelper.positiveModulo(rotation + rota, 1.0D);
			}
		}
		
	}
	



}

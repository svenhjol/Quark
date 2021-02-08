package vazkii.quark.content.tweaks.client.item;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import vazkii.arl.util.ItemNBTHelper;

public class ClockTimeGetter {

	private static final String TAG_CALCULATED = "quark:clock_calculated";
	
	public static void tickClock(ItemStack stack) {
		boolean calculated = isCalculated(stack);
		if(!calculated)
			ItemNBTHelper.setBoolean(stack, TAG_CALCULATED, true);
	}

	static boolean isCalculated(ItemStack stack) {
		return stack.hasTag() && ItemNBTHelper.getBoolean(stack, TAG_CALCULATED, false);
	}
	
	@OnlyIn(Dist.CLIENT)
	public static class Impl implements IItemPropertyGetter {
		
		private double rotation;
		private double rota;
		private long lastUpdateTick;
		
		@Override
		@OnlyIn(Dist.CLIENT)
		public float call(@Nonnull ItemStack stack, @Nullable ClientWorld worldIn, @Nullable LivingEntity entityIn) {
			if(!isCalculated(stack))
				return 0F;
			
			boolean carried = entityIn != null;
			Entity entity = carried ? entityIn : stack.getItemFrame();

			if(worldIn == null && entity != null && entity.world instanceof ClientWorld)
				worldIn = (ClientWorld) entity.world;

			if(worldIn == null)
				return 0F;
			else {
				double angle;

				if (worldIn.getDimensionType().isNatural())
					angle = worldIn.func_242415_f(1F); // getCelestrialAngleByTime
				else
					angle = Math.random();

				angle = wobble(worldIn, angle);
				return (float) angle;
			}
		}
		
		private double wobble(World world, double time) {
			long gameTime = world.getGameTime();
			if(gameTime != lastUpdateTick) {
				lastUpdateTick = gameTime;
				double d0 = time - rotation;
				d0 = MathHelper.positiveModulo(d0 + 0.5D, 1.0D) - 0.5D;
				rota += d0 * 0.1D;
				rota *= 0.9D;
				rotation = MathHelper.positiveModulo(rotation + rota, 1.0D);
			}

			return rotation;
		}
		
	}

}

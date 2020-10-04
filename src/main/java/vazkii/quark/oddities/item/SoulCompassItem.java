package vazkii.quark.oddities.item;

import com.mojang.datafixers.util.Pair;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import vazkii.arl.util.ItemNBTHelper;
import vazkii.quark.base.item.QuarkItem;
import vazkii.quark.base.module.Module;
import vazkii.quark.oddities.module.TotemOfHoldingModule;

/**
 * @author WireSegal
 * Created at 1:25 PM on 3/30/20.
 */
public class SoulCompassItem extends QuarkItem {

    private static final String TAG_POS_X = "posX";
    private static final String TAG_DIMENSION_ID = "dimensionID";
    private static final String TAG_POS_Z = "posZ";

    @Environment(EnvType.CLIENT)
    private static double rotation, rota;

    @Environment(EnvType.CLIENT)
    private static long lastUpdateTick;

    public SoulCompassItem(Module module) {
        super("soul_compass", module, new Settings().group(ItemGroup.TOOLS).maxCount(1));
    }
    
    @Environment(EnvType.CLIENT)
    public static float angle(ItemStack stack, ClientWorld world, LivingEntity entityIn) {
        if(entityIn == null && !stack.isInFrame())
            return 0;

        else {
            boolean hasEntity = entityIn != null;
            Entity entity = (hasEntity ? entityIn : stack.getFrame());

            if (entity == null)
                return 0;

            if(world == null && entity != null && entity.world instanceof ClientWorld)
                world = (ClientWorld) entity.world;

            double angle;
            BlockPos pos = getPos(stack);

            if(getDim(stack).equals(world.getDimensionRegistryKey().getValue().toString())) { // getDimensionType().resourceLocation
                double yaw = hasEntity ? entity.yaw : getFrameRotation((ItemFrameEntity) entity);
                yaw = MathHelper.floorMod(yaw / 360.0, 1.0);
                double relAngle = getDeathToAngle(entity, pos) / (Math.PI * 2);
                angle = 0.5 - (yaw - 0.25 - relAngle);
            }
            else angle = Math.random();

            if (hasEntity)
                angle = wobble(world, angle);

            return MathHelper.floorMod((float) angle, 1.0F);
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
        if(!worldIn.isClient) {
            Pair<BlockPos, String> deathPos = TotemOfHoldingModule.getPlayerDeathPosition(entityIn);
            
            if(deathPos != null) {
            	ItemNBTHelper.setInt(stack, TAG_POS_X, deathPos.getFirst().getX());
                ItemNBTHelper.setInt(stack, TAG_POS_Z, deathPos.getFirst().getZ());
                ItemNBTHelper.setString(stack, TAG_DIMENSION_ID, deathPos.getSecond());
            }
        }
    }

    private static BlockPos getPos(ItemStack stack) {
        if(stack.hasTag()) {
            int x = ItemNBTHelper.getInt(stack, TAG_POS_X, 0);
            int y = -1;
            int z = ItemNBTHelper.getInt(stack, TAG_POS_Z, 0);

            return new BlockPos(x, y, z);
        }

        return new BlockPos(0, -1, 0);
    }
    
    private static String getDim(ItemStack stack) {
    	if(stack.hasTag())
    		return ItemNBTHelper.getString(stack, TAG_DIMENSION_ID, "");
    	
    	return "";
    }

    @Environment(EnvType.CLIENT)
    private static double wobble(World worldIn, double angle) {
        if(worldIn.getTime() != lastUpdateTick) {
            lastUpdateTick = worldIn.getTime();
            double relAngle = angle - rotation;
            relAngle = MathHelper.floorMod(relAngle + 0.5, 1.0) - 0.5;
            rota += relAngle * 0.1;
            rota *= 0.8;
            rotation = MathHelper.floorMod(rotation + rota, 1.0);
        }

        return rotation;
    }

    @Environment(EnvType.CLIENT)
    private static double getFrameRotation(ItemFrameEntity frame) {
        Direction facing = frame.getHorizontalFacing();
        return MathHelper.wrapDegrees(180 + facing.asRotation());
    }

    @Environment(EnvType.CLIENT)
    private static double getDeathToAngle(Entity entity, BlockPos blockpos) {
        return Math.atan2(blockpos.getZ() - entity.getZ(), blockpos.getX() - entity.getX());
    }


}

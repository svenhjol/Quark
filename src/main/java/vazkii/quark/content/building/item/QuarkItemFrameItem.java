package vazkii.quark.content.building.item;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.HangingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import vazkii.quark.base.item.QuarkItem;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.base.util.TriFunction;

import javax.annotation.Nonnull;

/**
 * @author WireSegal
 * Created at 11:04 AM on 8/25/19.
 */
public class QuarkItemFrameItem extends QuarkItem {
    private final TriFunction<? extends HangingEntity, World, BlockPos, Direction> entityProvider;

    public QuarkItemFrameItem(String name, QuarkModule module, TriFunction<? extends HangingEntity, World, BlockPos, Direction> entityProvider, Item.Properties properties) {
        super(name, module, properties);
        this.entityProvider = entityProvider;
    }

    @Nonnull
    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        BlockPos pos = context.getPos();
        Direction facing = context.getFace();
        BlockPos placeLocation = pos.offset(facing);
        PlayerEntity player = context.getPlayer();
        ItemStack stack = context.getItem();
        if (player != null && !this.canPlace(player, facing, stack, placeLocation)) {
            return ActionResultType.FAIL;
        } else {
            World world = context.getWorld();
            HangingEntity frame = entityProvider.apply(world, placeLocation, facing);

            CompoundNBT tag = stack.getTag();
            if (tag != null)
                EntityType.applyItemNBT(world, player, frame, tag);

            if (frame.onValidSurface()) {
                if (!world.isRemote) {
                    frame.playPlaceSound();
                    world.addEntity(frame);
                }

                stack.shrink(1);
            }

            return ActionResultType.SUCCESS;
        }
    }

    protected boolean canPlace(PlayerEntity player, Direction facing, ItemStack stack, BlockPos pos) {
        return !World.isOutsideBuildHeight(pos) && player.canPlayerEdit(pos, facing, stack);
    }
}

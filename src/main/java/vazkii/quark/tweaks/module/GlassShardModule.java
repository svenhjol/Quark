package vazkii.quark.tweaks.module;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.Material;
import net.minecraft.block.MaterialColor;
import net.minecraft.block.StainedGlassBlock;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.tag.ItemTags;
import net.minecraft.tag.Tag;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import vazkii.quark.base.Quark;
import vazkii.quark.base.block.QuarkBlock;
import vazkii.quark.base.block.QuarkInheritedPaneBlock;
import vazkii.quark.base.item.QuarkItem;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.Module;
import vazkii.quark.base.module.ModuleCategory;
import vazkii.quark.tweaks.block.DirtyGlassBlock;

/**
 * @author WireSegal
 * Created at 12:26 PM on 8/24/19.
 */
@LoadModule(category = ModuleCategory.TWEAKS, hasSubscriptions = true)
public class GlassShardModule extends Module {

    public static QuarkBlock dirtyGlass;

    public static Tag<Item> shardTag;

    public static Item clearShard;
    public static Item dirtyShard;

    private static final Map<DyeColor, Item> shardColors = new HashMap<>();

    @Override
    public void construct() {
        dirtyGlass = new DirtyGlassBlock("dirty_glass", this, ItemGroup.DECORATIONS,
                Block.Properties.of(Material.GLASS, MaterialColor.BROWN).strength(0.3F).sounds(BlockSoundGroup.GLASS));
        new QuarkInheritedPaneBlock(dirtyGlass);

        clearShard = new QuarkItem("clear_shard", this, new Item.Settings().group(ItemGroup.MATERIALS));
        dirtyShard = new QuarkItem("dirty_shard", this, new Item.Settings().group(ItemGroup.MATERIALS));

        for(DyeColor color : DyeColor.values())
            shardColors.put(color, new QuarkItem(color.asString() + "_shard", this, new Item.Settings().group(ItemGroup.MATERIALS)));
    }

    @Override
    public void setup() {
        shardTag = ItemTags.register(Quark.MOD_ID + ":shards");
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onDrops(BlockEvent.HarvestDropsEvent event) {
        Block block = event.getState().getBlock();
        if(event.getDrops() != null && event.getDrops().isEmpty() && !event.isSilkTouching()) {
            Item item = null;

            if (block == Blocks.GLASS)
                item = clearShard;
            else if (block == dirtyGlass)
                item = dirtyShard;
            else if (block instanceof StainedGlassBlock)
                item = shardColors.get(((StainedGlassBlock) block).getColor());

            if (item == null)
                return;

            Random rand = event.getWorld().getRandom();

            int quantity = MathHelper.clamp(2 + rand.nextInt(3) + rand.nextInt(event.getFortuneLevel() + 1), 1, 4);

            event.getDrops().add(new ItemStack(item, quantity));
        }
    }
}

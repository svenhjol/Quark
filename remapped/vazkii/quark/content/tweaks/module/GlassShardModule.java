package vazkii.quark.content.tweaks.module;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.block.MaterialColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.tag.ItemTags;
import net.minecraft.tag.Tag;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import vazkii.quark.base.Quark;
import vazkii.quark.base.block.QuarkBlock;
import vazkii.quark.base.block.QuarkInheritedPaneBlock;
import vazkii.quark.base.item.QuarkItem;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.content.tweaks.block.DirtyGlassBlock;
import vazkii.quark.base.module.ModuleCategory;

/**
 * @author WireSegal
 * Created at 12:26 PM on 8/24/19.
 */
@LoadModule(category = ModuleCategory.TWEAKS, hasSubscriptions = true)
public class GlassShardModule extends QuarkModule {

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
        shardTag = ItemTags.createOptional(new Identifier(Quark.MOD_ID, "shards"));
    }
}

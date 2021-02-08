package vazkii.quark.content.tools.module;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.item.DyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootEntry;
import net.minecraft.loot.LootTables;
import net.minecraft.loot.TagLootEntry;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import vazkii.arl.util.ItemNBTHelper;
import vazkii.quark.api.IRuneColorProvider;
import vazkii.quark.api.QuarkCapabilities;
import vazkii.quark.base.Quark;
import vazkii.quark.base.handler.MiscUtil;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.base.module.ModuleCategory;
import vazkii.quark.base.module.config.Config;
import vazkii.quark.content.tools.client.GlintRenderType;
import vazkii.quark.content.tools.item.RuneItem;

/**
 * @author WireSegal
 * Hacked by svenhjol
 * Created at 1:52 PM on 8/17/19.
 */
@LoadModule(category = ModuleCategory.TOOLS, hasSubscriptions = true)
public class ColorRunesModule extends QuarkModule {

    public static final String TAG_RUNE_ATTACHED = Quark.MOD_ID + ":RuneAttached";
    public static final String TAG_RUNE_COLOR = Quark.MOD_ID + ":RuneColor";

    private static final ThreadLocal<ItemStack> targetStack = new ThreadLocal<>();
    public static ITag<Item> runesTag, runesLootableTag;

    @Config public static int dungeonWeight = 10;
    @Config public static int netherFortressWeight = 8;
    @Config public static int jungleTempleWeight = 8;
    @Config public static int desertTempleWeight = 8;
    @Config public static int itemQuality = 0;
    @Config public static int applyCost = 15;

    public static void setTargetStack(ItemStack stack) {
        targetStack.set(stack);
    }

    public static int changeColor() {
        ItemStack target = targetStack.get();

        if (target == null)
            return -1;

        LazyOptional<IRuneColorProvider> cap = get(target);

        if (cap.isPresent())
            return cap.orElse((s) -> -1).getRuneColor(target);
        if (!ItemNBTHelper.getBoolean(target, TAG_RUNE_ATTACHED, false))
            return -1;

        ItemStack proxied = ItemStack.read(ItemNBTHelper.getCompound(target, TAG_RUNE_COLOR, false));
        LazyOptional<IRuneColorProvider> proxyCap = get(proxied);
        return proxyCap.orElse((s) -> -1).getRuneColor(target);
    }

    @OnlyIn(Dist.CLIENT)
    public static RenderType getGlint() {
        int color = changeColor();
        return color >= 0 && color <= 16 ? GlintRenderType.glintColor.get(color) : RenderType.getGlint();
    }

    @OnlyIn(Dist.CLIENT)
    public static RenderType getEntityGlint() {
        int color = changeColor();
        return color >= 0 && color <= 16 ? GlintRenderType.entityGlintColor.get(color) : RenderType.getEntityGlint();
    }
    
    @OnlyIn(Dist.CLIENT)
    public static RenderType getGlintDirect() {
        int color = changeColor();
        return color >= 0 && color <= 16 ? GlintRenderType.glintDirectColor.get(color) : RenderType.getGlintDirect();
    }

    @OnlyIn(Dist.CLIENT)
    public static RenderType getEntityGlintDirect() {
        int color = changeColor();
        return color >= 0 && color <= 16 ? GlintRenderType.entityGlintDirectColor.get(color) : RenderType.getEntityGlintDirect();
    }

    @OnlyIn(Dist.CLIENT)
    public static RenderType getArmorGlint() {
        int color = changeColor();
        return color >= 0 && color <= 16 ? GlintRenderType.armorGlintColor.get(color) : RenderType.getArmorGlint();
    }

    @OnlyIn(Dist.CLIENT)
    public static RenderType getArmorEntityGlint() {
        int color = changeColor();
        return color >= 0 && color <= 16 ? GlintRenderType.armorEntityGlintColor.get(color) : RenderType.getArmorEntityGlint();
    }
    
    @Override
    public void construct() {
        for(DyeColor color : DyeColor.values())
            new RuneItem(color.getString() + "_rune", this, color.getId());
        new RuneItem("rainbow_rune", this, 16);
    }

    @Override
    public void setup() {
        runesTag = ItemTags.createOptional(new ResourceLocation(Quark.MOD_ID, "runes"));
        runesLootableTag = ItemTags.createOptional(new ResourceLocation(Quark.MOD_ID, "runes_lootable"));
    }

    @SubscribeEvent
    public void onLootTableLoad(LootTableLoadEvent event) {
        int weight = 0;

        if(event.getName().equals(LootTables.CHESTS_SIMPLE_DUNGEON))
            weight = dungeonWeight;
        else if(event.getName().equals(LootTables.CHESTS_NETHER_BRIDGE))
            weight = netherFortressWeight;
        else if(event.getName().equals(LootTables.CHESTS_JUNGLE_TEMPLE))
            weight = jungleTempleWeight;
        else if(event.getName().equals(LootTables.CHESTS_DESERT_PYRAMID))
            weight = desertTempleWeight;

        if(weight > 0) {
            LootEntry entry = TagLootEntry.getBuilder(runesLootableTag)
                .weight(weight)
                .quality(itemQuality)
                .build();
            MiscUtil.addToLootTable(event.getTable(), entry);
        }
    }

    @SubscribeEvent
    public void onAnvilUpdate(AnvilUpdateEvent event) {
        ItemStack left = event.getLeft();
        ItemStack right = event.getRight();
        ItemStack output = event.getOutput();

        if(!left.isEmpty() && !right.isEmpty() && left.isEnchanted() && right.getItem().isIn(runesTag)) {
            ItemStack out = (output.isEmpty() ? left : output).copy();
            ItemNBTHelper.setBoolean(out, TAG_RUNE_ATTACHED, true);
            ItemNBTHelper.setCompound(out, TAG_RUNE_COLOR, right.serializeNBT());
            event.setOutput(out);
            event.setCost(applyCost);
            event.setMaterialCost(1);
        }
    }

    @SuppressWarnings("ConstantConditions")
    private static LazyOptional<IRuneColorProvider> get(ICapabilityProvider provider) {
        return provider.getCapability(QuarkCapabilities.RUNE_COLOR);
    }

}

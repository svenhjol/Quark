package vazkii.quark.content.client.module;

import com.google.common.collect.Lists;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.base.module.ModuleCategory;
import vazkii.quark.base.module.config.Config;
import vazkii.quark.content.client.tooltip.*;

import java.util.List;

/**
 * @author WireSegal
 * Created at 6:19 PM on 8/31/19.
 */
@LoadModule(category = ModuleCategory.CLIENT, hasSubscriptions = true, subscribeOn = Dist.CLIENT)
public class ImprovedTooltipsModule extends QuarkModule {

    @Config
    public static boolean attributeTooltips = true;
    @Config
    public static boolean foodTooltips = true;
    @Config
    public static boolean shulkerTooltips = true;
    @Config
    public static boolean mapTooltips = true;
    @Config
    public static boolean enchantingTooltips = true;

    @Config
    public static boolean shulkerBoxUseColors = true;
    @Config
    public static boolean shulkerBoxRequireShift = false;
    @Config
    public static boolean mapRequireShift = false;
    
	@Config 
	public static boolean showSaturation = true;

    @Config(description = "The value of each shank of food. " +
            "Tweak this when using mods like Hardcore Hunger which change that value.")
    public static int foodDivisor = 2;

    @Config
    public static List<String> enchantingStacks = Lists.newArrayList("minecraft:diamond_sword", "minecraft:diamond_pickaxe", "minecraft:diamond_shovel", "minecraft:diamond_axe", "minecraft:diamond_hoe",
            "minecraft:diamond_helmet", "minecraft:diamond_chestplate", "minecraft:diamond_leggings", "minecraft:diamond_boots",
            "minecraft:shears", "minecraft:bow", "minecraft:fishing_rod", "minecraft:crossbow", "minecraft:trident", "minecraft:elytra", "quark:pickarang");

    @Config(description = "A list of additional stacks to display on each enchantment\n"
            + "The format is as follows:\n"
            + "enchant_id=item1,item2,item3...\n"
            + "So to display a carrot on a stick on a mending book, for example, you use:\n"
            + "minecraft:mending=minecraft:carrot_on_a_stick")
    public static List<String> enchantingAdditionalStacks = Lists.newArrayList();

    @Override
    public void configChanged() {
        EnchantedBookTooltips.reloaded();
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void makeTooltip(ItemTooltipEvent event) {
        if (attributeTooltips)
            AttributeTooltips.makeTooltip(event);
        if (foodTooltips)
            FoodTooltips.makeTooltip(event);
        if (shulkerTooltips)
            ShulkerBoxTooltips.makeTooltip(event);
        if (mapTooltips)
            MapTooltips.makeTooltip(event);
        if (enchantingTooltips)
            EnchantedBookTooltips.makeTooltip(event);
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void renderTooltip(RenderTooltipEvent.PostText event) {
        if (attributeTooltips)
            AttributeTooltips.renderTooltip(event);
        if (foodTooltips)
            FoodTooltips.renderTooltip(event);
        if (shulkerTooltips)
            ShulkerBoxTooltips.renderTooltip(event);
        if (mapTooltips)
            MapTooltips.renderTooltip(event);
        if (enchantingTooltips)
            EnchantedBookTooltips.renderTooltip(event);
    }
}

package vazkii.quark.content.tweaks.module;

import net.minecraft.block.ComposterBlock;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraftforge.event.furnace.FurnaceFuelBurnTimeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.base.module.ModuleCategory;
import vazkii.quark.base.module.config.Config;

/**
 * @author WireSegal
 * Created at 7:34 PM on 9/28/19.
 */
@LoadModule(category = ModuleCategory.TWEAKS, hasSubscriptions = true)
public class UtilityRecipesModule extends QuarkModule {

    @Config(description = "Can any wool color be dyed?", flag = "dye_any_wool")
    public static boolean dyeAnyWool = true;

    @Config(description = "Can other stone-like materials be used for crafting stone tools?", flag = "better_stone_tools")
    public static boolean betterStoneToolCrafting = true;

    @Config(description = "Can a dispenser be crafted by adding a bow to a dropper?", flag = "dropper_upgrade")
    public static boolean enableDispenser = true;

    @Config(description = "Can a repeater be crafted with the pattern for a redstone torch?", flag = "repeater_and_torches")
    public static boolean enableRepeater = true;

    @Config(description = "Can you craft a minecart around blocks which can be placed inside?", flag = "minecart_upgrade")
    public static boolean enableMinecarts = true;

    @Config(description = "Can you craft four chests at once using logs?", flag = "wood_to_chest_recipes")
    public static boolean logsToChests = true;

    @Config(description = "Can Coral be crafted into dye?", flag = "coral_to_dye")
    public static boolean coralToDye = true;

    // Credit to /u/InfiniteNexus for the idea! https://reddit.com/d5k1s9/
    @Config(description = "Can cookies, paper, and bread be crafted in a 2x2 crafting table?", flag = "bent_recipes")
    public static boolean bentRecipes = true;

    @Config(description = "Can Rotten Flesh and Poisonous Potatoes be composted?")
    public static boolean compostableToxins = true;

    @Config(description = "Does Dragon Breath return a bottle when used as a reagent or material?")
    public static boolean effectiveDragonBreath = true;

    @Config(description = "Can torches can be used as fuel in furnaces?")
    public static boolean torchesBurn = true;
    
    @Config(description = "Can bones be smelted down to bone meal?", flag = "bone_meal_utility")
    public static boolean boneMealUtility = true;

    @Override
    public void configChanged() {
        if (effectiveDragonBreath)
            Items.DRAGON_BREATH.containerItem = null;
        else
            Items.DRAGON_BREATH.containerItem = Items.GLASS_BOTTLE;

        if (compostableToxins) {
            ComposterBlock.CHANCES.put(Items.POISONOUS_POTATO, 0.85F);
            ComposterBlock.CHANCES.put(Items.ROTTEN_FLESH, 0.3F);
        } else {
            ComposterBlock.CHANCES.removeFloat(Items.POISONOUS_POTATO);
            ComposterBlock.CHANCES.removeFloat(Items.ROTTEN_FLESH);
        }
    }

    @SubscribeEvent
    public void torchBurnTime(FurnaceFuelBurnTimeEvent event) {
        if (torchesBurn) {
            Item item = event.getItemStack().getItem();
            if (item == Items.TORCH)
                event.setBurnTime(400);
        }
    }

}

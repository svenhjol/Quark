package vazkii.quark.content.tweaks.module;

import net.minecraft.entity.ai.goal.TemptGoal;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.base.module.ModuleCategory;

/**
 * @author WireSegal
 * Created at 11:25 AM on 9/2/19.
 */
@LoadModule(category = ModuleCategory.TWEAKS, hasSubscriptions = true)
public class VillagersFollowEmeraldsModule extends QuarkModule {

    @SubscribeEvent
    public void onVillagerAppear(EntityJoinWorldEvent event) {
        if(event.getEntity() instanceof VillagerEntity) {
            VillagerEntity villager = (VillagerEntity) event.getEntity();
            boolean alreadySetUp = villager.goalSelector.goals.stream().anyMatch((goal) -> goal.getGoal() instanceof TemptGoal);

            if (!alreadySetUp)
                villager.goalSelector.add(2, new TemptGoal(villager, 0.6, Ingredient.ofItems(Items.EMERALD_BLOCK), false));
        }
    }
}

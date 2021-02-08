package vazkii.quark.content.automation.module;

import net.minecraft.entity.ai.goal.TemptGoal;
import net.minecraft.entity.monster.EndermiteEntity;
import net.minecraftforge.event.entity.EntityEvent.EnteringChunk;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.base.module.ModuleCategory;
import vazkii.quark.base.module.config.Config;
import vazkii.quark.content.automation.ai.FormShulkerGoal;

@LoadModule(category = ModuleCategory.AUTOMATION, hasSubscriptions = true)
public class EndermitesFormShulkersModule extends QuarkModule {

	@Config public static double chance = 0.005;
	
	@SubscribeEvent
	public void onEnterChunk(EnteringChunk event) {
		if(event.getEntity() instanceof EndermiteEntity) {
			EndermiteEntity endermite = (EndermiteEntity) event.getEntity();
            boolean alreadySetUp = endermite.goalSelector.goals.stream().anyMatch((goal) -> goal.getGoal() instanceof TemptGoal);

            if(!alreadySetUp) 
            	endermite.goalSelector.addGoal(2, new FormShulkerGoal(endermite));
		}
	}
	
}

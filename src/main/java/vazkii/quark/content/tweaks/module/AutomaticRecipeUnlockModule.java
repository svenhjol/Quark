package vazkii.quark.content.tweaks.module;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;

import com.google.common.collect.Lists;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.recipebook.IRecipeShownListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.toasts.IToast;
import net.minecraft.client.gui.toasts.RecipeToast;
import net.minecraft.client.gui.toasts.ToastGui;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.ImageButton;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.RecipeBookCategory;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.GameRules;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.GuiScreenEvent.InitGuiEvent;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.base.module.ModuleCategory;
import vazkii.quark.base.module.config.Config;

@LoadModule(category = ModuleCategory.TWEAKS, hasSubscriptions = true)
public class AutomaticRecipeUnlockModule extends QuarkModule {

	@Config(description = "A list of recipe names that should NOT be added in by default")
	public static List<String> ignoredRecipes = Lists.newArrayList();

	@Config public static boolean forceLimitedCrafting = false;	
	@Config public static boolean disableRecipeBook = false;

	@SubscribeEvent 
	public void onPlayerLoggedIn(PlayerLoggedInEvent event) {
		PlayerEntity player = event.getPlayer();

		if(player instanceof ServerPlayerEntity) {
			ServerPlayerEntity spe = (ServerPlayerEntity) player;
			MinecraftServer server = spe.getServer();
			if (server != null) {
				List<IRecipe<?>> recipes = new ArrayList<>(server.getRecipeManager().getRecipes());
				recipes.removeIf((recipe) -> ignoredRecipes.contains(Objects.toString(recipe.getId())) || recipe.getRecipeOutput().isEmpty());
				player.unlockRecipes(recipes);

				if (forceLimitedCrafting)
					player.world.getGameRules().get(GameRules.DO_LIMITED_CRAFTING).set(true, server);
			}
		}
	}

	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public void onInitGui(InitGuiEvent.Post event) {
		Screen gui = event.getGui();
		if(disableRecipeBook && gui instanceof IRecipeShownListener) {
			Minecraft.getInstance().player.getRecipeBook().func_242139_a().func_242152_a(RecipeBookCategory.CRAFTING, false); 

			List<Widget> widgets = event.getWidgetList();
			for(Widget w : widgets)
				if(w instanceof ImageButton) {
					event.removeWidget(w);
					return;
				}
		}
	}

	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public void clientTick(ClientTickEvent event) {
		Minecraft mc = Minecraft.getInstance();
		if(mc.player != null && mc.player.ticksExisted < 20) {
			ToastGui toasts = mc.getToastGui();
			Queue<IToast> toastQueue = toasts.toastsQueue;
			for(IToast toast : toastQueue)
				if(toast instanceof RecipeToast) {
					RecipeToast recipeToast = (RecipeToast) toast;
					List<IRecipe<?>> stacks = recipeToast.recipes;
					if(stacks.size() > 100) {
						toastQueue.remove(toast);
						return;
					}
				}
		}
	}

}

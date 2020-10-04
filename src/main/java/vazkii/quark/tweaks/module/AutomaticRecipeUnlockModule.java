package vazkii.quark.tweaks.module;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;

import com.google.common.collect.Lists;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.recipebook.RecipeBookProvider;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.toast.RecipeToast;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.recipe.Recipe;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameRules;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.GuiScreenEvent.InitGuiEvent;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.Module;
import vazkii.quark.base.module.ModuleCategory;
import vazkii.quark.base.module.config.Config;

@LoadModule(category = ModuleCategory.TWEAKS, hasSubscriptions = true)
public class AutomaticRecipeUnlockModule extends Module {

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
				List<Recipe<?>> recipes = new ArrayList<>(server.getRecipeManager().values());
				recipes.removeIf((recipe) -> ignoredRecipes.contains(Objects.toString(recipe.getId())) || recipe.getOutput().isEmpty());
				player.unlockRecipes(recipes);

				if (forceLimitedCrafting)
					player.world.getGameRules().get(GameRules.DO_LIMITED_CRAFTING).set(true, server);
			}
		}
	}

	@SubscribeEvent
	@Environment(EnvType.CLIENT)
	public void onInitGui(InitGuiEvent.Post event) {
		Screen gui = event.getGui();
		if(disableRecipeBook && gui instanceof RecipeBookProvider) {
			MinecraftClient.getInstance().player.getRecipeBook().setGuiOpen(false);

			List<AbstractButtonWidget> widgets = event.getWidgetList();
			for(AbstractButtonWidget w : widgets)
				if(w instanceof TexturedButtonWidget) {
					event.removeWidget(w);
					return;
				}
		}
	}

	@SubscribeEvent
	@Environment(EnvType.CLIENT)
	public void clientTick(ClientTickEvent event) {
		MinecraftClient mc = MinecraftClient.getInstance();
		if(mc.player != null && mc.player.age < 20) {
			ToastManager toasts = mc.getToastManager();
			Queue<Toast> toastQueue = toasts.toastQueue;
			for(Toast toast : toastQueue)
				if(toast instanceof RecipeToast) {
					RecipeToast recipeToast = (RecipeToast) toast;
					List<Recipe<?>> stacks = recipeToast.recipes;
					if(stacks.size() > 100) {
						toastQueue.remove(toast);
						return;
					}
				}
		}
	}

}

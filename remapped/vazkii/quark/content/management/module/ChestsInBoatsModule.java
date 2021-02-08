package vazkii.quark.content.management.module;

import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tag.ItemTags;
import net.minecraft.tag.Tag;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import vazkii.arl.util.RegistryHelper;
import vazkii.quark.base.Quark;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.base.module.ModuleCategory;
import vazkii.quark.base.network.QuarkNetwork;
import vazkii.quark.base.network.message.OpenBoatChestMessage;
import vazkii.quark.content.management.client.render.ChestPassengerRenderer;
import vazkii.quark.content.management.entity.ChestPassengerEntity;

@LoadModule(category = ModuleCategory.MANAGEMENT, hasSubscriptions = true)
public class ChestsInBoatsModule extends QuarkModule {

	public static EntityType<ChestPassengerEntity> chestPassengerEntityType;

	private static Tag<Item> boatableChestsTag;
	
	@Override
	public void construct() {
		chestPassengerEntityType = EntityType.Builder.<ChestPassengerEntity>create(ChestPassengerEntity::new, SpawnGroup.MISC)
				.setDimensions(0.8F, 0.8F)
				.setTrackingRange(64)
				.setUpdateInterval(128)
				.setCustomClientFactory((spawnEntity, world) -> new ChestPassengerEntity(chestPassengerEntityType, world))
				.build("chest_passenger");
		RegistryHelper.register(chestPassengerEntityType, "chest_passenger");
	}
	
    @Override
    public void setup() {
    	boatableChestsTag = ItemTags.createOptional(new Identifier(Quark.MOD_ID, "boatable_chests"));
    }

	@Override
	@Environment(EnvType.CLIENT)
	public void clientSetup() {
		RenderingRegistry.registerEntityRenderingHandler(chestPassengerEntityType, ChestPassengerRenderer::new);
	}

	@SubscribeEvent
	public void onEntityInteract(PlayerInteractEvent.EntityInteractSpecific event) {
		Entity target = event.getTarget();
		PlayerEntity player = event.getPlayer();

		if(target instanceof BoatEntity && target.getPassengerList().isEmpty()) {
			Hand hand = Hand.MAIN_HAND;
			ItemStack stack = player.getMainHandStack();
			if(!isChest(stack)) {
				stack = player.getOffHandStack();
				hand = Hand.OFF_HAND;
			}

			if(isChest(stack)) {
				World world = event.getWorld();
				
				if(!event.getWorld().isClient) {
					ItemStack chestStack = stack.copy();
					chestStack.setCount(1);
					if (!player.isCreative())
						stack.decrement(1);

					ChestPassengerEntity passenger = new ChestPassengerEntity(world, chestStack);
					Vec3d pos = target.getPos();
					passenger.updatePosition(pos.x, pos.y, pos.z);
					passenger.yaw = target.yaw;
					passenger.startRiding(target, true);
					world.spawnEntity(passenger);
				}
				
				player.swingHand(hand);
				event.setCancellationResult(ActionResult.SUCCESS);
				event.setCanceled(true);
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	@Environment(EnvType.CLIENT)
	public void onOpenGUI(GuiOpenEvent event) {
		PlayerEntity player = MinecraftClient.getInstance().player;
		if(player != null && event.getGui() instanceof InventoryScreen && player.hasVehicle()) {
			Entity riding = player.getVehicle();
			if(riding instanceof BoatEntity) {
				List<Entity> passengers = riding.getPassengerList();
				for(Entity passenger : passengers)
					if(passenger instanceof ChestPassengerEntity) {
						QuarkNetwork.sendToServer(new OpenBoatChestMessage());
						event.setCanceled(true);
						return;
					}
			}
		}
	}
	
	private boolean isChest(ItemStack stack) {
		return !stack.isEmpty() && stack.getItem().isIn(boatableChestsTag);
	}
}

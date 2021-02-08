package vazkii.quark.content.management.module;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
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

	private static ITag<Item> boatableChestsTag;
	
	@Override
	public void construct() {
		chestPassengerEntityType = EntityType.Builder.<ChestPassengerEntity>create(ChestPassengerEntity::new, EntityClassification.MISC)
				.size(0.8F, 0.8F)
				.setTrackingRange(64)
				.setUpdateInterval(128)
				.setCustomClientFactory((spawnEntity, world) -> new ChestPassengerEntity(chestPassengerEntityType, world))
				.build("chest_passenger");
		RegistryHelper.register(chestPassengerEntityType, "chest_passenger");
	}
	
    @Override
    public void setup() {
    	boatableChestsTag = ItemTags.createOptional(new ResourceLocation(Quark.MOD_ID, "boatable_chests"));
    }

	@Override
	@OnlyIn(Dist.CLIENT)
	public void clientSetup() {
		RenderingRegistry.registerEntityRenderingHandler(chestPassengerEntityType, ChestPassengerRenderer::new);
	}

	@SubscribeEvent
	public void onEntityInteract(PlayerInteractEvent.EntityInteractSpecific event) {
		Entity target = event.getTarget();
		PlayerEntity player = event.getPlayer();

		if(target instanceof BoatEntity && target.getPassengers().isEmpty()) {
			Hand hand = Hand.MAIN_HAND;
			ItemStack stack = player.getHeldItemMainhand();
			if(!isChest(stack)) {
				stack = player.getHeldItemOffhand();
				hand = Hand.OFF_HAND;
			}

			if(isChest(stack)) {
				World world = event.getWorld();
				
				if(!event.getWorld().isRemote) {
					ItemStack chestStack = stack.copy();
					chestStack.setCount(1);
					if (!player.isCreative())
						stack.shrink(1);

					ChestPassengerEntity passenger = new ChestPassengerEntity(world, chestStack);
					Vector3d pos = target.getPositionVec();
					passenger.setPosition(pos.x, pos.y, pos.z);
					passenger.rotationYaw = target.rotationYaw;
					passenger.startRiding(target, true);
					world.addEntity(passenger);
				}
				
				player.swingArm(hand);
				event.setCancellationResult(ActionResultType.SUCCESS);
				event.setCanceled(true);
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	@OnlyIn(Dist.CLIENT)
	public void onOpenGUI(GuiOpenEvent event) {
		PlayerEntity player = Minecraft.getInstance().player;
		if(player != null && event.getGui() instanceof InventoryScreen && player.isPassenger()) {
			Entity riding = player.getRidingEntity();
			if(riding instanceof BoatEntity) {
				List<Entity> passengers = riding.getPassengers();
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

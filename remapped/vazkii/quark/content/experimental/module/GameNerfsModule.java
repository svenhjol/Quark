package vazkii.quark.content.experimental.module;

import java.util.Map;
import java.util.UUID;

import com.mojang.serialization.Dynamic;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.mob.ZombieVillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.tag.ItemTags;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.village.VillageGossipType;
import net.minecraft.village.VillagerGossips;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerXpEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.ModuleCategory;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.base.module.config.Config;

@LoadModule(category = ModuleCategory.EXPERIMENTAL, enabledByDefault = false, hasSubscriptions = true)
public class GameNerfsModule extends QuarkModule {
	
	private static final String TAG_TRADES_ADJUSTED = "quark:zombie_trades_adjusted";
	
	@Config(description = "Makes Mending act like the Unmending mod\n"
			+ "https://www.curseforge.com/minecraft/mc-mods/unmending")
	public static boolean nerfMending = true;
	
	@Config(description = "Resets all villager discounts when zombified to prevent reducing prices to ridiculous levels") 
	public static boolean nerfVillagerDiscount = true;

	@Config(description = "Makes Iron Golems not drop Iron Ingots") 
	public static boolean disableIronFarms = true;
	
	@Config(description = "Makes Boats not glide on ice") 
	public static boolean disableIceRoads = true;
	
	@Config(description = "Makes Sheep not drop Wool when killed") 
	public static boolean disableWoolDrops = true;
	
	private static boolean staticEnabled;
	
	@Override
	public void configChanged() {
		staticEnabled = enabled;
	}
	
	// Source for this magic number is the ice-boat-nerf mod 
	// https://gitlab.com/supersaiyansubtlety/ice_boat_nerf/-/blob/master/src/main/java/net/sssubtlety/ice_boat_nerf/mixin/BoatEntityMixin.java
	public static float getBoatGlide(float glide) {
		return (staticEnabled && disableIceRoads) ? 0.45F : glide;
	}
	
	// stolen from King Lemming thanks mate
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void killMending(PlayerXpEvent.PickupXp event) {
		if(!nerfMending)
			return;
		
		PlayerEntity player = event.getPlayer();
		ExperienceOrbEntity orb = event.getOrb();

		player.experiencePickUpDelay = 2;
		player.sendPickup(orb, 1);
		if(orb.amount > 0)
			player.addExperience(orb.amount);

		orb.remove();
		event.setCanceled(true);
	}

	@SubscribeEvent
	public void onAnvilUpdate(AnvilUpdateEvent event) {
		if(!nerfMending)
			return;
		
		ItemStack left = event.getLeft();
		ItemStack right = event.getRight();
		ItemStack out = event.getOutput();

		if(out.isEmpty() && (left.isEmpty() || right.isEmpty()))
			return;

		boolean isMended = false;

		Map<Enchantment, Integer> enchLeft = EnchantmentHelper.get(left);
		Map<Enchantment, Integer> enchRight = EnchantmentHelper.get(right);

		if(enchLeft.containsKey(Enchantments.MENDING) || enchRight.containsKey(Enchantments.MENDING)) {
			if(left.getItem() == right.getItem())
				isMended = true;

			if(right.getItem() == Items.ENCHANTED_BOOK)
				isMended = true;
		}

		if(isMended) {
			if(out.isEmpty())
				out = left.copy();

			if(!out.hasTag())
				out.setTag(new CompoundTag());

			Map<Enchantment, Integer> enchOutput = EnchantmentHelper.get(out);
			enchOutput.putAll(enchRight);
			enchOutput.remove(Enchantments.MENDING);
			
			EnchantmentHelper.set(enchOutput, out);

			out.setRepairCost(0);
			if(out.isDamageable())
				out.setDamage(0);

			event.setOutput(out);
			if(event.getCost() == 0)
				event.setCost(1);
		}
	}

	@SubscribeEvent
	@Environment(EnvType.CLIENT)
	public void onTooltip(ItemTooltipEvent event) {
		if(!nerfMending)
			return;
		
		Text itemgotmodified = new TranslatableText("quark.misc.repaired").formatted(Formatting.YELLOW);
		int repairCost = event.getItemStack().getRepairCost();
		if(repairCost > 0)
			event.getToolTip().add(itemgotmodified);
	}
	
	@SubscribeEvent
	public void onTick(LivingUpdateEvent event) {
		if(nerfVillagerDiscount && event.getEntity().getType() == EntityType.ZOMBIE_VILLAGER && !event.getEntity().getPersistentData().contains(TAG_TRADES_ADJUSTED)) {
			ZombieVillagerEntity zombie = (ZombieVillagerEntity) event.getEntity();
			
			Tag gossipsNbt = zombie.gossipData;
			
			VillagerGossips manager = new VillagerGossips();
			manager.deserialize(new Dynamic<>(NbtOps.INSTANCE, gossipsNbt));
			
			for(UUID uuid : manager.entityReputation.keySet()) {
				VillagerGossips.Reputation gossips = manager.entityReputation.get(uuid);
				gossips.remove(VillageGossipType.MAJOR_POSITIVE);
				gossips.remove(VillageGossipType.MINOR_POSITIVE);
			}
			
			zombie.getPersistentData().putBoolean(TAG_TRADES_ADJUSTED, true);
		}
	}

	@SubscribeEvent
	public void onLoot(LivingDropsEvent event) {
		if(disableIronFarms && event.getEntity().getType() == EntityType.IRON_GOLEM)
			event.getDrops().removeIf(e -> e.getStack().getItem() == Items.IRON_INGOT);
		
		if(disableWoolDrops && event.getEntity().getType() == EntityType.SHEEP)
			event.getDrops().removeIf(e -> e.getStack().getItem().isIn(ItemTags.WOOL));
	}
	
}

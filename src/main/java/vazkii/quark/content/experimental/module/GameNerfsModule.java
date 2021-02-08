package vazkii.quark.content.experimental.module;

import java.util.Map;
import java.util.UUID;

import com.mojang.serialization.Dynamic;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.entity.monster.ZombieVillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.village.GossipManager;
import net.minecraft.village.GossipType;
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

		player.xpCooldown = 2;
		player.onItemPickup(orb, 1);
		if(orb.xpValue > 0)
			player.giveExperiencePoints(orb.xpValue);

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

		Map<Enchantment, Integer> enchLeft = EnchantmentHelper.getEnchantments(left);
		Map<Enchantment, Integer> enchRight = EnchantmentHelper.getEnchantments(right);

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
				out.setTag(new CompoundNBT());

			Map<Enchantment, Integer> enchOutput = EnchantmentHelper.getEnchantments(out);
			enchOutput.putAll(enchRight);
			enchOutput.remove(Enchantments.MENDING);
			
			EnchantmentHelper.setEnchantments(enchOutput, out);

			out.setRepairCost(0);
			if(out.isDamageable())
				out.setDamage(0);

			event.setOutput(out);
			if(event.getCost() == 0)
				event.setCost(1);
		}
	}

	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public void onTooltip(ItemTooltipEvent event) {
		if(!nerfMending)
			return;
		
		ITextComponent itemgotmodified = new TranslationTextComponent("quark.misc.repaired").mergeStyle(TextFormatting.YELLOW);
		int repairCost = event.getItemStack().getRepairCost();
		if(repairCost > 0)
			event.getToolTip().add(itemgotmodified);
	}
	
	@SubscribeEvent
	public void onTick(LivingUpdateEvent event) {
		if(nerfVillagerDiscount && event.getEntity().getType() == EntityType.ZOMBIE_VILLAGER && !event.getEntity().getPersistentData().contains(TAG_TRADES_ADJUSTED)) {
			ZombieVillagerEntity zombie = (ZombieVillagerEntity) event.getEntity();
			
			INBT gossipsNbt = zombie.gossips;
			
			GossipManager manager = new GossipManager();
			manager.read(new Dynamic<>(NBTDynamicOps.INSTANCE, gossipsNbt));
			
			for(UUID uuid : manager.uuid_gossips_mapping.keySet()) {
				GossipManager.Gossips gossips = manager.uuid_gossips_mapping.get(uuid);
				gossips.removeGossipType(GossipType.MAJOR_POSITIVE);
				gossips.removeGossipType(GossipType.MINOR_POSITIVE);
			}
			
			zombie.getPersistentData().putBoolean(TAG_TRADES_ADJUSTED, true);
		}
	}

	@SubscribeEvent
	public void onLoot(LivingDropsEvent event) {
		if(disableIronFarms && event.getEntity().getType() == EntityType.IRON_GOLEM)
			event.getDrops().removeIf(e -> e.getItem().getItem() == Items.IRON_INGOT);
		
		if(disableWoolDrops && event.getEntity().getType() == EntityType.SHEEP)
			event.getDrops().removeIf(e -> e.getItem().getItem().isIn(ItemTags.WOOL));
	}
	
}

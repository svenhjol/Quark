package vazkii.quark.content.tools.module;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.DamageSource;
import net.minecraft.util.IndirectEntityDamageSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import vazkii.arl.util.RegistryHelper;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.base.module.ModuleCategory;
import vazkii.quark.base.module.config.Config;
import vazkii.quark.content.tools.client.render.PickarangRenderer;
import vazkii.quark.content.tools.entity.PickarangEntity;
import vazkii.quark.content.tools.item.PickarangItem;

@LoadModule(category = ModuleCategory.TOOLS, hasSubscriptions = true)
public class PickarangModule extends QuarkModule {
	
	public static EntityType<PickarangEntity> pickarangType;

	@Config(description = "How long it takes before the pickarang starts returning to the player if it doesn't hit anything.")
	public static int timeout = 20;
	@Config(description = "2 is Iron, 3 is Diamond.")
	public static int harvestLevel = 3;
	@Config(description = "2 is Iron, 4 is Diamond.")
	public static int netheriteHarvestLevel = 3;
	
	@Config(description = "Set to -1 to have the Pickarang be unbreakable.")
	public static int durability = 800;
	
	@Config(description = "Set to -1 to have the Pickarang be unbreakable.")
	public static int netheriteDurability = 1040;
	
	@Config(description = "22.5 is ender chests, 25.0 is monster boxes, 50 is obsidian. Most things are below 5.")
	public static double maxHardness = 20.0;
	
	@Config(description = "Set this to true to use the recipe without the Heart of Diamond, even if the Heart of Diamond is enabled.", flag = "pickarang_never_uses_heart")
	public static boolean neverUseHeartOfDiamond = false;
	@Config(description = "Set this to true to disable the short cooldown between throwing pickarangs.")
	public static boolean noCooldown = false;
	
	public static Item pickarang;
	public static Item flamerang;
	
	private static boolean isEnabled;

	@Override
	public void construct() {
		pickarangType = EntityType.Builder.<PickarangEntity>create(PickarangEntity::new, EntityClassification.MISC)
				.size(0.4F, 0.4F)
				.setTrackingRange(80)
				.setUpdateInterval(3)
				.setShouldReceiveVelocityUpdates(true)
				.setCustomClientFactory((spawnEntity, world) -> new PickarangEntity(pickarangType, world))
				.build("pickarang");
		RegistryHelper.register(pickarangType, "pickarang");

		pickarang = new PickarangItem("pickarang", this, propertiesFor(harvestLevel, durability, false), false);
		flamerang = new PickarangItem("flamerang", this, propertiesFor(netheriteHarvestLevel, netheriteDurability, true), true);
	}
	
	private static Item.Properties propertiesFor(int level, int durability, boolean netherite) {
		Item.Properties properties = new Item.Properties()
				.maxStackSize(1)
				.group(ItemGroup.TOOLS)
				.addToolType(ToolType.PICKAXE, harvestLevel)
				.addToolType(ToolType.AXE, harvestLevel)
				.addToolType(ToolType.SHOVEL, harvestLevel);

		if (durability > 0)
			properties.maxDamage(durability);
		
		if(netherite)
			properties.isImmuneToFire();
		
		return properties;
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public void clientSetup() {
		RenderingRegistry.registerEntityRenderingHandler(pickarangType, PickarangRenderer::new);
	}
	
	@Override
	public void configChanged() {
		// Pass over to a static reference for easier computing the coremod hook
		isEnabled = this.enabled;
	}
	
    private static final ThreadLocal<PickarangEntity> ACTIVE_PICKARANG = new ThreadLocal<>();

	public static void setActivePickarang(PickarangEntity pickarang) {
		ACTIVE_PICKARANG.set(pickarang);
	}

	public static DamageSource createDamageSource(PlayerEntity player) {
		PickarangEntity pickarang = ACTIVE_PICKARANG.get();

		if (pickarang == null)
			return null;

		return new IndirectEntityDamageSource("player", pickarang, player).setProjectile();
	}
	
	public static boolean getIsFireResistant(boolean vanillaVal, Entity entity) {
		if(!isEnabled || vanillaVal)
			return vanillaVal;
		
		Entity riding = entity.getRidingEntity();
		if(riding instanceof PickarangEntity)
			return ((PickarangEntity) riding).netherite;
		
		return false;
	}

}

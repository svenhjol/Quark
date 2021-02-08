package vazkii.quark.content.world.module.underground;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.block.MaterialColor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.attribute.DefaultAttributeRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.item.ItemGroup;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import vazkii.arl.util.RegistryHelper;
import vazkii.quark.base.block.QuarkBlock;
import vazkii.quark.base.handler.VariantHandler;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.ModuleCategory;
import vazkii.quark.base.module.config.Config;
import vazkii.quark.base.world.EntitySpawnHandler;
import vazkii.quark.content.world.client.render.WrappedRenderer;
import vazkii.quark.content.world.config.UndergroundBiomeConfig;
import vazkii.quark.content.world.entity.WrappedEntity;
import vazkii.quark.content.world.gen.underground.SpiderNestUndergroundBiome;

@LoadModule(category = ModuleCategory.WORLD, hasSubscriptions = true)
public class SpiderNestUndergroundBiomeModule extends UndergroundBiomeModule {

	public static QuarkBlock cobbedstone;
	public static EntityType<WrappedEntity> wrappedType;

	@Config public static boolean enabledWrapped = true;

	@Override
	public void construct() {
		cobbedstone = new QuarkBlock("cobbedstone", this, ItemGroup.BUILDING_BLOCKS, 
				Block.Properties.of(Material.STONE, MaterialColor.GRAY)
				.requiresTool() // needs tool
        		.harvestTool(ToolType.PICKAXE)
				.strength(1.5F, 10F)
				.sounds(BlockSoundGroup.STONE));

		VariantHandler.addSlabStairsWall(cobbedstone);

		wrappedType = EntityType.Builder.create(WrappedEntity::new, SpawnGroup.MONSTER)
				.setDimensions(0.6F, 1.95F)
				.setTrackingRange(80)
				.setUpdateInterval(3)
				.setCustomClientFactory((spawnEntity, world) -> new WrappedEntity(wrappedType, world))
				.build("wrapped");
		RegistryHelper.register(wrappedType, "wrapped");
        EntitySpawnHandler.addEgg(wrappedType, 0x246565, 0x9f978b, this, () -> enabledWrapped);

		super.construct();
	}
	
	@Override
	public void setup() {
		super.setup();
		
		DefaultAttributeRegistry.put(wrappedType, ZombieEntity.createZombieAttributes().build());
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void clientSetup() {
		RenderingRegistry.registerEntityRenderingHandler(wrappedType, WrappedRenderer::new);
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onZombieSpawn(LivingSpawnEvent.CheckSpawn event) {
		LivingEntity entity = event.getEntityLiving();
		Result result = event.getResult();
		if(entity.getType() == EntityType.ZOMBIE && entity instanceof MobEntity && enabledWrapped && result != Result.DENY) {
			MobEntity mob = (MobEntity) entity;
			 
			if(result == Result.ALLOW || (mob.canSpawn(entity.world, event.getSpawnReason()) && mob.canSpawn(entity.world)))
				if(changeToWrapped(entity))
					event.setResult(Result.DENY);
		}
	}
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onZombieSpecialSpawn(LivingSpawnEvent.SpecialSpawn event) {
		LivingEntity entity = event.getEntityLiving();
		if(entity.getType() == EntityType.ZOMBIE && enabledWrapped)
			if(changeToWrapped(entity))
				event.setCanceled(true);
	}
	
	private static boolean changeToWrapped(Entity entity) {
		BlockPos pos = entity.getBlockPos();
		int i = 0;

		while(i < 4) {
			pos = pos.down();
			i++;
			if(entity.world.isAir(pos))
				continue;

			if(entity.world.getBlockState(pos).getBlock() == cobbedstone) {
				WrappedEntity wrapped = new WrappedEntity(wrappedType, entity.world);
				Vec3d epos = entity.getPos();
				
				wrapped.updatePositionAndAngles(epos.x, epos.y, epos.z, entity.yaw, entity.pitch);
				entity.world.spawnEntity(wrapped);

				return true;
			}

			return false;
		}
		return false;
	}

	@Override
	protected UndergroundBiomeConfig getBiomeConfig() {
		return new UndergroundBiomeConfig(new SpiderNestUndergroundBiome(), 80, Biome.Category.PLAINS);
	}

	@Override
	protected String getBiomeName() {
		return "spider_nest";
	}

}

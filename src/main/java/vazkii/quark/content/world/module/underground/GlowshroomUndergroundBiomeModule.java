package vazkii.quark.content.world.module.underground;

import java.util.stream.Stream;

import net.minecraft.block.Block;
import net.minecraft.block.ComposterBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntitySpawnPlacementRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.spawner.WorldEntitySpawner;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import vazkii.quark.base.handler.BrewingHandler;
import vazkii.quark.base.handler.VariantHandler;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.ModuleCategory;
import vazkii.quark.base.module.config.Config;
import vazkii.quark.base.util.QuarkEffect;
import vazkii.quark.content.world.block.GlowceliumBlock;
import vazkii.quark.content.world.block.GlowshroomBlock;
import vazkii.quark.content.world.block.HugeGlowshroomBlock;
import vazkii.quark.content.world.config.UndergroundBiomeConfig;
import vazkii.quark.content.world.gen.underground.GlowshroomUndergroundBiome;

@LoadModule(category = ModuleCategory.WORLD, hasSubscriptions = true, subscribeOn = Dist.CLIENT)
public class GlowshroomUndergroundBiomeModule extends UndergroundBiomeModule {

	@Config
	@Config.Min(value = 0, exclusive = true)
	public static int glowshroomGrowthRate = 20;

	@Config
	@Config.Min(value = 0)
	@Config.Max(value = 1)
	public static double glowshroomSpawnChance = 0.0625;

	@Config
	public static boolean enableHugeGlowshrooms = true;

	@Config(flag = "glowshroom_danger_sight")
	public static boolean enableDangerSight = true;

	public static Block glowcelium;
	public static GlowshroomBlock glowshroom;
	public static Block glowshroom_block;
	public static Block glowshroom_stem;

	private QuarkEffect dangerSight;

	@Override
	public void construct() {
		glowcelium = new GlowceliumBlock(this);
		glowshroom = new GlowshroomBlock(this);
		glowshroom_block = new HugeGlowshroomBlock("glowshroom_block", this);
		glowshroom_stem = new HugeGlowshroomBlock("glowshroom_stem", this);

		dangerSight = new QuarkEffect("danger_sight", EffectType.BENEFICIAL, 0x08C8E3);

		BrewingHandler.addPotionMix("glowshroom_danger_sight",
				() -> Ingredient.fromItems(glowshroom), dangerSight, 3600, 9600, -1);

		VariantHandler.addFlowerPot(glowshroom, "glowshroom", p -> p.setLightLevel(b -> 14));

		super.construct();
	}

	@Override
	public void loadComplete() {
		enqueue(() -> {
			ComposterBlock.CHANCES.put(glowshroom_stem.asItem(), 0.65F);
			ComposterBlock.CHANCES.put(glowshroom.asItem(), 0.65F);
			ComposterBlock.CHANCES.put(glowshroom_block.asItem(), 0.65F);
		});
		
		super.loadComplete();
	}

	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public void clientTick(TickEvent.ClientTickEvent event) {
		Minecraft mc = Minecraft.getInstance();
		if(enableDangerSight && event.phase == TickEvent.Phase.START && mc.player != null && mc.player.getActivePotionEffect(dangerSight) != null && !mc.isGamePaused()) {
			int range = 12;
			World world = mc.world;
			Stream<BlockPos> positions = BlockPos.getAllInBox(mc.player.getPosition().add(-range, -range, -range), mc.player.getPosition().add(range, range, range));

			positions.forEach((pos) -> {
				if(world.rand.nextFloat() < 0.1 && canSpawnOn(EntityType.ZOMBIE, world, pos)) { 
					float x = pos.getX() + 0.3F + world.rand.nextFloat() * 0.4F;
					float y = pos.getY();
					float z = pos.getZ() + 0.3F + world.rand.nextFloat() * 0.4F;
					
					world.addParticle(ParticleTypes.ENTITY_EFFECT, x, y, z, world.rand.nextFloat() < 0.9 ? 0 : 1, 0, 0);
				}
			});
		}
	}


	public static boolean canSpawnOn(EntityType<? extends MobEntity> typeIn, IWorld worldIn, BlockPos pos) {
		BlockPos testPos = pos.down();
		return worldIn instanceof World
				&& worldIn.getLightFor(LightType.BLOCK, pos) <= 7
				&& worldIn.getBlockState(testPos).canEntitySpawn(worldIn, testPos, typeIn)
				&& WorldEntitySpawner.canSpawnAtBody(EntitySpawnPlacementRegistry.PlacementType.ON_GROUND, worldIn, pos, EntityType.ZOMBIE)
				&& ((World) worldIn).hasNoCollisions(EntityType.ZOMBIE.getBoundingBoxWithSizeApplied(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5));
	}

	@Override
	protected String getBiomeName() {
		return "glowshroom";
	}

	@Override
	protected UndergroundBiomeConfig getBiomeConfig() {
		return new UndergroundBiomeConfig(new GlowshroomUndergroundBiome(), 80, Biome.Category.EXTREME_HILLS, Biome.Category.MUSHROOM);
	}

}

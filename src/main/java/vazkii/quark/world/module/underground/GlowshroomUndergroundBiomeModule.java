package vazkii.quark.world.module.underground;

import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.ComposterBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.entity.effect.StatusEffectType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.recipe.Ingredient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;
import net.minecraft.world.SpawnHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import vazkii.quark.base.effect.QuarkEffect;
import vazkii.quark.base.handler.BrewingHandler;
import vazkii.quark.base.handler.VariantHandler;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.ModuleCategory;
import vazkii.quark.base.module.config.Config;
import vazkii.quark.world.block.GlowceliumBlock;
import vazkii.quark.world.block.GlowshroomBlock;
import vazkii.quark.world.block.HugeGlowshroomBlock;
import vazkii.quark.world.config.UndergroundBiomeConfig;
import vazkii.quark.world.gen.underground.GlowshroomUndergroundBiome;

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

		dangerSight = new QuarkEffect("danger_sight", StatusEffectType.BENEFICIAL, 0x08C8E3);

		BrewingHandler.addPotionMix("glowshroom_danger_sight",
				() -> Ingredient.ofItems(glowshroom), dangerSight, 3600, 9600, -1);

		VariantHandler.addFlowerPot(glowshroom, "glowshroom", p -> p.lightLevel(b -> 14)); // lightValue

		super.construct();
	}

	@Override
	public void setup() {
		ComposterBlock.ITEM_TO_LEVEL_INCREASE_CHANCE.put(glowshroom_stem.asItem(), 0.65F);
		ComposterBlock.ITEM_TO_LEVEL_INCREASE_CHANCE.put(glowshroom.asItem(), 0.65F);
		ComposterBlock.ITEM_TO_LEVEL_INCREASE_CHANCE.put(glowshroom_block.asItem(), 0.65F);

		super.setup();
	}


	@SubscribeEvent
	@Environment(EnvType.CLIENT)
	public void clientTick(TickEvent.ClientTickEvent event) {
		MinecraftClient mc = MinecraftClient.getInstance();
		if(enableDangerSight && event.phase == TickEvent.Phase.START && mc.player != null && mc.player.getStatusEffect(dangerSight) != null && !mc.isPaused()) {
			int range = 12;
			World world = mc.world;
			Stream<BlockPos> positions = BlockPos.stream(mc.player.getBlockPos().add(-range, -range, -range), mc.player.getBlockPos().add(range, range, range));

			positions.forEach((pos) -> {
				if(world.random.nextFloat() < 0.1 && canSpawnOn(EntityType.ZOMBIE, world, pos)) { 
					float x = pos.getX() + 0.3F + world.random.nextFloat() * 0.4F;
					float y = pos.getY();
					float z = pos.getZ() + 0.3F + world.random.nextFloat() * 0.4F;
					
					world.addParticle(ParticleTypes.ENTITY_EFFECT, x, y, z, world.random.nextFloat() < 0.9 ? 0 : 1, 0, 0);
				}
			});
		}
	}


	public static boolean canSpawnOn(EntityType<? extends MobEntity> typeIn, WorldAccess worldIn, BlockPos pos) {
		BlockPos testPos = pos.down();
		return worldIn instanceof World
				&& worldIn.getLightLevel(LightType.BLOCK, pos) <= 7
				&& worldIn.getBlockState(testPos).allowsSpawning(worldIn, testPos, typeIn)
				&& SpawnHelper.canSpawnAtBody(SpawnRestriction.Location.ON_GROUND, worldIn, pos, EntityType.ZOMBIE)
				&& ((World) worldIn).doesNotCollide(EntityType.ZOMBIE.createSimpleBoundingBox(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5));
	}

	@Override
	protected String getBiomeName() {
		return "glowshroom";
	}

	@Override
	protected UndergroundBiomeConfig getBiomeConfig() {
		return new UndergroundBiomeConfig(new GlowshroomUndergroundBiome(), 80, Type.MOUNTAIN, Type.MUSHROOM);
	}

}

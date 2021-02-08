package vazkii.quark.content.automation.module;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.ai.goal.TemptGoal;
import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemGroup;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.GameRules;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.BabyEntitySpawnEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import vazkii.arl.util.RegistryHelper;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.base.module.ModuleCategory;
import vazkii.quark.base.module.ModuleLoader;
import vazkii.quark.base.module.config.Config;
import vazkii.quark.content.automation.block.FeedingTroughBlock;
import vazkii.quark.content.automation.tile.FeedingTroughTileEntity;

/**
 * @author WireSegal
 * Created at 9:48 AM on 9/20/19.
 */
@LoadModule(category = ModuleCategory.AUTOMATION, hasSubscriptions = true)
public class FeedingTroughModule extends QuarkModule {
    public static TileEntityType<FeedingTroughTileEntity> tileEntityType;

    @Config(description = "How long, in game ticks, between animals being able to eat from the trough")
    @Config.Min(1)
    public static int cooldown = 30;

    @Config(description = "The maximum amount of animals allowed around the trough's range for an animal to enter love mode")
    public static int maxAnimals = 32;
    
    @Config(description = "The chance (between 0 and 1) for an animal to enter love mode when eating from the trough")
    @Config.Min(value = 0.0, exclusive = true)
    @Config.Max(1.0)
    public static double loveChance = 0.333333333;
    
    @Config public static double range = 10;

    private static final ThreadLocal<Set<FeedingTroughTileEntity>> loadedTroughs = ThreadLocal.withInitial(HashSet::new);

    @SubscribeEvent
    public void buildTroughSet(TickEvent.WorldTickEvent event) {
        Set<FeedingTroughTileEntity> troughs = loadedTroughs.get();
        if (event.side == LogicalSide.SERVER) {
            if (event.phase == TickEvent.Phase.START) {
                breedingOccurred.remove();
                for (TileEntity tile : event.world.loadedTileEntityList) {
                    if (tile instanceof FeedingTroughTileEntity)
                        troughs.add((FeedingTroughTileEntity) tile);
                }
            } else {
                troughs.clear();
            }
        }
    }

    private static final ThreadLocal<Boolean> breedingOccurred = ThreadLocal.withInitial(() -> false);

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onBreed(BabyEntitySpawnEvent event) {
        if (event.getCausedByPlayer() == null && event.getParentA().world.getGameRules().getBoolean(GameRules.DO_MOB_LOOT))
            breedingOccurred.set(true);
    }

    @SubscribeEvent
    public void onOrbSpawn(EntityJoinWorldEvent event) {
        if (event.getEntity() instanceof ExperienceOrbEntity && breedingOccurred.get()) {
            event.setCanceled(true);
            breedingOccurred.remove();
        }
    }

    public static PlayerEntity temptWithTroughs(TemptGoal goal, PlayerEntity found) {
        if (!ModuleLoader.INSTANCE.isModuleEnabled(FeedingTroughModule.class) ||
                (found != null && (goal.isTempting(found.getHeldItemMainhand()) || goal.isTempting(found.getHeldItemOffhand()))))
            return found;

        if (!(goal.creature instanceof AnimalEntity) ||
                !((AnimalEntity) goal.creature).canFallInLove() ||
                ((AnimalEntity) goal.creature).getGrowingAge() != 0)
            return found;

        double shortestDistanceSq = Double.MAX_VALUE;
        BlockPos location = null;
        FakePlayer target = null;

        Set<FeedingTroughTileEntity> troughs = loadedTroughs.get();
        for (FeedingTroughTileEntity tile : troughs) {
            BlockPos pos = tile.getPos();
            double distanceSq = pos.distanceSq(goal.creature.getPositionVec(), true);
            if (distanceSq <= range * range && distanceSq < shortestDistanceSq) {
                FakePlayer foodHolder = tile.getFoodHolder(goal);
                if (foodHolder != null) {
                    shortestDistanceSq = distanceSq;
                    target = foodHolder;
                    location = pos.toImmutable();
                }
            }
        }

        if (target != null) {
        	Vector3d eyesPos = goal.creature.getPositionVec().add(0, goal.creature.getEyeHeight(), 0);
            Vector3d targetPos = new Vector3d(location.getX(), location.getY(), location.getZ()).add(0.5, 0.0625, 0.5);
            BlockRayTraceResult ray = goal.creature.world.rayTraceBlocks(new RayTraceContext(eyesPos, targetPos, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, goal.creature));

            if (ray.getType() == RayTraceResult.Type.BLOCK && ray.getPos().equals(location))
                return target;
        }

        return found;
    }

    @Override
    public void construct() {
        Block feedingTrough = new FeedingTroughBlock("feeding_trough", this, ItemGroup.DECORATIONS,
                Block.Properties.create(Material.WOOD).hardnessAndResistance(0.6F).sound(SoundType.WOOD));
        tileEntityType = TileEntityType.Builder.create(FeedingTroughTileEntity::new, feedingTrough).build(null);
        RegistryHelper.register(tileEntityType, "feeding_trough");
    }
}

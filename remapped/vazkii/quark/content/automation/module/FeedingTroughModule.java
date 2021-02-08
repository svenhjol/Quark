package vazkii.quark.content.automation.module;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.ai.goal.TemptGoal;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemGroup;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.RaycastContext;
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
    public static BlockEntityType<FeedingTroughTileEntity> tileEntityType;

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
                for (BlockEntity tile : event.world.blockEntities) {
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
                (found != null && (goal.isTemptedBy(found.getMainHandStack()) || goal.isTemptedBy(found.getOffHandStack()))))
            return found;

        if (!(goal.mob instanceof AnimalEntity) ||
                !((AnimalEntity) goal.mob).canEat() ||
                ((AnimalEntity) goal.mob).getBreedingAge() != 0)
            return found;

        double shortestDistanceSq = Double.MAX_VALUE;
        BlockPos location = null;
        FakePlayer target = null;

        Set<FeedingTroughTileEntity> troughs = loadedTroughs.get();
        for (FeedingTroughTileEntity tile : troughs) {
            BlockPos pos = tile.getPos();
            double distanceSq = pos.getSquaredDistance(goal.mob.getPos(), true);
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
        	Vec3d eyesPos = goal.mob.getPos().add(0, goal.mob.getStandingEyeHeight(), 0);
            Vec3d targetPos = new Vec3d(location.getX(), location.getY(), location.getZ()).add(0.5, 0.0625, 0.5);
            BlockHitResult ray = goal.mob.world.raycast(new RaycastContext(eyesPos, targetPos, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, goal.mob));

            if (ray.getType() == HitResult.Type.BLOCK && ray.getBlockPos().equals(location))
                return target;
        }

        return found;
    }

    @Override
    public void construct() {
        Block feedingTrough = new FeedingTroughBlock("feeding_trough", this, ItemGroup.DECORATIONS,
                Block.Properties.of(Material.WOOD).strength(0.6F).sounds(BlockSoundGroup.WOOD));
        tileEntityType = BlockEntityType.Builder.create(FeedingTroughTileEntity::new, feedingTrough).build(null);
        RegistryHelper.register(tileEntityType, "feeding_trough");
    }
}

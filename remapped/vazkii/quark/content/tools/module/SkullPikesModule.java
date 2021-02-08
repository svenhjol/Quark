package vazkii.quark.content.tools.module;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.PatrolEntity;
import net.minecraft.tag.BlockTags;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import vazkii.arl.util.RegistryHelper;
import vazkii.quark.base.Quark;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.ModuleCategory;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.base.module.config.Config;
import vazkii.quark.content.tools.ai.RunAwayFromPikesGoal;
import vazkii.quark.content.tools.client.render.SkullPikeRenderer;
import vazkii.quark.content.tools.entity.SkullPikeEntity;

@LoadModule(category = ModuleCategory.TWEAKS, hasSubscriptions = true)
public class SkullPikesModule extends QuarkModule {

	public static EntityType<SkullPikeEntity> skullPikeType;

    public static Tag<Block> pikeTrophiesTag;
    
    @Config public static double pikeRange = 5;
	
	@Override
	public void construct() {
		skullPikeType = EntityType.Builder.<SkullPikeEntity>create(SkullPikeEntity::new, SpawnGroup.MISC)
				.setDimensions(0.5F, 0.5F)
				.setTrackingRange(10)
				.setUpdateInterval(Integer.MAX_VALUE)
				.setShouldReceiveVelocityUpdates(false)
				.setCustomClientFactory((spawnEntity, world) -> new SkullPikeEntity(skullPikeType, world))
				.build("skull_pike");
		RegistryHelper.register(skullPikeType, "skull_pike");
	}
	
    @Override
    public void setup() {
    	pikeTrophiesTag = BlockTags.createOptional(new Identifier(Quark.MOD_ID, "pike_trophies"));
    }
	
	@Override
	@Environment(EnvType.CLIENT)
	public void clientSetup() {
		RenderingRegistry.registerEntityRenderingHandler(skullPikeType, SkullPikeRenderer::new);
	}

	@SubscribeEvent
	public void onPlaceBlock(BlockEvent.EntityPlaceEvent event) {
		BlockState state = event.getPlacedBlock();
		
		if(state.getBlock().isIn(pikeTrophiesTag)) {
			WorldAccess iworld = event.getWorld();
			
			if(iworld instanceof World) {
				World world = (World) iworld;
				BlockPos pos = event.getPos();
				BlockPos down = pos.down();
				BlockState downState = world.getBlockState(down);
				
				if(downState.getBlock().isIn(BlockTags.FENCES)) {
					SkullPikeEntity pike = new SkullPikeEntity(skullPikeType, world);
					pike.updatePosition(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
					world.spawnEntity(pike);
				}
			}
		}
	}
	
    @SubscribeEvent
    public void onMonsterAppear(EntityJoinWorldEvent event) {
    	Entity e = event.getEntity();
        if(e instanceof HostileEntity && !(e instanceof PatrolEntity) && e.canUsePortals()) {
        	HostileEntity monster = (HostileEntity) e;
            boolean alreadySetUp = monster.goalSelector.goals.stream().anyMatch((goal) -> goal.getGoal() instanceof RunAwayFromPikesGoal);

            if (!alreadySetUp)
            	monster.goalSelector.add(3, new RunAwayFromPikesGoal(monster, (float) pikeRange, 1.0D, 1.2D));
        }
    }
}

package vazkii.quark.content.world.module;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particles.BlockParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.GenerationStage.Decoration;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.entity.living.EnderTeleportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.ModuleCategory;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.base.module.config.Config;
import vazkii.quark.base.world.WorldGenHandler;
import vazkii.quark.base.world.WorldGenWeights;
import vazkii.quark.base.world.config.DimensionConfig;
import vazkii.quark.base.world.config.StrictBiomeConfig;
import vazkii.quark.content.world.block.MyaliteCrystalBlock;
import vazkii.quark.content.world.gen.SpiralSpireGenerator;

@LoadModule(category = ModuleCategory.WORLD, hasSubscriptions = true)
public class SpiralSpiresModule extends QuarkModule {

	@Config
	public static DimensionConfig dimensions = DimensionConfig.end(false);
	
	@Config
	public static StrictBiomeConfig biomes = new StrictBiomeConfig(false, "minecraft:end_highlands");
	
	@Config public static int rarity = 200;
	@Config public static int radius = 15;
	
	@Config(description = "Set to 0 to turn off Myalite Conduits") 
	public static int myaliteConduitDistance = 24;
	
	@Config public static boolean renewableMyalite = true;
	
	public static Block myalite_crystal;
	
	@Override
	public void construct() {
		myalite_crystal = new MyaliteCrystalBlock(this);
	}
	
	@Override
	public void setup() {
		WorldGenHandler.addGenerator(this, new SpiralSpireGenerator(dimensions), Decoration.SURFACE_STRUCTURES, WorldGenWeights.SPIRAL_SPIRES);
	}
	
	@SubscribeEvent
	public void onTeleport(EnderTeleportEvent event) {
		if(myaliteConduitDistance <= 0)
			return;
		
		LivingEntity entity = event.getEntityLiving();
		World world = entity.world;
		BlockPos pos = new BlockPos(event.getTargetX(), event.getTargetY(), event.getTargetZ());
		
		List<BlockPos> myalite = getAdjacentMyalite(null, world, pos, null);
		if(myalite.isEmpty()) {
			pos = pos.down();
			myalite = getAdjacentMyalite(null, world, pos, null);
		}
		
		if(!myalite.isEmpty()) {
			BlockPos prev = null;
			BlockPos cond = pos;
			
			List<BlockPos> found = new ArrayList<>();
			int moves = 0;
			do {
				prev = cond;
				cond = myalite.get(world.rand.nextInt(myalite.size()));
				found.add(cond);
				myalite = getAdjacentMyalite(found, world, cond, null);
				
				moves++;
				if(myalite == null || moves > myaliteConduitDistance)
					return;
			} while(!myalite.isEmpty());
			
			
			BlockPos test = cond.add(cond.getX() - prev.getX(), cond.getY() - prev.getY(), cond.getZ() - prev.getZ());
			
			find: if(!world.getBlockState(test).isAir()) {
				for(Direction d : Direction.values()) {
					test = cond.offset(d);
					if(world.getBlockState(test).isAir()) {
						if(d.getAxis() == Axis.Y)
							test = test.offset(d);
						
						break find;
					}
				}
				
				return;
			}
			
			event.setTargetX(test.getX() + 0.5);
			event.setTargetY(test.getY() + 0.5);
			event.setTargetZ(test.getZ() + 0.5);
			
			if(world instanceof ServerWorld) {
				ServerWorld sworld = (ServerWorld) world;
				for(BlockPos f : found)
					sworld.spawnParticle(new BlockParticleData(ParticleTypes.BLOCK, myalite_crystal.getDefaultState()), f.getX() + 0.5, f.getY() + 0.5, f.getZ() + 0.5, 30, 0.25, 0.25, 0.25, 0);
			}
		}
	}
	
	private static List<BlockPos> getAdjacentMyalite(List<BlockPos> found, World world, BlockPos pos, Direction ignore) {
		List<BlockPos> ret = new ArrayList<>(6);
		List<BlockPos> collisions = new ArrayList<>();
		
		for(Direction d : Direction.values()) 
			if(d != ignore) {
				BlockPos off = pos.offset(d);
				if(world.getBlockState(off).getBlock() == myalite_crystal) {
					if(found != null && found.contains(off))
						collisions.add(off);
					else ret.add(off);
				}
			}
		
		if(ret.isEmpty() && collisions.size() > 1)
			return null;
		
		return ret;
	}

}

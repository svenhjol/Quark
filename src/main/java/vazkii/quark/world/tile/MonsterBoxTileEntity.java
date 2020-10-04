package vazkii.quark.world.tile;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.CaveSpiderEntity;
import net.minecraft.entity.mob.WitchEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Tickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Difficulty;
import vazkii.arl.block.tile.TileMod;
import vazkii.quark.base.handler.QuarkSounds;
import vazkii.quark.world.module.MonsterBoxModule;

public class MonsterBoxTileEntity extends TileMod implements Tickable {

	private int breakProgress;
	
	public MonsterBoxTileEntity() {
		super(MonsterBoxModule.tileEntityType);
	}
	
	@Override
	public void tick() {
		if(world.getDifficulty() == Difficulty.PEACEFUL)
			return;
		
		BlockPos pos = getPos();
		
		int x = pos.getX();
		int y = pos.getY();
		int z = pos.getZ();
		
		if(world.isClient)
			world.addParticle(breakProgress == 0 ? ParticleTypes.FLAME : ParticleTypes.LARGE_SMOKE, x + Math.random(), y + Math.random(), z + Math.random(), 0, 0, 0);
		
		boolean doBreak = breakProgress > 0;
		if(!doBreak) {
			List<? extends PlayerEntity> players = world.getPlayers();
			for(PlayerEntity p : players)
				if(p.squaredDistanceTo(x + 0.5, y + 0.5, z + 0.5) < 6.25 && !p.isSpectator()) {
					doBreak = true;
					break;
				}
		}
		
		if(doBreak) {
			if(breakProgress == 0) 
				world.playSound(null, pos, QuarkSounds.BLOCK_MONSTER_BOX_GROWL, SoundCategory.BLOCKS, 0.5F, 1F);
			
			breakProgress++;
			if(breakProgress > 40) {
				world.syncWorldEvent(2001, pos, Block.getRawIdFromState(world.getBlockState(pos)));
				world.removeBlock(pos, false);
				spawnMobs();
			}
		}
	}
	
	private void spawnMobs() {
		if(world.isClient)
			return;
		
		BlockPos pos = getPos();

		int mobCount = MonsterBoxModule.minMobCount + world.random.nextInt(Math.max(MonsterBoxModule.maxMobCount - MonsterBoxModule.minMobCount + 1, 1));
		for(int i = 0; i < mobCount; i++) {
			LivingEntity e;
			
			float r = world.random.nextFloat();
			if(r < 0.1)
				e = new WitchEntity(EntityType.WITCH, world);
			else if(r < 0.3)
				e = new CaveSpiderEntity(EntityType.CAVE_SPIDER, world);
			else e = new ZombieEntity(world);
			
			double motionMultiplier = 0.4;
			e.updatePosition(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
			double mx = (world.random.nextFloat() - 0.5) * motionMultiplier;
			double my = (world.random.nextFloat() - 0.5) * motionMultiplier;
			double mz = (world.random.nextFloat() - 0.5) * motionMultiplier;
			e.setVelocity(mx, my, mz);
			e.getPersistentData().putBoolean(MonsterBoxModule.TAG_MONSTER_BOX_SPAWNED, true);
			
			world.spawnEntity(e);
		}
	}

}
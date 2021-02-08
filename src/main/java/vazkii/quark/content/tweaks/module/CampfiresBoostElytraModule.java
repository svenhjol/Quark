package vazkii.quark.content.tweaks.module;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CampfireBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.base.module.ModuleCategory;
import vazkii.quark.base.module.config.Config;

@LoadModule(category = ModuleCategory.TWEAKS, hasSubscriptions = true)
public class CampfiresBoostElytraModule extends QuarkModule {
	
	@Config public double boostStrength = 0.5;
	@Config public double maxSpeed = 1;

	@SubscribeEvent
	public void onPlayerTick(PlayerTickEvent event) {
		PlayerEntity player = event.player;
		
		if(player.isElytraFlying()) {
			Vector3d motion = player.getMotion();
			if(motion.getY() < maxSpeed) {
				BlockPos pos = player.getPosition();
				World world = player.world;
				
				int moves = 0;
				while(world.isAirBlock(pos) && pos.getY() > 0 && moves < 20) {
					pos = pos.down();
					moves++;
				}
				
				BlockState state = world.getBlockState(pos);
				Block block = state.getBlock();
				boolean isCampfire = block.isIn(BlockTags.CAMPFIRES);
				if(isCampfire && block instanceof CampfireBlock && state.get(CampfireBlock.LIT) && state.get(CampfireBlock.SIGNAL_FIRE)) {
					double force = boostStrength;
					if(moves > 16)
						force -= (force * (1.0 - ((double) moves - 16.0) / 4.0));
					
					if(block == Blocks.SOUL_CAMPFIRE)
						force *= -1.5;
					
					player.setMotion(motion.getX(), Math.min(maxSpeed, motion.getY() + force), motion.getZ());
				}
			}
		}
	}
	
}

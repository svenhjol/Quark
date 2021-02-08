package vazkii.quark.content.world.gen;

import java.util.Random;

import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.FlatChunkGenerator;
import net.minecraft.world.gen.WorldGenRegion;
import vazkii.quark.base.world.config.DimensionConfig;
import vazkii.quark.base.world.generator.Generator;
import vazkii.quark.content.world.module.MonsterBoxModule;

public class MonsterBoxGenerator extends Generator {

	public MonsterBoxGenerator(DimensionConfig dimConfig) {
		super(dimConfig);
	}

	@Override
	public void generateChunk(WorldGenRegion world, ChunkGenerator generator, Random rand, BlockPos chunkCorner) {
		if(generator instanceof FlatChunkGenerator)
			return;
		
		double chance =  MonsterBoxModule.chancePerChunk;
		
		while(rand.nextDouble() <= chance) {
				BlockPos pos = chunkCorner.add(rand.nextInt(16), MonsterBoxModule.minY + rand.nextInt(MonsterBoxModule.maxY - MonsterBoxModule.minY + 1), rand.nextInt(16));
			if(world.isAirBlock(pos)) {
				BlockPos testPos = pos;
				BlockState testState;
				do {
					testPos = testPos.down();
					testState = world.getBlockState(testPos);
				} while(testState.getMaterial() != Material.ROCK && testPos.getY() >= MonsterBoxModule.minY);
				
				BlockPos placePos = testPos.up();
				if(testPos.getY() >= MonsterBoxModule.minY && world.isAirBlock(placePos))
					world.setBlockState(placePos, MonsterBoxModule.monster_box.getDefaultState(), 0);
			}
			
			chance -=MonsterBoxModule.chancePerChunk;
		}
	}

}

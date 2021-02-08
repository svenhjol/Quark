package vazkii.quark.content.world.gen;

import java.util.Random;
import java.util.function.BooleanSupplier;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.WorldGenRegion;
import vazkii.quark.base.world.config.DimensionConfig;
import vazkii.quark.base.world.generator.Generator;
import vazkii.quark.content.world.block.SpeleothemBlock;
import vazkii.quark.content.world.block.SpeleothemBlock.SpeleothemSize;
import vazkii.quark.content.world.module.SpeleothemsModule;

public class SpeleothemGenerator extends Generator {

	public SpeleothemGenerator(DimensionConfig dimConfig, BooleanSupplier condition) {
		super(dimConfig, condition);
	}

	@Override
	public void generateChunk(WorldGenRegion world, ChunkGenerator generator, Random rand, BlockPos pos) {
		int spread = 10;
		int tries = SpeleothemsModule.triesPerChunk;
		int innerSpread = 6;
		int innerTries = SpeleothemsModule.speleothemsPerChunk;
		int upperBound = SpeleothemsModule.maxYlevel;
		int offset = 6;
		
		if(isNether(world)) { 
			upperBound = 128;
			offset = 0;
			tries = SpeleothemsModule.triesPerChunkInNether;
			innerTries = SpeleothemsModule.speleothemsPerChunkInNether;
		}
		
		if(upperBound > 0)
			for(int i = 0; i < tries; i++) {
				BlockPos target = pos.add(rand.nextInt(spread), rand.nextInt(upperBound) + offset, rand.nextInt(spread));
				if(placeSpeleothemCluster(rand, world, target, innerSpread, innerTries))
					i++;
			}
	}
	
	private boolean placeSpeleothemCluster(Random random, IWorld world, BlockPos pos, int spread, int tries) {
		if(!findAndPlaceSpeleothem(random, world, pos))
			return false;
		
		for(int i = 0; i < tries; i++) {
			BlockPos target = pos.add(random.nextInt(spread * 2 + 1) - spread, random.nextInt(spread + 1) - spread, random.nextInt(spread * 2 + 1) - spread);
			findAndPlaceSpeleothem(random, world, target);
		}
		
		return true;
	}
	
	private boolean findAndPlaceSpeleothem(Random random, IWorld world, BlockPos pos) {
		if(!world.isAirBlock(pos))
			return false;
		
		int off = isNether(world) ? -1000 : 0; // isNether
		boolean up = random.nextBoolean();
		Direction diff = (up ? Direction.UP : Direction.DOWN);
		
		if(!up && world.canBlockSeeSky(pos))
 			return false;
		
		BlockState stateAt;
		do {
			pos = pos.offset(diff);
			stateAt = world.getBlockState(pos);
			off++;
		} while(pos.getY() > 4 && pos.getY() < 200 && !stateAt.isSolid() && off < 10);
		
		Block type = getSpeleothemType(stateAt);
		placeSpeleothem(random, world, pos, type, !up);
		
		return true;
	}
		
	private void placeSpeleothem(Random random, IWorld world, BlockPos pos, Block type, boolean up) {
		if(type == null)
			return;
		
		Direction diff = up ? Direction.UP : Direction.DOWN;
		int size = random.nextInt(3) == 0 ? 2 : 3;
		if(!up && random.nextInt(20) == 0)
			size = 1;
		
		for(int i = 0; i < size; i++) {
			pos = pos.offset(diff);
			if(!world.isAirBlock(pos))
				return;
			
			SpeleothemSize sizeType = SpeleothemSize.values()[size - i - 1];
			BlockState targetBlock = type.getDefaultState().with(SpeleothemBlock.SIZE, sizeType);
			world.setBlockState(pos, targetBlock, 0);
		}
	}
	
	private Block getSpeleothemType(BlockState state) {
		return SpeleothemsModule.speleothemMapping.get(state.getBlock());
	}
	
}

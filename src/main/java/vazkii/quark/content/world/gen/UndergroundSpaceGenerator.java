package vazkii.quark.content.world.gen;

import java.util.Random;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.FlatChunkGenerator;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.WorldGenRegion;
import vazkii.quark.base.world.config.ClusterSizeConfig;
import vazkii.quark.base.world.config.DimensionConfig;
import vazkii.quark.base.world.generator.multichunk.ClusterBasedGenerator;

public class UndergroundSpaceGenerator extends ClusterBasedGenerator {

	public UndergroundSpaceGenerator(DimensionConfig dimConfig, ClusterSizeConfig sizeConfig, long seedXor) {
		super(dimConfig, sizeConfig, seedXor);
	}

	@Override
	public IGenerationContext createContext(BlockPos src, ChunkGenerator generator, Random random, BlockPos chunkCorner, WorldGenRegion world) {
		return (pos, caveNoise) -> {
			BlockState state = world.getBlockState(pos);

			if(state.getBlockHardness(world, pos) > -1 || state.getMaterial() == Material.LAVA) {
				if(pos.getY() < 6){
					world.setBlockState(pos, Blocks.LAVA.getDefaultState(), 1); //flags doesnt do anything during worldgen btw. thanks mojang
				}
				else if(noWaterNeighbor(pos, world)){
					world.setBlockState(pos, Blocks.CAVE_AIR.getDefaultState(), 2);

					// These checks should only happen at edge of cave so by passing the
					// actual noise all the way here, we can make these checks more efficient
					// by doing them where they actually matter. (0 is edge of cave)
					if(caveNoise < 0.06D){
						removeInvalidBlocksAndEncloseLava(pos, world, state);
					}
				}
			}
		};
	}

	/**
	 * Attempt to check above and remove blocks whose position is no longer
	 * valid once we carved the cave (think tall grass floating and such)
	 */
	private static void removeInvalidBlocksAndEncloseLava(BlockPos src, WorldGenRegion world, BlockState state){
		BlockPos.Mutable mutable = new BlockPos.Mutable().setPos(src);

		for(Direction direction : Direction.values()){
			mutable.setPos(src).move(direction);

			// Recursive due to nature of some blocks like double tall grass or
			// stalagemites being multiple blocks that needs to be removed if floating.
			if(direction == Direction.UP || direction == Direction.DOWN){
				while(mutable.getY() < world.getHeight() && mutable.getY() > 0 && !world.getBlockState(mutable).isValidPosition(world, mutable)){
					world.setBlockState(mutable, Blocks.CAVE_AIR.getDefaultState(), 2);
					mutable.move(direction);
				}
			}
			else{
				// for sides only
				if(!world.getBlockState(mutable).isValidPosition(world, mutable)){
					world.setBlockState(mutable, Blocks.CAVE_AIR.getDefaultState(), 2);
				}
			}

			// Enclose lava.
			if(direction != Direction.DOWN){
				mutable.setPos(src).move(direction);
				BlockState neighborBlock = world.getBlockState(mutable);
				if(neighborBlock.getMaterial() == Material.LAVA){
					world.setBlockState(mutable, state, 3);
				}
			}
		}
	}

	private static boolean noWaterNeighbor(BlockPos src, WorldGenRegion world){
		// This prevents extra unnecessary blockpos objects from being made by reusing
		// a mutable blockpos instead of doing src.add(direction) which would make this
		// a new blockpos for every direction. If you want to optimize further, make
		// a mutable be make for the chunk and pass it down all the way here so every
		// pos in the chunk uses the same mutable for liquid checks too.
		BlockPos.Mutable mutable = new BlockPos.Mutable();

		for(Direction direction : Direction.values()){
			//We do not need to check below as fluids only flow sideways or downward.
			if(direction != Direction.DOWN){
				BlockState neighborBlock = world.getBlockState(mutable.setPos(src).move(direction));
				// The heightmap check is to prevent vanilla lake features from floating inside the cave.
				// Also thickens the buffer between seafloor and cave opening.
				// Though lakes at surface can still be an issue... screw vanilla lakes
				if(neighborBlock.getMaterial() == Material.WATER && mutable.getY() > world.getHeight(Heightmap.Type.OCEAN_FLOOR, mutable).getY() - 2){
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public BlockPos[] getSourcesInChunk(WorldGenRegion world, Random random, ChunkGenerator generator, BlockPos chunkLeft) {
		if(!(generator instanceof FlatChunkGenerator)) {
			int rarity = shapeProvider.getRarity();
			if(rarity > 0 && random.nextInt(rarity) == 0) {
				BlockPos pos = chunkLeft.add(random.nextInt(16), shapeProvider.getRandomYLevel(random), random.nextInt(16));
				if(shapeProvider.getBiomeTypes().canSpawn(getBiome(world, pos)))
					return new BlockPos[] { pos };
			}
		}
		
		return new BlockPos[0];
	}

}

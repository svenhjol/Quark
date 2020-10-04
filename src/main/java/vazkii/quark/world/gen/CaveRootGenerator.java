package vazkii.quark.world.gen;

import java.util.Random;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import vazkii.quark.base.handler.MiscUtil;
import vazkii.quark.base.world.config.DimensionConfig;
import vazkii.quark.base.world.generator.Generator;
import vazkii.quark.world.block.RootBlock;
import vazkii.quark.world.module.CaveRootsModule;

public class CaveRootGenerator extends Generator {

	public CaveRootGenerator(DimensionConfig dimConfig) {
		super(dimConfig);
	}

	@Override
	public void generateChunk(ChunkRegion worldIn, ChunkGenerator generator, StructureAccessor structureManager, Random rand, BlockPos corner) {
		for(int i = 0; i < CaveRootsModule.chunkAttempts; i++) {
			int x = rand.nextInt(12) + 2;
			int z = rand.nextInt(12) + 2;
			int y = rand.nextInt(CaveRootsModule.maxY - CaveRootsModule.minY) + CaveRootsModule.minY;
			
			BlockPos pos = corner.add(x, y, z);
			if(worldIn.isAir(pos)) {
				for(Direction facing : MiscUtil.HORIZONTALS) {
					BlockPos target = pos.offset(facing);
					if(RootBlock.isAcceptableNeighbor(worldIn, target, facing.getOpposite())) {
						BlockState state = CaveRootsModule.root.getDefaultState().with(RootBlock.getFacingProperty(facing), true);
						worldIn.setBlockState(pos, state, 2);
						RootBlock.growMany(worldIn, rand, pos, state, 0.4F);
					}
				}
			}
		}
	}

}

package vazkii.quark.content.world.gen;

import java.util.Random;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SaplingBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.Heightmap.Type;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.Feature;
import vazkii.quark.base.world.generator.Generator;
import vazkii.quark.content.world.block.BlossomSaplingBlock.BlossomTree;
import vazkii.quark.content.world.config.BlossomTreeConfig;

public class BlossomTreeGenerator extends Generator {

	BlossomTreeConfig config;
	BlossomTree tree;
	
	public BlossomTreeGenerator(BlossomTreeConfig config, BlossomTree tree) {
		super(config.dimensions);
		this.config = config;
		this.tree = tree;
	}

	@Override
	public void generateChunk(ChunkRegion worldIn, ChunkGenerator generator, Random rand, BlockPos pos) {
		BlockPos placePos = pos.add(rand.nextInt(16), 0, rand.nextInt(16));
		if(config.biomeTypes.canSpawn(getBiome(worldIn, placePos)) && rand.nextInt(config.rarity) == 0) {
			placePos = worldIn.getTopPosition(Type.MOTION_BLOCKING, placePos).down();

			BlockState state = worldIn.getBlockState(placePos);
			if(state.getBlock().canSustainPlant(state, worldIn, pos, Direction.UP, (SaplingBlock) Blocks.OAK_SAPLING))
				Feature.TREE.generate(worldIn, generator, rand, placePos, tree.config);
		}
	}

}

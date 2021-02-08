package vazkii.quark.base.world;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;

import net.minecraft.block.Blocks;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.gen.feature.jigsaw.JigsawPattern;
import net.minecraft.world.gen.feature.jigsaw.JigsawPatternRegistry;
import net.minecraft.world.gen.feature.jigsaw.JigsawPiece;
import net.minecraft.world.gen.feature.template.IStructureProcessorType;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.StructureProcessor;
import net.minecraft.world.gen.feature.template.StructureProcessorList;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraft.world.gen.feature.template.Template.BlockInfo;
import vazkii.quark.base.Quark;

public class JigsawRegistryHelper {

	public static final FakeAirProcessor FAKE_AIR = new FakeAirProcessor();

	private static Codec<FakeAirProcessor> fakeAirCodec = Codec.unit(FAKE_AIR);
	private static IStructureProcessorType<FakeAirProcessor> fakeAirType = () -> fakeAirCodec;

	public static PoolBuilder pool(String namespace, String name) {
		return new PoolBuilder(namespace, name);
	}

	public static void setup() {
		Registry.register(Registry.STRUCTURE_PROCESSOR, Quark.MOD_ID + ":fake_air", fakeAirType);
	}

	public static class PoolBuilder {

		private final String namespace, name;
		private final List<PiecePrototype> pieces = new LinkedList<>();
		private final List<StructureProcessor> globalProcessors = new LinkedList<>();

		private PoolBuilder(String namespace, String name) {
			this.namespace = namespace;
			this.name = name;

			globalProcessors.add(FAKE_AIR);
		}

		public PoolBuilder processor(StructureProcessor... processors) {
			for(StructureProcessor p : processors)
				globalProcessors.add(p);
			return this;
		}

		public PoolBuilder add(String name, int weight) {
			pieces.add(new PiecePrototype(name, weight));
			return this;
		}

		public PoolBuilder add(String name, int weight, StructureProcessor... processors) {
			pieces.add(new PiecePrototype(name, weight, processors));
			return this;
		}

		public PoolBuilder addMult(String dir, Iterable<String> names, int weight) {
			String pref = dir.isEmpty() ? "" : (dir + "/");
			for(String s : names)
				add(pref + s, weight);
			return this;
		}

		public JigsawPattern register(JigsawPattern.PlacementBehaviour placementBehaviour) {
			ResourceLocation resource = new ResourceLocation(Quark.MOD_ID, namespace + "/" + name);

			List<Pair<Function<JigsawPattern.PlacementBehaviour, ? extends JigsawPiece>, Integer>> createdPieces = new LinkedList<>();
			for(PiecePrototype proto : pieces) {
				Pair<Function<JigsawPattern.PlacementBehaviour, ? extends JigsawPiece>, Integer> newPiece =
						Pair.of(JigsawPiece.func_242861_b((Quark.MOD_ID + ":" + namespace + "/" + proto.name), new StructureProcessorList(proto.processors)), proto.weight);
				createdPieces.add(newPiece);
			}

			JigsawPattern pattern = new JigsawPattern(resource, new ResourceLocation("empty"), createdPieces, placementBehaviour);
			return JigsawPatternRegistry.func_244094_a(pattern);
		}

		private class PiecePrototype {
			final String name;
			final int weight;
			final List<StructureProcessor> processors;

			public PiecePrototype(String name, int weight) {
				this(name, weight, new StructureProcessor[0]);
			}

			public PiecePrototype(String name, int weight, StructureProcessor... processors) {
				this.name = name;
				this.weight = weight;
				this.processors = Streams.concat(Arrays.stream(processors), globalProcessors.stream()).collect(ImmutableList.toImmutableList());
			}
		}

	}

	private static class FakeAirProcessor extends StructureProcessor {

		public FakeAirProcessor() { 
			// NO-OP
		}

		@Override
		public BlockInfo process(IWorldReader worldReaderIn, BlockPos pos, BlockPos otherposidk, BlockInfo p_215194_3_, BlockInfo blockInfo, PlacementSettings placementSettingsIn, Template template) {
			if(blockInfo.state.getBlock() == Blocks.BARRIER)
				return new BlockInfo(blockInfo.pos, Blocks.CAVE_AIR.getDefaultState(), new CompoundNBT());

			else if(blockInfo.state.getValues().containsKey(BlockStateProperties.WATERLOGGED) && blockInfo.state.get(BlockStateProperties.WATERLOGGED))
				return new BlockInfo(blockInfo.pos, blockInfo.state.with(BlockStateProperties.WATERLOGGED, false), blockInfo.nbt);

			return blockInfo;
		}

		@Override
		protected IStructureProcessorType<?> getType() {
			return fakeAirType;
		}

	}

}



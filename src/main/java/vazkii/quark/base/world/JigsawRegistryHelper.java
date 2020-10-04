package vazkii.quark.base.world;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;

import net.minecraft.block.Blocks;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.state.property.Properties;
import net.minecraft.structure.Structure;
import net.minecraft.structure.Structure.StructureBlockInfo;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.pool.SinglePoolElement;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.structure.pool.StructurePoolBasedGenerator;
import net.minecraft.structure.pool.StructurePoolElement;
import net.minecraft.structure.processor.StructureProcessor;
import net.minecraft.structure.processor.StructureProcessorType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.WorldView;
import net.minecraftforge.registries.ForgeRegistries;
import vazkii.arl.util.RegistryHelper;
import vazkii.quark.base.Quark;

public class JigsawRegistryHelper {
	
	public static final FakeAirProcessor FAKE_AIR = new FakeAirProcessor();
	
	private static Codec<FakeAirProcessor> fakeAirCodec = Codec.unit(FAKE_AIR);
	private static StructureProcessorType<FakeAirProcessor> fakeAirType = () -> fakeAirCodec;
	
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
		
		@SuppressWarnings("deprecation")
		public void register(StructurePool.Projection placementBehaviour) {
			Identifier resource = new Identifier(Quark.MOD_ID, namespace + "/" + name);
			
			List<Pair<StructurePoolElement, Integer>> createdPieces = 
			pieces.stream()
			.map(proto -> Pair.of((StructurePoolElement) new SinglePoolElement((Quark.MOD_ID + ":" + namespace + "/" + proto.name), proto.processors), proto.weight))
			.collect(ImmutableList.toImmutableList());
			
			StructurePoolBasedGenerator.REGISTRY.add(new StructurePool(resource, new Identifier("empty"), createdPieces, placementBehaviour));
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
	    public StructureBlockInfo process(WorldView worldReaderIn, BlockPos pos, BlockPos otherposidk, StructureBlockInfo p_215194_3_, StructureBlockInfo blockInfo, StructurePlacementData placementSettingsIn, Structure template) {
	        if(blockInfo.state.getBlock() == Blocks.BARRIER)
	            return new StructureBlockInfo(blockInfo.pos, Blocks.CAVE_AIR.getDefaultState(), new CompoundTag());
	        
	        else if(blockInfo.state.getEntries().containsKey(Properties.WATERLOGGED) && blockInfo.state.get(Properties.WATERLOGGED))
	        	return new StructureBlockInfo(blockInfo.pos, blockInfo.state.with(Properties.WATERLOGGED, false), blockInfo.tag);
	            
	    	return blockInfo;
	    }

		@Override
		protected StructureProcessorType<?> getType() {
			return fakeAirType;
		}
	    
	}
	
}



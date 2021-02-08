package vazkii.quark.base.world;

import javax.annotation.Nonnull;

import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.base.world.generator.IGenerator;

public class WeightedGenerator implements Comparable<WeightedGenerator> {

	public final QuarkModule module;
	public final IGenerator generator;
	public final int weight;
	
	public WeightedGenerator(QuarkModule module, IGenerator generator, int weight) {
		this.module = module;
		this.generator = generator;
		this.weight = weight;
	}

	@Override
	public int compareTo(@Nonnull WeightedGenerator o) {
		int diff = weight - o.weight;
		if(diff != 0)
			return diff;
		
		return hashCode() - o.hashCode();
	}
	
	@Override
	public int hashCode() {
		return generator.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		return obj == this || (obj instanceof WeightedGenerator && ((WeightedGenerator) obj).generator == generator); 
	}
	
}

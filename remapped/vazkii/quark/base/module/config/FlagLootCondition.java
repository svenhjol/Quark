package vazkii.quark.base.module.config;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import javax.annotation.Nonnull;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.condition.LootConditionType;
import net.minecraft.loot.context.LootContext;
import net.minecraft.util.JsonSerializer;

/**
 * @author WireSegal
 * Created at 1:23 PM on 8/24/19.
 */
public class FlagLootCondition implements LootCondition {

    private final ConfigFlagManager manager;
    private final String flag;

    public FlagLootCondition(ConfigFlagManager manager, String flag) {
        this.manager = manager;
        this.flag = flag;
    }

    @Override
    public boolean test(LootContext lootContext) {
        return manager.getFlag(flag);
    }
    

	@Nonnull
    @Override
	public LootConditionType getType() {
		return ConfigFlagManager.flagConditionType;
	}

    
    public static class Serializer implements JsonSerializer<FlagLootCondition> {
        private final ConfigFlagManager manager;

        public Serializer(ConfigFlagManager manager) {
            this.manager = manager;
        }

        @Override
        public void serialize(@Nonnull JsonObject json, @Nonnull FlagLootCondition value, @Nonnull JsonSerializationContext context) {
            json.addProperty("flag", value.flag);
        }

        @Nonnull
        @Override
        public FlagLootCondition fromJson(@Nonnull JsonObject json, @Nonnull JsonDeserializationContext context) {
            return new FlagLootCondition(manager, json.getAsJsonPrimitive("flag").getAsString());
        }
    }

}

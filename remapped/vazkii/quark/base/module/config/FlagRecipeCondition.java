package vazkii.quark.base.module.config;

import com.google.gson.JsonObject;
import net.minecraft.util.Identifier;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;

/**
 * @author WireSegal
 * Created at 1:23 PM on 8/24/19.
 */
public class FlagRecipeCondition implements ICondition {

    private final ConfigFlagManager manager;
    private final String flag;
    private final Identifier loc;

    public FlagRecipeCondition(ConfigFlagManager manager, String flag, Identifier loc) {
        this.manager = manager;
        this.flag = flag;
        this.loc = loc;
    }


    @Override
    public Identifier getID() {
        return loc;
    }

    @Override
    public boolean test() {
    	if(flag.contains("%"))
    		throw new RuntimeException("Illegal flag: " + flag);
    	
        return manager.getFlag(flag);
    }

    public static class Serializer implements IConditionSerializer<FlagRecipeCondition> {
        private final ConfigFlagManager manager;
        private final Identifier location;

        public Serializer(ConfigFlagManager manager, Identifier location) {
            this.manager = manager;
            this.location = location;
        }

        @Override
        public void write(JsonObject json, FlagRecipeCondition value) {
            json.addProperty("flag", value.flag);
        }

        @Override
        public FlagRecipeCondition read(JsonObject json) {
            return new FlagRecipeCondition(manager, json.getAsJsonPrimitive("flag").getAsString(), location);
        }

        @Override
        public Identifier getID() {
            return location;
        }
    }
}

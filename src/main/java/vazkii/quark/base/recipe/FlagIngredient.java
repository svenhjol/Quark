package vazkii.quark.base.recipe;

import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntLists;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraftforge.common.crafting.IIngredientSerializer;
import vazkii.quark.base.module.config.ConfigFlagManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.stream.Stream;

/**
 * @author WireSegal
 * Created at 3:44 PM on 10/20/19.
 */
public class FlagIngredient extends Ingredient {
    private final Ingredient parent;
    private final String flag;

    public FlagIngredient(Ingredient parent, String flag) {
        super(Stream.of());
        this.parent = parent;
        this.flag = flag;
    }

    private boolean isEnabled() {
        if (Serializer.INSTANCE == null)
            return false;
        return Serializer.INSTANCE.flagManager.getFlag(flag);
    }

    @Override
    @Nonnull
    public ItemStack[] getMatchingStacksClient() {
        if (!isEnabled())
            return new ItemStack[0];
        return parent.getMatchingStacksClient();
    }

    @Override
    @Nonnull
    public IntList getIds() {
        if (!isEnabled())
            return IntLists.EMPTY_LIST;
        return parent.getIds();
    }

    @Override
    public boolean test(@Nullable ItemStack target) {
        if (target == null || !isEnabled())
            return false;

        return parent.test(target);
    }

    @Override
    protected void invalidate() {
        // The invalidate method will collect our parent as well
    }

    @Override
    public boolean isSimple() {
        return parent.isSimple();
    }

    @Nonnull
    @Override
    public IIngredientSerializer<? extends Ingredient> getSerializer() {
        return Serializer.INSTANCE;
    }

    public static class Serializer implements IIngredientSerializer<FlagIngredient> {

        public static Serializer INSTANCE;

        private final ConfigFlagManager flagManager;

        public Serializer(ConfigFlagManager flagManager) {
            this.flagManager = flagManager;
            INSTANCE = this;
        }

        @Nonnull
        @Override
        public FlagIngredient parse(@Nonnull PacketByteBuf buffer) {
            return new FlagIngredient(Ingredient.fromPacket(buffer), buffer.readString());
        }

        @Nonnull
        @Override
        public FlagIngredient parse(@Nonnull JsonObject json) {
            Ingredient value = Ingredient.fromJson(json.get("value"));
            String flag = json.getAsJsonPrimitive("flag").getAsString();
            return new FlagIngredient(value, flag);
        }

        @Override
        public void write(@Nonnull PacketByteBuf buffer, @Nonnull FlagIngredient ingredient) {
            ingredient.parent.write(buffer);
            buffer.writeString(ingredient.flag);
        }

    }
}

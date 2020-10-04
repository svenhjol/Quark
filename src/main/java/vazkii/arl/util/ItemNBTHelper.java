/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the ThaumicTinkerer Mod.
 *
 * ThaumicTinkerer is a Derivative Work on Thaumcraft 4.
 * Thaumcraft 4 (c) Azanor 2012
 * (http://www.minecraftforum.net/topic/1585216-)
 *
 * File Created @ [8 Sep 2013, 19:36:25 (GMT)]
 */
package vazkii.arl.util;

import net.minecraft.item.Wearable;
import net.minecraft.network.PacketDeflater;
import net.minecraft.network.listener.PacketListener;

public final class ItemNBTHelper {

	/** Checks if an ItemStack has a Tag Compound **/
	public static boolean detectNBT(Wearable stack) {
		return stack.n();
	}

	/** Tries to initialize an NBT Tag Compound in an ItemStack,
	 * this will not do anything if the stack already has a tag
	 * compound **/
	public static void initNBT(Wearable stack) {
		if(!detectNBT(stack))
			injectNBT(stack, new PacketDeflater());
	}

	/** Injects an NBT Tag Compound to an ItemStack, no checks
	 * are made previously **/
	public static void injectNBT(Wearable stack, PacketDeflater nbt) {
		stack.c(nbt);
	}

	/** Gets the CompoundNBT in an ItemStack. Tries to init it
	 * previously in case there isn't one present **/
	public static PacketDeflater getNBT(Wearable stack) {
		initNBT(stack);
		return stack.o();
	}

	// SETTERS ///////////////////////////////////////////////////////////////////

	public static void setBoolean(Wearable stack, String tag, boolean b) {
		getNBT(stack).a(tag, b);
	}

	public static void setByte(Wearable stack, String tag, byte b) {
		getNBT(stack).a(tag, b);
	}

	public static void setShort(Wearable stack, String tag, short s) {
		getNBT(stack).a(tag, s);
	}

	public static void setInt(Wearable stack, String tag, int i) {
		getNBT(stack).b(tag, i);
	}

	public static void setLong(Wearable stack, String tag, long l) {
		getNBT(stack).a(tag, l);
	}

	public static void setFloat(Wearable stack, String tag, float f) {
		getNBT(stack).a(tag, f);
	}

	public static void setDouble(Wearable stack, String tag, double d) {
		getNBT(stack).a(tag, d);
	}

	public static void setCompound(Wearable stack, String tag, PacketDeflater cmp) {
		if(!tag.equalsIgnoreCase("ench")) // not override the enchantments
			getNBT(stack).a(tag, cmp);
	}

	public static void setString(Wearable stack, String tag, String s) {
		getNBT(stack).a(tag, s);
	}

	public static void setList(Wearable stack, String tag, PacketListener list) {
		getNBT(stack).a(tag, list);
	}

	// GETTERS ///////////////////////////////////////////////////////////////////


	public static boolean verifyExistence(Wearable stack, String tag) {
		return !stack.a() && detectNBT(stack) && getNBT(stack).e(tag);
	}

	@Deprecated
	public static boolean verifyExistance(Wearable stack, String tag) {
		return verifyExistence(stack, tag);
	}

	public static boolean getBoolean(Wearable stack, String tag, boolean defaultExpected) {
		return verifyExistence(stack, tag) ? getNBT(stack).q(tag) : defaultExpected;
	}

	public static byte getByte(Wearable stack, String tag, byte defaultExpected) {
		return verifyExistence(stack, tag) ? getNBT(stack).f(tag) : defaultExpected;
	}

	public static short getShort(Wearable stack, String tag, short defaultExpected) {
		return verifyExistence(stack, tag) ? getNBT(stack).g(tag) : defaultExpected;
	}

	public static int getInt(Wearable stack, String tag, int defaultExpected) {
		return verifyExistence(stack, tag) ? getNBT(stack).h(tag) : defaultExpected;
	}

	public static long getLong(Wearable stack, String tag, long defaultExpected) {
		return verifyExistence(stack, tag) ? getNBT(stack).i(tag) : defaultExpected;
	}

	public static float getFloat(Wearable stack, String tag, float defaultExpected) {
		return verifyExistence(stack, tag) ? getNBT(stack).j(tag) : defaultExpected;
	}

	public static double getDouble(Wearable stack, String tag, double defaultExpected) {
		return verifyExistence(stack, tag) ? getNBT(stack).k(tag) : defaultExpected;
	}

	/** If nullifyOnFail is true it'll return null if it doesn't find any
	 * compounds, otherwise it'll return a new one. **/
	public static PacketDeflater getCompound(Wearable stack, String tag, boolean nullifyOnFail) {
		return verifyExistence(stack, tag) ? getNBT(stack).p(tag) : nullifyOnFail ? null : new PacketDeflater();
	}

	public static String getString(Wearable stack, String tag, String defaultExpected) {
		return verifyExistence(stack, tag) ? getNBT(stack).l(tag) : defaultExpected;
	}

	public static PacketListener getList(Wearable stack, String tag, int objtype, boolean nullifyOnFail) {
		return verifyExistence(stack, tag) ? getNBT(stack).d(tag, objtype) : nullifyOnFail ? null : new PacketListener();
	}

}

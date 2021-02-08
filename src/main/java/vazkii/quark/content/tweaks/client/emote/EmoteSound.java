/**
 * This class was created by <WireSegal>. It's distributed as
 * part of the Quark Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Quark
 * <p>
 * Quark is Open Source and distributed under the
 * CC-BY-NC-SA 3.0 License: https://creativecommons.org/licenses/by-nc-sa/3.0/deed.en_GB
 * <p>
 * File Created @ [May 05, 2019, 21:30 AM (EST)]
 */
package vazkii.quark.content.tweaks.client.emote;

import java.lang.ref.WeakReference;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ITickableSound;
import net.minecraft.client.audio.LocatableSound;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class EmoteSound extends LocatableSound implements ITickableSound {

	protected boolean donePlaying;

	private final WeakReference<PlayerEntity> player;
	private final EmoteTemplate template;
	private final boolean endWithSequence;

	public static void add(List<EmoteSound> allSounds, List<EmoteSound> sounds, PlayerEntity player, EmoteTemplate template,
						   ResourceLocation soundEvent, float volume, float pitch,
						   boolean repeating, boolean endWithSequence) {
		EmoteSound emoteSound = new EmoteSound(player, template, soundEvent, volume, pitch, repeating, endWithSequence);
		sounds.add(emoteSound);
		allSounds.add(emoteSound);
		Minecraft.getInstance().getSoundHandler().play(emoteSound);
	}

	public static void endAll(List<EmoteSound> sounds) {
		for (EmoteSound sound : sounds)
			sound.donePlaying = true;
	}

	public static void endSection(List<EmoteSound> sounds) {
		for (EmoteSound sound : sounds)
			if (sound.endWithSequence)
				sound.donePlaying = true;
	}

	public EmoteSound(PlayerEntity player, EmoteTemplate template, ResourceLocation sound, float volume, float pitch, boolean repeating, boolean endWithSequence) {
		super(sound, SoundCategory.PLAYERS);
		this.player = new WeakReference<>(player);
		this.template = template;
		this.endWithSequence = endWithSequence;

		this.volume = volume;
		this.pitch = pitch;

		if (repeating) {
			this.repeat = true;
			this.repeatDelay = 0;
		}
	}

	@Override
	public void tick() {
		PlayerEntity player = this.player.get();

		if (player == null || !player.isAlive())
			donePlaying = true;
		else {
			EmoteBase emote = EmoteHandler.getPlayerEmote(player);
			if (emote == null || emote.desc.template != template)
				donePlaying = true;
			else {
				Vector3d pos = player.getPositionVec();
				x = (float) pos.x;
				y = (float) pos.y;
				z = (float) pos.z;
			}
		}
	}

	@Override
	public boolean isDonePlaying() {
		return this.donePlaying;
	}
}

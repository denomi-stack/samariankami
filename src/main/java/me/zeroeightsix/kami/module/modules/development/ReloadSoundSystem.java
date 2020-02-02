package me.zeroeightsix.kami.module.modules.development;

import me.zeroeightsix.kami.module.Module;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.audio.SoundManager;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

@Module.Info(name = "ReloadSounds", category = Module.Category.development, description = "UsedToReloadSoundsIfNonePlay")
public class ReloadSoundSystem extends Module {
    private GuiScreen ReloadSoundSystem;

    public ReloadSoundSystem() {

    }

    public void onEnabled() {
        try {
            final SoundManager sndManager = (SoundManager) ObfuscationReflectionHelper.getPrivateValue((Class) SoundHandler.class, (Object) ReloadSoundSystem.mc.getSoundHandler(), new String[]{"sndManager", "sndManager"});
            sndManager.reloadSoundSystem();
        } catch (Exception e) {
            System.out.println("Could not restart sound manager: " + e.toString());
            e.printStackTrace();
            this.setEnabled(false);
        }
        {
            super.toggle();
        }
    }
}

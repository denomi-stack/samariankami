package me.zeroeightsix.kami.module.modules.development;

import me.zeroeightsix.kami.module.Module;

import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemStack;



@Module.Info(name = "CrystalCount", description = "CrystalCount", category = Module.Category.development)
public class CrystalCount extends Module {


    public static Object Info;
    public static java.lang.String String;
    public static int crystals;
    public boolean moving = false;
    public boolean returnI = false;
    private Setting<Boolean> soft = register(Settings.b("Soft"));


    @Override
    public void onUpdate() {
        if (mc.currentScreen instanceof GuiContainer) return;
        if (returnI) {
            int t = -1;
            for (int i = 0; i < 45; i++)
                if (mc.player.inventory.getStackInSlot(i).isEmpty) {
                    t = i;
                    break;
                }
            if (t == -1) return;
            mc.playerController.windowClick(0, t < 9 ? t + 36 : t, 0, ClickType.PICKUP, mc.player);
            returnI = false;
            super.toggle();
        }
        crystals = mc.player.inventory.mainInventory.stream().filter(itemStack -> itemStack.getItem() == Items.END_CRYSTAL).mapToInt(ItemStack::getCount).sum();

            }




    @Override
    public String getHudInfo() {
        return String.valueOf(crystals);
    }
    {


    }

    {

    }
}




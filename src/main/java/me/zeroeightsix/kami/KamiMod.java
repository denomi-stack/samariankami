package me.zeroeightsix.kami;

import me.zeroeightsix.kami.command.Command;
import me.zeroeightsix.kami.command.CommandManager;
import me.zeroeightsix.kami.command.commands.BindCommand;
import me.zeroeightsix.kami.event.ForgeEventProcessor;
import me.zeroeightsix.kami.event.events.DrawBlockBoundingBoxEvent;
import me.zeroeightsix.kami.gui.kami.KamiGUI;
import me.zeroeightsix.kami.gui.rgui.component.AlignedComponent;
import me.zeroeightsix.kami.gui.rgui.component.Component;
import me.zeroeightsix.kami.gui.rgui.component.container.use.Frame;
import me.zeroeightsix.kami.gui.rgui.util.ContainerHelper;
import me.zeroeightsix.kami.gui.rgui.util.Docking;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.module.ModuleManager;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import me.zeroeightsix.kami.setting.SettingsRegister;
import me.zeroeightsix.kami.setting.config.Configuration;
import me.zeroeightsix.kami.util.Friends;
import me.zeroeightsix.kami.util.LagCompensator;
import me.zeroeightsix.kami.util.Wrapper;
import com.google.common.base.Converter;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import me.zero.alpine.EventManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import java.io.*;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

/**
 * Created by 086 on 7/11/2017.
 */
@Mod(modid="kami", name="kami", version="b1")
public class KamiMod {
    public static final String MODID = "Denomi";
    public static final String MODNAME = "Samarian";
    public static final String MODVER = "b1";
    public static final String KAMI_HIRAGANA = "\u03a9";
    public static final String KAMI_KATAKANA = "\u03a9";
    public static final String KAMI_KANJI = "Samarian";
    private static final String KAMI_CONFIG_NAME_DEFAULT = "kamiConfig.json";
    public static final Logger log = LogManager.getLogger((String)"kami");
    public static final me.zero.alpine.EventBus EVENT_BUS = new EventManager();
    @Mod.Instance
    private static KamiMod INSTANCE;
    public KamiGUI guiManager;
    public CommandManager commandManager;
    private Setting<JsonObject> guiStateSetting = Settings.custom("gui", new JsonObject(), (Converter)new Converter<JsonObject, JsonObject>(){

        protected JsonObject doForward(JsonObject jsonObject) {
            return jsonObject;
        }

        protected JsonObject doBackward(JsonObject jsonObject) {
            return jsonObject;
        }
    }).buildAndRegister("");

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
    }

    @SubscribeEvent
    public void onRenderGui(RenderGameOverlayEvent.Post event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (event.getType() != RenderGameOverlayEvent.ElementType.HOTBAR) {
            return;
        }
        String playername = mc.player.getName();
        mc.fontRenderer.drawStringWithShadow(" " + "", 1.0f, 10.0f, 16711680);
        mc.fontRenderer.drawStringWithShadow("", 1.0f, 1.0f, 16711680);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        log.info("\n\nInitializing kami b1");
        ModuleManager.initialize();
        ModuleManager.getModules().stream().filter(module -> module.alwaysListening).forEach(EVENT_BUS::subscribe);
        MinecraftForge.EVENT_BUS.register((Object)this);
        MinecraftForge.EVENT_BUS.register((Object)new ForgeEventProcessor());
        LagCompensator.INSTANCE = new LagCompensator();
        Wrapper.init();
        this.guiManager = new KamiGUI();
        this.guiManager.initializeGUI();
        this.commandManager = new CommandManager();
        Friends.initFriends();
        SettingsRegister.register("commandPrefix", Command.commandPrefix);
        KamiMod.loadConfiguration();
        log.info("Settings loaded");
        ModuleManager.updateLookup();
        ModuleManager.getModules().stream().filter(Module::isEnabled).forEach(Module::enable);
        BindCommand.modifiersEnabled.setValue(false);
        log.info("kami Client Initialized!\n");
    }

    public static String getConfigName() {
        Path config = Paths.get("kamiConfigAce.txt", new String[0]);
        String kamiConfigName = KAMI_CONFIG_NAME_DEFAULT;
        try {
            try (BufferedReader reader = Files.newBufferedReader(config);){
                kamiConfigName = reader.readLine();
                if (!KamiMod.isFilenameValid(kamiConfigName)) {
                    kamiConfigName = KAMI_CONFIG_NAME_DEFAULT;
                }
            }
        }
        catch (NoSuchFileException e) {
            try {
                try (BufferedWriter writer = Files.newBufferedWriter(config, new OpenOption[0]);){
                    writer.write(KAMI_CONFIG_NAME_DEFAULT);
                }
            }
            catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return kamiConfigName;
    }

    public static void loadConfiguration() {
        try {
            KamiMod.loadConfigurationUnsafe();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void loadConfigurationUnsafe() throws IOException {
        String kamiConfigName = KamiMod.getConfigName();
        Path kamiConfig = Paths.get(kamiConfigName, new String[0]);
        if (!Files.exists(kamiConfig, new LinkOption[0])) {
            return;
        }
        Configuration.loadConfiguration(kamiConfig);
        JsonObject gui = KamiMod.INSTANCE.guiStateSetting.getValue();
        for (Map.Entry entry : gui.entrySet()) {
            Optional<Component> optional = KamiMod.INSTANCE.guiManager.getChildren().stream().filter(component -> component instanceof Frame).filter(component -> ((Frame)component).getTitle().equals(entry.getKey())).findFirst();
            if (optional.isPresent()) {
                JsonObject object = ((JsonElement)entry.getValue()).getAsJsonObject();
                Frame frame = (Frame)optional.get();
                frame.setX(object.get("x").getAsInt());
                frame.setY(object.get("y").getAsInt());
                Docking docking = Docking.values()[object.get("docking").getAsInt()];
                if (docking.isLeft()) {
                    ContainerHelper.setAlignment(frame, AlignedComponent.Alignment.LEFT);
                } else if (docking.isRight()) {
                    ContainerHelper.setAlignment(frame, AlignedComponent.Alignment.RIGHT);
                }
                frame.setDocking(docking);
                frame.setMinimized(object.get("minimized").getAsBoolean());
                frame.setPinned(object.get("pinned").getAsBoolean());
                continue;
            }
            System.err.println("Found GUI config entry for " + (String)entry.getKey() + ", but found no frame with that name");
        }
        KamiMod.getInstance().getGuiManager().getChildren().stream().filter(component -> component instanceof Frame && ((Frame)component).isPinneable() && component.isVisible()).forEach(component -> component.setOpacity(0.0f));
    }

    public static void saveConfiguration() {
        try {
            KamiMod.saveConfigurationUnsafe();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void onDrawBoundingBoxPost() {
        MinecraftForge.EVENT_BUS.post((Event)new DrawBlockBoundingBoxEvent.Post());
    }

    public static void saveConfigurationUnsafe() throws IOException {
        JsonObject object = new JsonObject();
        KamiMod.INSTANCE.guiManager.getChildren().stream().filter(component -> component instanceof Frame).map(component -> (Frame)component).forEach(frame -> {
            JsonObject frameObject = new JsonObject();
            frameObject.add("x", (JsonElement)new JsonPrimitive((Number)frame.getX()));
            frameObject.add("y", (JsonElement)new JsonPrimitive((Number)frame.getY()));
            frameObject.add("docking", (JsonElement)new JsonPrimitive((Number)Arrays.asList(Docking.values()).indexOf((Object)frame.getDocking())));
            frameObject.add("minimized", (JsonElement)new JsonPrimitive(Boolean.valueOf(frame.isMinimized())));
            frameObject.add("pinned", (JsonElement)new JsonPrimitive(Boolean.valueOf(frame.isPinned())));
            object.add(frame.getTitle(), (JsonElement)frameObject);
        });
        KamiMod.INSTANCE.guiStateSetting.setValue(object);
        Path outputFile = Paths.get(KamiMod.getConfigName(), new String[0]);
        if (!Files.exists(outputFile, new LinkOption[0])) {
            Files.createFile(outputFile, new FileAttribute[0]);
        }
        Configuration.saveConfiguration(outputFile);
        ModuleManager.getModules().forEach(Module::destroy);
    }

    public static boolean isFilenameValid(String file) {
        File f = new File(file);
        try {
            f.getCanonicalPath();
            return true;
        }
        catch (IOException e) {
            return false;
        }
    }

    public static KamiMod getInstance() {
        return INSTANCE;
    }

    public KamiGUI getGuiManager() {
        return this.guiManager;
    }

    public CommandManager getCommandManager() {
        return this.commandManager;
    } }


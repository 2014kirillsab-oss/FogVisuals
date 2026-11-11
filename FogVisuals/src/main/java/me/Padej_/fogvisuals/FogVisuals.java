package me.Padej_.fogvisuals;

import com.mojang.blaze3d.platform.InputConstants;
import me.Padej_.fogvisuals.config.FogConfig;
import me.Padej_.fogvisuals.fog.FogPreset;
import me.Padej_.fogvisuals.gui.FogMenuScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FogVisuals implements ClientModInitializer {
    public static final String MOD_ID = "fogvisuals";
    public static final Logger LOGGER = LoggerFactory.getLogger("FogVisuals");

    private static final KeyMapping.Category KEY_CATEGORY =
            KeyMapping.Category.register(Identifier.fromNamespaceAndPath(MOD_ID, "main"));

    public static final KeyMapping OPEN_GUI_KEY = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "key.fogvisuals.open_gui", InputConstants.KEY_O, KEY_CATEGORY));

    public static final KeyMapping CYCLE_PRESET_KEY = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "key.fogvisuals.cycle_preset", InputConstants.KEY_V, KEY_CATEGORY));

    @Override
    public void onInitializeClient() {
        me.Padej_.fogvisuals.hud.InfoHud.register();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            me.Padej_.fogvisuals.fx.VisualEffects.endClientTick(client);
            while (OPEN_GUI_KEY.consumeClick()) {
                if (client.player != null && client.gui.screen() == null) {
                    client.gui.setScreen(new FogMenuScreen(null));
                }
            }
            while (CYCLE_PRESET_KEY.consumeClick()) {
                if (client.player != null) {
                    FogConfig cfg = FogConfig.get();
                    FogPreset next = FogPreset.cycle(cfg.preset);
                    if (next == FogPreset.CUSTOM) {
                        cfg.enabled = false;
                        cfg.preset = next.name();
                        cfg.save();
                        if (cfg.showMessages) {
                            client.player.sendOverlayMessage(Component.translatable(
                                    "fogvisuals.preset.cleared",
                                    Component.translatable("fogvisuals.preset.custom")));
                        }
                    } else {
                        next.applyTo(cfg);
                        cfg.enabled = true;
                        cfg.save();
                        if (cfg.showMessages) {
                            client.player.sendOverlayMessage(Component.translatable(
                                    "fogvisuals.preset.set",
                                    Component.translatable("fogvisuals.preset." + next.name().toLowerCase())));
                        }
                    }
                }
            }
        });

        LOGGER.info("FogVisuals loaded - press O for settings, V to cycle presets");
    }
}

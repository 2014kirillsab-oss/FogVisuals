package me.Padej_.fogvisuals.hud;

import me.Padej_.fogvisuals.config.FogConfig;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

/**
 * FlyVisuals-style info HUD: FPS, ping, coordinates and in-game time
 * in a compact line at the top-left corner.
 */
public final class InfoHud implements HudElement {
    private static final Identifier ID = Identifier.fromNamespaceAndPath("fogvisuals", "info_hud");
    private static final InfoHud INSTANCE = new InfoHud();
    private static final int BG = 0x90101419;
    private static final int TEXT = 0xFFD7E0EC;
    private static final int ACCENT = 0xFF4DA6FF;

    private InfoHud() {
    }

    public static void register() {
        HudElementRegistry.addLast(ID, INSTANCE);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor gfx, DeltaTracker delta) {
        FogConfig cfg = FogConfig.get();
        if (!cfg.hudInfo) {
            return;
        }
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) {
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(client.getFps()).append(" FPS");

        if (client.getConnection() != null && client.getConnection().getPlayerInfo(client.player.getUUID()) != null) {
            sb.append("  ").append(client.getConnection().getPlayerInfo(client.player.getUUID()).getLatency()).append("ms");
        }

        if (client.level != null) {
            sb.append("  XYZ: ")
              .append(String.format("%.0f %.0f %.0f", client.player.getX(), client.player.getY(), client.player.getZ()));
            long dayTime = client.level.getOverworldClockTime() % 24000L;
            long hours = (dayTime / 1000L + 6) % 24;
            long minutes = (dayTime % 1000L) * 60 / 1000;
            sb.append("  ").append(String.format("%02d:%02d", hours, minutes));
        }

        String text = sb.toString();
        int x = 4;
        int y = 4;
        int w = client.font.width(text) + 8;
        gfx.fill(x, y, x + w, y + 12, BG);
        gfx.fill(x, y, x + 2, y + 12, ACCENT);
        gfx.text(client.font, text, x + 6, y + 3, TEXT);
    }
}

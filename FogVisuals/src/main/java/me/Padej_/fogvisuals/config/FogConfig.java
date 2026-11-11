package me.Padej_.fogvisuals.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Client config, stored as a simple JSON file in the config directory.
 */
public final class FogConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("fogvisuals.json");

    private static FogConfig instance;

    // Custom fog
    public boolean enabled = false;
    public int colorRed = 140;
    public int colorGreen = 170;
    public int colorBlue = 215;
    public int opacity = 60;          // 0..100, mapped to fog alpha
    public float startDistance = 4.0f;   // blocks
    public float endDistance = 32.0f;    // blocks

    // Biome-aware tinting
    public boolean biomeFog = false;
    public int biomeTint = 50;          // 0..100

    // Fog pulse ("breathing")
    public boolean pulse = false;
    public int pulseDepth = 35;         // 0..100
    public float pulseSpeed = 1.0f;     // 0.1..5.0

    // Clear Vision - pushes vanilla fog away entirely
    public boolean clearVision = false;
    public float clearDistance = 512.0f;

    // If false, vanilla water/lava/powder snow fog is left untouched
    public boolean replaceLiquidFog = true;

    public String preset = "MIST";

    // Click-GUI
    public String guiTheme = "BLUE";
    public boolean showMessages = true;
    public boolean autoSave = true;

    // Extra effects
    public boolean nightFog = false;      // fog thickens at night
    public boolean weatherBoost = false;  // fog thickens in rain/thunder
    public boolean healthTint = false;    // fog reddens at low health
    public int healthThreshold = 30;      // percent

    // Visuals (FlyVisuals-style)
    public boolean trail = false;         // particle trail behind the player
    public int trailDensity = 50;         // 0..100
    public boolean aura = false;          // particles orbiting the player
    public int auraDensity = 40;          // 0..100
    public boolean rainbow = false;       // animated fog color
    public boolean hudInfo = false;       // FPS / ping / XYZ / time overlay

    public static FogConfig get() {
        if (instance == null) {
            instance = load();
        }
        return instance;
    }

    public static FogConfig load() {
        try {
            if (Files.exists(PATH)) {
                FogConfig read = GSON.fromJson(Files.readString(PATH), FogConfig.class);
                if (read != null) {
                    return read;
                }
            }
        } catch (Exception e) {
            System.err.println("[FogVisuals] Failed to read config, using defaults: " + e);
        }
        return new FogConfig();
    }

    public void save() {
        try {
            Path tmp = PATH.resolveSibling(PATH.getFileName() + ".tmp");
            Files.writeString(tmp, GSON.toJson(this));
            Files.move(tmp, PATH, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            System.err.println("[FogVisuals] Failed to save config: " + e);
        }
    }

    /** Restores every field to its default value. */
    public void resetToDefaults() {
        FogConfig def = new FogConfig();
        enabled = def.enabled;
        colorRed = def.colorRed;
        colorGreen = def.colorGreen;
        colorBlue = def.colorBlue;
        opacity = def.opacity;
        startDistance = def.startDistance;
        endDistance = def.endDistance;
        biomeFog = def.biomeFog;
        biomeTint = def.biomeTint;
        pulse = def.pulse;
        pulseDepth = def.pulseDepth;
        pulseSpeed = def.pulseSpeed;
        clearVision = def.clearVision;
        clearDistance = def.clearDistance;
        replaceLiquidFog = def.replaceLiquidFog;
        preset = def.preset;
        guiTheme = def.guiTheme;
        showMessages = def.showMessages;
        autoSave = def.autoSave;
        nightFog = def.nightFog;
        weatherBoost = def.weatherBoost;
        healthTint = def.healthTint;
        healthThreshold = def.healthThreshold;
        trail = def.trail;
        trailDensity = def.trailDensity;
        aura = def.aura;
        auraDensity = def.auraDensity;
        rainbow = def.rainbow;
        hudInfo = def.hudInfo;
    }
}

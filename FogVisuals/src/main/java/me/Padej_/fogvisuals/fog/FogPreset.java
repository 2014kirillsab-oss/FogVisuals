package me.Padej_.fogvisuals.fog;

import me.Padej_.fogvisuals.config.FogConfig;

/**
 * Ready-made fog looks. Cycling applies the look to the config;
 * {@link #CUSTOM} keeps whatever the user tuned by hand.
 */
public enum FogPreset {
    CUSTOM(0, 0.0f, 0.0f, 0),
    MIST(0xBFC9D4, 4.0f, 48.0f, 45),
    DENSE(0x9AA7B0, 0.0f, 16.0f, 85),
    UNDERWATER(0x1A4FA8, 0.0f, 24.0f, 90),
    INFERNO(0xE23C18, 1.0f, 40.0f, 80),
    TOXIC(0x59E31E, 2.0f, 36.0f, 75),
    BLOOD_MOON(0x8C0F0F, 4.0f, 64.0f, 60),
    VOID(0x05030C, 8.0f, 48.0f, 95),
    CANDY(0xFF9AD5, 4.0f, 56.0f, 55);

    private final int color;
    private final float start;
    private final float end;
    private final int opacity;

    FogPreset(int color, float start, float end, int opacity) {
        this.color = color;
        this.start = start;
        this.end = end;
        this.opacity = opacity;
    }

    public int color() {
        return color;
    }

    public float start() {
        return start;
    }

    public float end() {
        return end;
    }

    public int opacity() {
        return opacity;
    }

    public void applyTo(FogConfig config) {
        if (this == CUSTOM) {
            return;
        }
        config.colorRed = (color >> 16) & 0xFF;
        config.colorGreen = (color >> 8) & 0xFF;
        config.colorBlue = color & 0xFF;
        config.startDistance = start;
        config.endDistance = end;
        config.opacity = opacity;
        config.preset = name();
    }

    public static FogPreset byName(String name) {
        for (FogPreset value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return MIST;
    }

    public static FogPreset cycle(String currentName) {
        FogPreset current = byName(currentName);
        FogPreset[] values = values();
        return values[(current.ordinal() + 1) % values.length];
    }
}

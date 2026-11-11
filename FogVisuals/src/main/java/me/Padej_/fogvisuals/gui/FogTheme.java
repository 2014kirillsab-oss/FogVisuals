package me.Padej_.fogvisuals.gui;

/**
 * Accent color themes for the click-GUI.
 */
public enum FogTheme {
    BLUE(0xFF3FD0FF, 0xFF1E3E52),
    PURPLE(0xFFB58CFF, 0xFF3A2E5C),
    RED(0xFFFF5C7A, 0xFF521E2A),
    GREEN(0xFF5CE8A4, 0xFF1E4634),
    GOLD(0xFFFFC94D, 0xFF4E3C1E),
    CYAN(0xFF4DE8E8, 0xFF1E4A4A),
    PINK(0xFFFF6EC7, 0xFF4A1E3C);

    private final int accent;
    private final int dim;

    FogTheme(int accent, int dim) {
        this.accent = accent;
        this.dim = dim;
    }

    public int accent() {
        return accent;
    }

    public int dim() {
        return dim;
    }

    public static FogTheme byName(String name) {
        for (FogTheme theme : values()) {
            if (theme.name().equalsIgnoreCase(name)) {
                return theme;
            }
        }
        return BLUE;
    }
}

package me.Padej_.fogvisuals.gui;

import com.mojang.blaze3d.platform.InputConstants;
import me.Padej_.fogvisuals.config.FogConfig;
import me.Padej_.fogvisuals.fog.FogPreset;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.DoubleConsumer;

/**
 * Click-GUI that is a literal copy of the FlyVisuals menu photo
 * (rail: logo / Всё / swords / heart / cup / person / Fly****+FunTime badge;
 * list: FPS Booster, Crosshair Mod, Fly Health, Fly HitBox, Fly****;
 * content: Crosshair header with pill + Предпросмотр, Общие настройки,
 * Режим [Simple|Presets|Custom], Рисование креста pixel grid,
 * Нарисуй свой крест PKM rows, Сохраненные кресты slots).
 * Only the brand text is FogVisuals.
 */
public class FogMenuScreen extends Screen {
    private static final int RAIL_W = 60;
    private static final int LIST_W = 96;

    private static final int BG = 0xF70A0D12;
    private static final int BG_RAIL = 0xFF070910;
    private static final int TILE = 0xFF1E242E;
    private static final int CARD = 0xFF13171E;
    private static final int CARD_HOVER = 0xFF191F28;
    private static final int FIELD = 0xFF10141B;
    private static final int ACCENT = 0xFF3F8CFF;
    private static final int RED = 0xFFE5484D;
    private static final int TEXT_MAIN = 0xFFF2F5F9;
    private static final int TEXT_MED = 0xFFB9C2D0;
    private static final int TEXT_DIM = 0xFF6F7B92;
    private static final int TRACK = 0xFF242B37;
    private static final int GRID_LINE = 0xFF161B22;
    private static final int GRID_BG = 0xFF0C0F15;
    private static final int PIXEL = 0xFFE9EEF4;

    private enum Tab { ALL, WORLD, EFFECTS, THEMES, CONFIG }

    private static final int[] TAB_COLORS = {
            ACCENT, 0xFF5CE8A4, 0xFFFFB84D, 0xFFB58CFF, 0xFF9AA7B8
    };

    private int panelX;
    private int panelY;
    private int panelW;
    private int panelH;

    private Tab tab = Tab.ALL;
    private String selectedModule = "crosshair";
    private String selectedKey = "";
    private String search = "";

    /** Session-only saved slots (slot 0 = live config). */
    private final List<FogConfig> slots = new ArrayList<>();

    private boolean dragPanel;
    private int dragOffX;
    private int dragOffY;
    private SliderHit dragSlider;

    private final List<Hit> hits = new ArrayList<>();
    private final List<SliderHit> sliders = new ArrayList<>();
    /** 14x14 pixel editor, like the photo. */
    private static final int GRID_N = 14;
    private final boolean[][] grid = new boolean[GRID_N][GRID_N];

    public FogMenuScreen(Screen parent) {
        super(Component.literal("FogVisuals"));
        // crosshair pattern, like the photo
        int[][] seed = {
                {6, 1}, {7, 1}, {6, 2}, {7, 2},
                {3, 4}, {4, 4}, {5, 4}, {6, 4}, {7, 4}, {8, 4}, {9, 4}, {10, 4},
                {3, 5}, {4, 5}, {5, 5}, {6, 5}, {7, 5}, {8, 5}, {9, 5}, {10, 5},
                {3, 6}, {9, 6},
                {5, 8}, {6, 8}, {7, 8}, {8, 8}
        };
        for (int[] s : seed) {
            if (s[0] >= 0 && s[0] < GRID_N && s[1] >= 0 && s[1] < GRID_N) {
                grid[s[0]][s[1]] = true;
            }
        }
        slots.add(null); // slot 0 = live
    }

    @Override
    protected void init() {
        panelW = this.width - 24;
        panelH = this.height - 20;
        panelX = 12;
        panelY = 10;
    }

    // ============================= frame =============================

    @Override
    public void extractRenderState(GuiGraphicsExtractor gfx, int mouseX, int mouseY, float partialTick) {
        hits.clear();
        sliders.clear();

        this.extractBackground(gfx, mouseX, mouseY, partialTick);

        FogConfig cfg = FogConfig.get();

        fillRounded(gfx, panelX, panelY, panelX + panelW, panelY + panelH, BG);
        fillRoundedLeft(gfx, panelX, panelY, panelX + RAIL_W, panelY + panelH, BG_RAIL);

        renderRail(gfx, mouseX, mouseY, cfg);
        renderList(gfx, mouseX, mouseY, cfg);
        renderContent(gfx, mouseX, mouseY, cfg);
    }

    // ============================= rail =============================

    private void renderRail(GuiGraphicsExtractor gfx, int mouseX, int mouseY, FogConfig cfg) {
        // logo with F
        int lx = panelX + 8;
        int ly = panelY + 7;
        fillRounded(gfx, lx, ly, lx + 22, ly + 22, ACCENT);
        gfx.fill(lx + 9, ly + 6, lx + 11, ly + 17, 0xFFFFFFFF);
        gfx.fill(lx + 9, ly + 6, lx + 17, ly + 8, 0xFFFFFFFF);
        gfx.fill(lx + 9, ly + 10, lx + 15, ly + 12, 0xFFFFFFFF);
        hits.add(new Hit(panelX, panelY, panelX + RAIL_W, panelY + 32, () -> tab = Tab.ALL));

        // five tiles + locked row, exactly like the photo
        int top = panelY + 36;
        int step = 24;
        for (int i = 0; i < 5; i++) {
            boolean active = tab.ordinal() == i;
            int tx = panelX + 6;
            int tw = RAIL_W - 12;
            int ty = top + i * step;
            int th = step - 5;
            int hov = hover(mouseX, mouseY, panelX, ty - 2, panelX + RAIL_W, ty + th + 2);
            if (active) {
                fillRounded(gfx, tx, ty, tx + tw, ty + th, TILE);
            } else if (hov != 0) {
                fillRounded(gfx, tx, ty, tx + tw, ty + th, 0xFF12161D);
            }
            drawGlyph(gfx, i, tx + tw / 2 - 5, ty + th / 2 - 6, active ? 0xFFEDF1F7 : TEXT_DIM);
            if (active) {
                String label = "Всё";
                gfx.text(this.font, label, tx + (tw - this.font.width(label)) / 2, ty + th + 1, TEXT_DIM);
            }
            final int idx = i;
            hits.add(new Hit(panelX, ty - 2, panelX + RAIL_W, ty + th + 2, () -> {
                tab = Tab.values()[idx];
                search = "";
            }));
        }

        // "Taret Hud" row at the rail bottom, like the photo
        int lockY = top + 5 * step + 2;
        int taretY = lockY + 26;
        gfx.fill(panelX + 6, taretY + 2, panelX + 14, taretY + 4, 0xFF6F7B92);
        gfx.fill(panelX + 6, taretY + 6, panelX + 14, taretY + 8, 0xFF6F7B92);
        gfx.text(this.font, "Taret Hud", panelX + 17, taretY + 3, 0xFF9AA6BC);

        int dy = panelY + panelH - 16;
        fillRounded(gfx, panelX + 17, dy, panelX + 27, dy + 10, cfg.enabled ? 0xFF3FE07F : 0xFF3A4150);
    }

    /** Glyphs matching the photo rail: grid, swords, heart, cup, person, lock. */
    private static void drawGlyph(GuiGraphicsExtractor gfx, int kind, int x, int y, int color) {
        switch (kind) {
            case 0 -> { // grid of 4
                gfx.fill(x, y, x + 4, y + 4, color);
                gfx.fill(x + 6, y, x + 10, y + 4, color);
                gfx.fill(x, y + 6, x + 4, y + 10, color);
                gfx.fill(x + 6, y + 6, x + 10, y + 10, color);
            }
            case 1 -> { // crossed swords (X)
                gfx.fill(x, y, x + 2, y + 2, color);
                gfx.fill(x + 2, y + 2, x + 4, y + 4, color);
                gfx.fill(x + 4, y + 4, x + 6, y + 6, color);
                gfx.fill(x + 6, y + 6, x + 8, y + 8, color);
                gfx.fill(x + 8, y + 8, x + 10, y + 10, color);
                gfx.fill(x + 8, y, x + 10, y + 2, color);
                gfx.fill(x + 6, y + 2, x + 8, y + 4, color);
                gfx.fill(x + 2, y + 6, x + 4, y + 8, color);
                gfx.fill(x, y + 8, x + 2, y + 10, color);
            }
            case 2 -> { // heart
                gfx.fill(x, y + 1, x + 4, y + 4, color);
                gfx.fill(x + 6, y + 1, x + 10, y + 4, color);
                gfx.fill(x, y + 4, x + 10, y + 7, color);
                gfx.fill(x + 2, y + 7, x + 8, y + 9, color);
                gfx.fill(x + 4, y + 9, x + 6, y + 10, color);
            }
            case 3 -> { // cup
                gfx.fill(x + 1, y, x + 9, y + 2, color);
                gfx.fill(x + 2, y + 2, x + 8, y + 7, color);
                gfx.fill(x + 3, y + 7, x + 7, y + 9, color);
                gfx.fill(x + 4, y + 9, x + 6, y + 10, color);
                gfx.fill(x + 9, y + 2, x + 11, y + 5, color);
            }
            case 4 -> { // person
                fillRoundedStatic(gfx, x + 3, y, x + 7, y + 4, color);
                fillRoundedStatic(gfx, x + 1, y + 5, x + 9, y + 10, color);
            }
            case 6 -> { // chart bars
                gfx.fill(x + 1, y + 6, x + 3, y + 10, color);
                gfx.fill(x + 4, y + 3, x + 6, y + 10, color);
                gfx.fill(x + 7, y, x + 9, y + 10, color);
            }
            case 7 -> { // crosshair
                gfx.fill(x + 4, y, x + 6, y + 10, color);
                gfx.fill(x, y + 4, x + 10, y + 6, color);
                gfx.fill(x + 4, y + 4, x + 6, y + 6, BG_RAIL);
            }
            case 8 -> { // pulse line
                gfx.fill(x, y + 5, x + 2, y + 7, color);
                gfx.fill(x + 2, y + 3, x + 4, y + 5, color);
                gfx.fill(x + 4, y + 2, x + 6, y + 9, color);
                gfx.fill(x + 6, y + 6, x + 8, y + 8, color);
                gfx.fill(x + 8, y + 4, x + 10, y + 6, color);
            }
            case 9 -> { // palette
                fillRoundedStatic(gfx, x, y + 1, x + 10, y + 11, color);
                gfx.fill(x + 4, y + 5, x + 6, y + 7, BG_RAIL);
            }
            case 10 -> { // save box
                gfx.fill(x, y, x + 10, y + 10, color);
                gfx.fill(x + 2, y + 2, x + 8, y + 5, BG_RAIL);
                gfx.fill(x + 3, y + 6, x + 7, y + 8, BG_RAIL);
            }
            default -> { // lock
                gfx.fill(x + 3, y, x + 5, y + 3, color);
                gfx.fill(x + 5, y + 3, x + 7, y, color);
                gfx.fill(x, y + 3, x + 10, y + 10, color);
                gfx.fill(x + 4, y + 6, x + 6, y + 9, BG_RAIL);
            }
        }
    }

    // ============================= module list =============================

    private record ListItem(String id, String label, boolean locked, int icon) { }

    private List<ListItem> listItems() {
        List<ListItem> items = new ArrayList<>();
        if (tab == Tab.ALL) {
            items.add(new ListItem("fps", "FPS Booster", false, 6));
            items.add(new ListItem("crosshair", "Crosshair Mod", false, 7));
            items.add(new ListItem("trail", "Trail", false, 8));
            items.add(new ListItem("aura", "Aura", false, 9));
            items.add(new ListItem("rainbow", "Rainbow", false, 9));
            items.add(new ListItem("hud", "HUD Info", false, 10));
            items.add(new ListItem("locked", "Locked", true, 5));
        } else {
            switch (tab) {
                case WORLD -> {
                    items.add(new ListItem("enabled", "Свой туман", false, 7));
                    items.add(new ListItem("density", "Плотность", false, 6));
                    items.add(new ListItem("start", "Начало", false, 6));
                    items.add(new ListItem("end", "Конец", false, 6));
                    items.add(new ListItem("liquid", "Туман в жидкостях", false, 7));
                    items.add(new ListItem("night", "Ночной туман", false, 1));
                    items.add(new ListItem("weather", "Погода", false, 3));
                }
                case EFFECTS -> {
                    items.add(new ListItem("biome", "Биомный оттенок", false, 2));
                    items.add(new ListItem("biomeTint", "Сила биома", false, 6));
                    items.add(new ListItem("pulse", "Пульсация", false, 2));
                    items.add(new ListItem("pulseDepth", "Глубина пульса", false, 6));
                    items.add(new ListItem("pulseSpeed", "Скорость", false, 6));
                    items.add(new ListItem("clear", "Чистый горизонт", false, 2));
                    items.add(new ListItem("clearDist", "Дистанция", false, 6));
                    items.add(new ListItem("healthTint", "Тинт здоровья", false, 2));
                    items.add(new ListItem("healthThr", "Порог HP", false, 6));
                    items.add(new ListItem("trail", "След (Trail)", false, 8));
                    items.add(new ListItem("trailDensity", "Плотность следа", false, 6));
                    items.add(new ListItem("aura", "Аура", false, 9));
                    items.add(new ListItem("auraDensity", "Плотность ауры", false, 6));
                    items.add(new ListItem("rainbow", "Радужный туман", false, 9));
                }
                case THEMES -> {
                    for (FogTheme t : FogTheme.values()) {
                        items.add(new ListItem(t.name(), themeLabel(t), false, 9));
                    }
                }
                case CONFIG -> {
                    items.add(new ListItem("messages", "Сообщения в игре", false, 7));
                    items.add(new ListItem("autoSave", "Автосохранение", false, 10));
                    items.add(new ListItem("hud", "HUD Info", false, 10));
                    items.add(new ListItem("reset", "Сбросить всё", false, 5));
                    items.add(new ListItem("save", "Сохранить конфиг", false, 10));
                }
                default -> { }
            }
        }
        if (!search.isBlank()) {
            String q = search.toLowerCase(Locale.ROOT);
            items.removeIf(e -> !e.label().toLowerCase(Locale.ROOT).contains(q));
        }
        return items;
    }

    private static String themeLabel(FogTheme t) {
        return switch (t) {
            case BLUE -> "Blue";
            case PURPLE -> "Purple";
            case RED -> "Red";
            case GREEN -> "Green";
            case GOLD -> "Gold";
            case CYAN -> "Cyan";
            case PINK -> "Pink";
        };
    }

    private void renderList(GuiGraphicsExtractor gfx, int mouseX, int mouseY, FogConfig cfg) {
        int x = panelX + RAIL_W;
        int w = LIST_W;

        gfx.text(this.font, "FogVisuals", x + 6, panelY + 7, 0xFF9AA6BC);

        int sy = panelY + 20;
        fillRounded(gfx, x + 4, sy, x + w - 18, sy + 13, FIELD);
        drawSearchGlyph(gfx, x + 9, sy + 4);
        gfx.text(this.font, search.isEmpty() ? "Поиск..." : search, x + 18, sy + 3,
                search.isEmpty() ? TEXT_DIM : TEXT_MAIN);
        hits.add(new Hit(x + 4, sy, x + w - 18, sy + 13, () -> { }));

        int gx = x + w - 15;
        fillRounded(gfx, gx, sy, gx + 13, sy + 13, FIELD);
        drawGlyph(gfx, 5, gx + 2, sy + 2, TEXT_DIM);
        hits.add(new Hit(gx, sy, gx + 13, sy + 13, () -> {
            tab = Tab.CONFIG;
            search = "";
        }));

        List<ListItem> items = listItems();
        int ry = sy + 18;
        int rowH = 16;
        for (ListItem it : items) {
            if (ry + rowH > panelY + panelH - 12) break;
            boolean selected = !it.locked() && isSelectedItem(it.id());
            int hov = hover(mouseX, mouseY, x + 2, ry, x + w - 2, ry + rowH - 1);
            if (selected) {
                fillRounded(gfx, x + 2, ry, x + w - 2, ry + rowH - 1, TILE);
            } else if (hov != 0 && !it.locked()) {
                fillRounded(gfx, x + 2, ry, x + w - 2, ry + rowH - 1, CARD_HOVER);
            }
            int iconC = it.locked() ? 0xFF2A303A : (selected ? ACCENT : 0xFF5A6472);
            drawGlyph(gfx, it.icon(), x + 6, ry + 3, iconC);
            gfx.text(this.font, clip(it.label(), this.font, w - 32), x + 19, ry + 4,
                    it.locked() ? 0xFF4A5260 : (selected ? TEXT_MAIN : TEXT_MED));

            if (!it.locked()) {
                final ListItem fi = it;
                hits.add(new Hit(x + 2, ry, x + w - 2, ry + rowH - 1, () -> {
                    if (tab == Tab.ALL) {
                        selectedModule = fi.id();
                    } else {
                        selectedKey = fi.id();
                        clickCategoryEntry(fi.id(), cfg);
                    }
                }));
            }
            ry += rowH;
        }
    }

    private boolean isSelectedItem(String id) {
        if (tab == Tab.ALL) {
            return id.equals(selectedModule);
        }
        if (id.equals(selectedKey)) {
            return true;
        }
        if (selectedKey.isEmpty()) {
            List<ListItem> all = listItems();
            return !all.isEmpty() && all.get(0).id().equals(id);
        }
        return false;
    }

    private void clickCategoryEntry(String id, FogConfig cfg) {
        switch (id) {
            case "enabled" -> { cfg.enabled = !cfg.enabled; cfg.save(); }
            case "liquid" -> { cfg.replaceLiquidFog = !cfg.replaceLiquidFog; cfg.save(); }
            case "biome" -> { cfg.biomeFog = !cfg.biomeFog; cfg.save(); }
            case "pulse" -> { cfg.pulse = !cfg.pulse; cfg.save(); }
            case "clear" -> { cfg.clearVision = !cfg.clearVision; cfg.save(); }
            case "messages" -> { cfg.showMessages = !cfg.showMessages; cfg.save(); }
            case "autoSave" -> { cfg.autoSave = !cfg.autoSave; cfg.save(); }
            case "night" -> { cfg.nightFog = !cfg.nightFog; cfg.save(); }
            case "weather" -> { cfg.weatherBoost = !cfg.weatherBoost; cfg.save(); }
            case "healthTint" -> { cfg.healthTint = !cfg.healthTint; cfg.save(); }
            case "trail" -> { cfg.trail = !cfg.trail; cfg.save(); }
            case "aura" -> { cfg.aura = !cfg.aura; cfg.save(); }
            case "rainbow" -> { cfg.rainbow = !cfg.rainbow; cfg.save(); }
            case "hud" -> { cfg.hudInfo = !cfg.hudInfo; cfg.save(); }
            case "reset" -> { cfg.resetToDefaults(); cfg.save(); }
            case "save" -> cfg.save();
            default -> { }
        }
    }

    // ============================= content =============================

    private void renderContent(GuiGraphicsExtractor gfx, int mouseX, int mouseY, FogConfig cfg) {
        int x = panelX + RAIL_W + LIST_W;
        int w = panelX + panelW - x;
        if (w < 60) {
            return;
        }
        if (tab == Tab.ALL) {
            renderAllPage(gfx, mouseX, mouseY, x, w, cfg);
        } else {
            renderCategoryPage(gfx, mouseX, mouseY, x, w, cfg);
        }
    }

    /** The exact photo page. */
    private void renderAllPage(GuiGraphicsExtractor gfx, int mouseX, int mouseY, int x, int w, FogConfig cfg) {
        boolean crosshairSelected = selectedModule.equals("crosshair");
        String title = switch (selectedModule) {
            case "fps" -> "FPS Booster";
            case "trail" -> "Trail";
            case "aura" -> "Aura";
            case "rainbow" -> "Rainbow";
            case "hud" -> "HUD Info";
            default -> "Crosshair";
        };

        // header: crosshair chip + title + pill + "⤢ Предпросмотр" at far right
        int hy = panelY + 6;
        int cx0 = x + 1;
        fillRounded(gfx, cx0, hy + 1, cx0 + 9, hy + 10, ACCENT);
        gfx.fill(cx0 + 3, hy + 1, cx0 + 5, hy + 10, BG);
        gfx.fill(cx0, hy + 4, cx0 + 9, hy + 6, BG);
        gfx.fill(cx0 + 4, hy + 5, cx0 + 4, hy + 5, ACCENT);
        gfx.text(this.font, title, x + 13, hy, TEXT_MAIN);

        // "Предпросмотр" button with expand icon, at the far right like the photo
        String prev = "Предпросмотр";
        int pw = this.font.width(prev) + 20;
        int pbx = x + w - 2 - pw;
        fillRounded(gfx, pbx, hy - 2, pbx + pw, hy + 11, CARD);
        gfx.fill(pbx + 6, hy + 2, pbx + 8, hy + 6, TEXT_MED);   // ⤢ icon
        gfx.fill(pbx + 6, hy + 6, pbx + 9, hy + 7, TEXT_MED);
        gfx.text(this.font, prev, pbx + 12, hy + 2, TEXT_MED);
        hits.add(new Hit(pbx, hy - 2, pbx + pw, hy + 11, () -> {
            FogPreset next = FogPreset.cycle(cfg.preset);
            next.applyTo(cfg);
            cfg.enabled = next != FogPreset.CUSTOM;
            cfg.save();
        }));

        // pill toggle sits left of the button, like the photo
        drawPill(gfx, pbx - 22, hy, cfg.enabled, ACCENT);
        hits.add(new Hit(pbx - 24, hy - 2, pbx - 2, hy + 11, () -> {
            cfg.enabled = !cfg.enabled;
            cfg.save();
        }));

        if (!crosshairSelected) {
            drawModuleSettings(gfx, x, w, panelY + 24, cfg);
            return;
        }

        // ---- section ----
        int by = panelY + 24;
        drawHouse(gfx, x + 3, by + 1);
        gfx.text(this.font, "Общие настройки", x + 15, by, TEXT_MAIN);
        by += 14;

        // ---- "Режим" + [Simple | Presets | Custom] ----
        gfx.text(this.font, "Режим", x + 2, by + 4, TEXT_MAIN);
        String s0 = "Simple";
        String s1 = "Presets";
        String s2 = "Custom";
        int w0 = this.font.width(s0) + 12;
        int w1 = this.font.width(s1) + 12;
        int w2 = this.font.width(s2) + 12;
        int segW = w0 + w1 + w2 + 6;
        int segH = 15;
        int segX = x + w - 2 - segW;
        int seg = cfg.enabled ? (cfg.biomeFog ? 2 : 1) : 0;
        fillRounded(gfx, segX, by, segX + segW, by + segH, CARD);
        int ax = seg == 0 ? segX + 1 : (seg == 1 ? segX + 1 + w0 + 2 : segX + 1 + w0 + 2 + w1 + 2);
        int aw = seg == 0 ? w0 : (seg == 1 ? w1 : w2);
        String at = seg == 0 ? s0 : (seg == 1 ? s1 : s2);
        fillRounded(gfx, ax, by + 1, ax + aw, by + segH - 1, ACCENT);
        gfx.text(this.font, at, ax + (aw - this.font.width(at)) / 2, by + 4, 0xFFFFFFFF);
        int ix1 = seg == 0 ? segX + 1 + w0 + 2 : segX + 1;
        gfx.text(this.font, seg == 0 ? s1 : s0, ix1 + 6, by + 4, TEXT_DIM);
        int ix2 = seg == 2 ? segX + 1 + w0 + 2 : segX + 1 + w0 + 2 + w1 + 2;
        gfx.text(this.font, seg == 2 ? s1 : s2, ix2 + 6, by + 4, TEXT_DIM);
        hits.add(new Hit(segX, by, segX + w0 + 2, by + segH, () -> {
            cfg.enabled = false;
            cfg.save();
        }));
        hits.add(new Hit(segX + w0 + 2, by, segX + w0 + 2 + w1 + 2, by + segH, () -> {
            cfg.biomeFog = false;
            FogPreset.byName(cfg.preset).applyTo(cfg);
            cfg.enabled = true;
            cfg.save();
        }));
        hits.add(new Hit(segX + w0 + 2 + w1 + 2, by, segX + segW, by + segH, () -> {
            cfg.enabled = true;
            cfg.biomeFog = true;
            cfg.save();
        }));
        by += segH + 6;

        // ---- left: Рисование креста + icons right after the title ----
        int leftW = Math.min(160, w / 2 - 6);
        gfx.text(this.font, "Рисование креста", x + 2, by, TEXT_MAIN);
        int iconX = x + 2 + this.font.width("Рисование креста") + 6;
        drawUndo(gfx, iconX, by);
        hits.add(new Hit(iconX - 2, by - 2, iconX + 10, by + 10, () -> {
            FogPreset.byName(cfg.preset).applyTo(cfg);
            cfg.save();
        }));
        drawDownload(gfx, iconX + 12, by);
        hits.add(new Hit(iconX + 10, by - 2, iconX + 22, by + 10, cfg::save));

        int gy = by + 12;
        int cell = Math.max(5, (leftW - 4) / GRID_N);
        int gridW = cell * GRID_N + 4;
        fillRounded(gfx, x + 2, gy, x + 2 + gridW, gy + gridW, CARD);
        // grid lines
        for (int i = 0; i <= GRID_N; i++) {
            gfx.fill(x + 3 + i * cell, gy + 2, x + 3 + i * cell + 1, gy + gridW - 2, GRID_LINE);
            gfx.fill(x + 3, gy + 2 + i * cell, x + gridW - 1, gy + 3 + i * cell, GRID_LINE);
        }
        // cells
        for (int r = 0; r < GRID_N; r++) {
            for (int c = 0; c < GRID_N; c++) {
                int cx = x + 4 + c * cell;
                int cy = gy + 3 + r * cell;
                if (grid[c][r]) {
                    gfx.fill(cx, cy, cx + cell - 1, cy + cell - 1, PIXEL);
                }
                final int cc = c;
                final int rr = r;
                hits.add(new Hit(cx, cy, cx + cell, cy + cell, () -> grid[cc][rr] = !grid[cc][rr]));
            }
        }

        // ---- right: Нарисуй свой крест + PKM rows + saved slots ----
        int rx = x + 2 + gridW + 12;
        int rw = x + w - 2 - rx;
        if (rw > 70) {
            gfx.text(this.font, "Нарисуй свой крест", rx, by, TEXT_MAIN);

            int rowY = by + 13;
            rowY = pkmRow(gfx, rx, rw, rowY, "Нарисовать пиксель", () -> {
                grid[GRID_N / 2][GRID_N / 2] = true;
            });
            rowY = pkmRow(gfx, rx, rw, rowY, "Удалить пиксель", () -> {
                grid[GRID_N / 2][GRID_N / 2] = false;
            });

            int sh = rowY + 20;
            gfx.text(this.font, clip("Сохраненные кресты  (1-8 из 16)", this.font, rw - 30), rx, sh, TEXT_MAIN);
            gfx.text(this.font, "Далее >", rx + rw - this.font.width("Далее >"), sh, TEXT_MED);

            int sy = sh + 11;
            int slotN = 8;
            int slotSize = Math.min(17, (rw - (slotN - 1) * 3) / slotN);
            for (int i = 0; i < slotN; i++) {
                int sx = rx + i * (slotSize + 3);
                if (i == 0) {
                    fillRounded(gfx, sx, sy, sx + slotSize, sy + slotSize, ACCENT);
                    int cx = sx + slotSize / 2;
                    int cy = sy + slotSize / 2;
                    // dotted grid glyph, like the photo's active slot
                    gfx.fill(cx - 4, cy - 4, cx - 1, cy - 1, 0xFFFFFFFF);
                    gfx.fill(cx + 1, cy - 4, cx + 4, cy - 1, 0xFFFFFFFF);
                    gfx.fill(cx - 4, cy + 1, cx - 1, cy + 4, 0xFFFFFFFF);
                    gfx.fill(cx + 1, cy + 1, cx + 4, cy + 4, 0xFFFFFFFF);
                    gfx.fill(cx - 1, cy - 1, cx + 1, cy + 1, 0xFFFFFFFF);
                } else if (i == 1) {
                    fillRounded(gfx, sx, sy, sx + slotSize, sy + slotSize, RED);
                    // trash glyph, like the photo
                    int cx = sx + slotSize / 2;
                    gfx.fill(cx - 4, sy + 5, cx + 4, sy + 7, 0xFFFFFFFF);
                    gfx.fill(cx - 3, sy + 8, cx + 3, sy + 12, 0xFFFFFFFF);
                    gfx.fill(cx - 1, sy + 4, cx + 1, sy + 5, 0xFFFFFFFF);
                } else {
                    fillRounded(gfx, sx, sy, sx + slotSize, sy + slotSize, CARD);
                }
                final int idx = i;
                hits.add(new Hit(sx, sy, sx + slotSize, sy + slotSize, () -> clickSlot(idx, cfg)));
            }
        }
    }

    private void clickSlot(int idx, FogConfig cfg) {
        if (idx == 0) {
            return;
        }
        if (idx == 1) {
            cfg.resetToDefaults();
            cfg.save();
            return;
        }
        while (slots.size() <= idx) {
            slots.add(null);
        }
        if (slots.get(idx) == null) {
            slots.set(idx, copyOf(cfg));
        } else {
            applyCopy(slots.get(idx), cfg);
            cfg.save();
        }
    }

    private static FogConfig copyOf(FogConfig c) {
        FogConfig n = new FogConfig();
        applyCopy(c, n);
        return n;
    }

    private static void applyCopy(FogConfig from, FogConfig to) {
        to.enabled = from.enabled;
        to.colorRed = from.colorRed;
        to.colorGreen = from.colorGreen;
        to.colorBlue = from.colorBlue;
        to.opacity = from.opacity;
        to.startDistance = from.startDistance;
        to.endDistance = from.endDistance;
        to.biomeFog = from.biomeFog;
        to.biomeTint = from.biomeTint;
        to.pulse = from.pulse;
        to.pulseDepth = from.pulseDepth;
        to.pulseSpeed = from.pulseSpeed;
        to.clearVision = from.clearVision;
        to.clearDistance = from.clearDistance;
        to.replaceLiquidFog = from.replaceLiquidFog;
        to.nightFog = from.nightFog;
        to.weatherBoost = from.weatherBoost;
        to.healthTint = from.healthTint;
        to.healthThreshold = from.healthThreshold;
        to.trail = from.trail;
        to.trailDensity = from.trailDensity;
        to.aura = from.aura;
        to.auraDensity = from.auraDensity;
        to.rainbow = from.rainbow;
        to.hudInfo = from.hudInfo;
        to.preset = from.preset;
        to.guiTheme = from.guiTheme;
        to.showMessages = from.showMessages;
    }

    /** Settings for the other ALL-tab modules. */
    private void drawModuleSettings(GuiGraphicsExtractor gfx, int x, int w, int y, FogConfig cfg) {
        int ry = y + 2;
        switch (selectedModule) {
            case "fps" -> {
                gfx.text(this.font, "Меньше затрат на рендер тумана — выше FPS",
                        x + 2, ry + 4, TEXT_DIM);
            }
            case "health" -> {
                ry = toggleCard(gfx, x, w, ry, "Тинт здоровья", cfg.healthTint,
                        () -> clickCategoryEntry("healthTint", cfg));
                sliderCard(gfx, x, w, ry, "Порог HP", cfg.healthThreshold, 5, 90,
                        v -> cfg.healthThreshold = (int) Math.round(v));
            }
            case "weather" -> {
                ry = toggleCard(gfx, x, w, ry, "Погода", cfg.weatherBoost,
                        () -> clickCategoryEntry("weather", cfg));
                gfx.text(this.font, "В дождь и грозу туман густеет и приближается",
                        x + 2, ry + 4, TEXT_DIM);
            }
            case "pulse" -> {
                ry = toggleCard(gfx, x, w, ry, "Пульсация", cfg.pulse,
                        () -> clickCategoryEntry("pulse", cfg));
                ry = sliderCard(gfx, x, w, ry, "Глубина пульса", cfg.pulseDepth, 0, 100,
                        v -> cfg.pulseDepth = (int) Math.round(v));
                sliderCard(gfx, x, w, ry, "Скорость", cfg.pulseSpeed, 0.1, 5,
                        v -> cfg.pulseSpeed = (float) v);
            }
            case "clear" -> {
                ry = toggleCard(gfx, x, w, ry, "Чистый горизонт", cfg.clearVision,
                        () -> clickCategoryEntry("clear", cfg));
                sliderCard(gfx, x, w, ry, "Дистанция", cfg.clearDistance, 64, 1024,
                        v -> cfg.clearDistance = (float) v);
            }
            case "trail" -> {
                ry = toggleCard(gfx, x, w, ry, "След", cfg.trail,
                        () -> clickCategoryEntry("trail", cfg));
                sliderCard(gfx, x, w, ry, "Плотность", cfg.trailDensity, 0, 100,
                        v -> cfg.trailDensity = (int) Math.round(v));
            }
            case "aura" -> {
                ry = toggleCard(gfx, x, w, ry, "Аура", cfg.aura,
                        () -> clickCategoryEntry("aura", cfg));
                sliderCard(gfx, x, w, ry, "Плотность", cfg.auraDensity, 0, 100,
                        v -> cfg.auraDensity = (int) Math.round(v));
            }
            case "rainbow" -> {
                toggleCard(gfx, x, w, ry, "Радужный туман", cfg.rainbow,
                        () -> clickCategoryEntry("rainbow", cfg));
            }
            case "hud" -> {
                toggleCard(gfx, x, w, ry, "HUD Info", cfg.hudInfo,
                        () -> clickCategoryEntry("hud", cfg));
            }
            case "hitbox" -> {
                gfx.text(this.font, "Скоро: подсветка хитбоксов сквозь туман",
                        x + 2, ry + 4, TEXT_DIM);
            }
            default -> { }
        }
    }

    /** Category tabs: selected entry card body. */
    private void renderCategoryPage(GuiGraphicsExtractor gfx, int mouseX, int mouseY, int x, int w, FogConfig cfg) {
        List<ListItem> items = listItems();
        ListItem sel = items.stream().filter(it -> !it.locked() && isSelectedItem(it.id()))
                .findFirst().orElse(items.isEmpty() ? null : items.get(0));

        String title = sel != null ? sel.label()
                : "FogVisuals";

        int hy = panelY + 6;
        fillRounded(gfx, x + 2, hy + 1, x + 9, hy + 8, TAB_COLORS[tab.ordinal()]);
        gfx.text(this.font, title, x + 13, hy, TEXT_MAIN);

        if (tab == Tab.WORLD) {
            drawPill(gfx, x + w - 24, hy, cfg.enabled, ACCENT);
            hits.add(new Hit(x + w - 26, hy - 2, x + w - 4, hy + 11, () -> {
                cfg.enabled = !cfg.enabled;
                cfg.save();
            }));
        }

        int by = panelY + 24;
        drawHouse(gfx, x + 3, by + 1);
        gfx.text(this.font, "Общие настройки", x + 15, by, TEXT_MAIN);
        by += 14;
        if (sel == null) {
            return;
        }
        String id = sel.id();
        int ry = by + 2;
        switch (id) {
            case "density" -> sliderCard(gfx, x, w, ry, "Плотность", cfg.opacity, 0, 100,
                    v -> cfg.opacity = (int) Math.round(v));
            case "start" -> sliderCard(gfx, x, w, ry, "Начало", cfg.startDistance, 0, 128,
                    v -> cfg.startDistance = (float) v);
            case "end" -> sliderCard(gfx, x, w, ry, "Конец", cfg.endDistance, 2, 256,
                    v -> cfg.endDistance = (float) v);
            case "biomeTint" -> sliderCard(gfx, x, w, ry, "Сила биома", cfg.biomeTint, 0, 100,
                    v -> cfg.biomeTint = (int) Math.round(v));
            case "pulseDepth" -> sliderCard(gfx, x, w, ry, "Глубина пульса", cfg.pulseDepth, 0, 100,
                    v -> cfg.pulseDepth = (int) Math.round(v));
            case "pulseSpeed" -> sliderCard(gfx, x, w, ry, "Скорость", cfg.pulseSpeed, 0.1, 5,
                    v -> cfg.pulseSpeed = (float) v);
            case "clearDist" -> sliderCard(gfx, x, w, ry, "Дистанция", cfg.clearDistance, 64, 1024,
                    v -> cfg.clearDistance = (float) v);
            case "healthThr" -> sliderCard(gfx, x, w, ry, "Порог HP", cfg.healthThreshold, 5, 90,
                    v -> cfg.healthThreshold = (int) Math.round(v));
            case "trailDensity" -> sliderCard(gfx, x, w, ry, "Плотность следа", cfg.trailDensity, 0, 100,
                    v -> cfg.trailDensity = (int) Math.round(v));
            case "auraDensity" -> sliderCard(gfx, x, w, ry, "Плотность ауры", cfg.auraDensity, 0, 100,
                    v -> cfg.auraDensity = (int) Math.round(v));
            case "enabled", "liquid", "biome", "pulse", "clear", "messages", "autoSave", "night", "weather", "healthTint",
                 "trail", "aura", "rainbow", "hud" -> {
                boolean val = switch (id) {
                    case "enabled" -> cfg.enabled;
                    case "liquid" -> cfg.replaceLiquidFog;
                    case "biome" -> cfg.biomeFog;
                    case "pulse" -> cfg.pulse;
                    case "clear" -> cfg.clearVision;
                    case "autoSave" -> cfg.autoSave;
                    case "night" -> cfg.nightFog;
                    case "weather" -> cfg.weatherBoost;
                    case "healthTint" -> cfg.healthTint;
                    case "trail" -> cfg.trail;
                    case "aura" -> cfg.aura;
                    case "rainbow" -> cfg.rainbow;
                    case "hud" -> cfg.hudInfo;
                    default -> cfg.showMessages;
                };
                toggleCard(gfx, x, w, ry, sel.label(), val, () -> clickCategoryEntry(id, cfg));
            }
            case "reset" -> {
                fillRounded(gfx, x + 2, ry, x + w - 2, ry + 16, CARD);
                String center = clip("Сбросить всё", this.font, w - 16);
                gfx.text(this.font, center, x + (w - this.font.width(center)) / 2, ry + 4, 0xFFFF8A8A);
                hits.add(new Hit(x + 2, ry, x + w - 2, ry + 16, () -> {
                    cfg.resetToDefaults();
                    cfg.save();
                }));
            }
            case "save" -> {
                fillRounded(gfx, x + 2, ry, x + w - 2, ry + 16, CARD);
                String center = clip("Сохранить конфиг", this.font, w - 16);
                gfx.text(this.font, center, x + (w - this.font.width(center)) / 2, ry + 4, 0xFF9BE8A8);
                hits.add(new Hit(x + 2, ry, x + w - 2, ry + 16, cfg::save));
            }
            default -> {
                try {
                    FogTheme t = FogTheme.valueOf(id);
                    boolean selectedT = t == FogTheme.byName(cfg.guiTheme);
                    fillRounded(gfx, x + 2, ry, x + w - 2, ry + 16, selectedT ? 0xFF1A2330 : CARD);
                    fillRounded(gfx, x + 8, ry + 5, x + 15, ry + 12, t.accent());
                    gfx.text(this.font, "Применить", x + 20, ry + 4, selectedT ? t.accent() : TEXT_MED);
                    hits.add(new Hit(x + 2, ry, x + w - 2, ry + 16, () -> {
                        cfg.guiTheme = t.name();
                        cfg.save();
                    }));
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
    }

    // ============================= small widgets =============================

    private int pkmRow(GuiGraphicsExtractor gfx, int x, int w, int y, String label, Runnable action) {
        String pkm = "ПКМ";
        int bw = this.font.width(pkm) + 16;
        fillRounded(gfx, x, y, x + bw, y + 11, CARD);
        // keyboard icon inside the badge, like the photo
        gfx.fill(x + 3, y + 3, x + 9, y + 8, 0xFF5A6472);
        gfx.fill(x + 4, y + 5, x + 8, y + 6, CARD);
        gfx.text(this.font, pkm, x + 12, y + 2, TEXT_MED);
        gfx.text(this.font, clip(label, this.font, w - bw - 10), x + bw + 6, y + 2, TEXT_MAIN);
        hits.add(new Hit(x, y, x + w, y + 13, action));
        return y + 16;
    }

    private int toggleCard(GuiGraphicsExtractor gfx, int x, int w, int y, String label, boolean value, Runnable onToggle) {
        fillRounded(gfx, x + 2, y, x + w - 2, y + 18, CARD);
        gfx.text(this.font, label, x + 8, y + 6, TEXT_MAIN);
        drawPill(gfx, x + w - 28, y + 5, value, ACCENT);
        hits.add(new Hit(x + 2, y, x + w - 2, y + 18, onToggle));
        return y + 23;
    }

    private static void drawUndo(GuiGraphicsExtractor gfx, int x, int y) {
        fillRoundedStatic(gfx, x, y + 2, x + 9, y + 8, TEXT_DIM);
        gfx.fill(x + 2, y, x + 5, y + 2, TEXT_DIM);
        gfx.fill(x + 3, y + 3, x + 5, y + 5, BG);
    }

    private static void drawDownload(GuiGraphicsExtractor gfx, int x, int y) {
        gfx.fill(x + 3, y, x + 5, y + 5, TEXT_DIM);
        gfx.fill(x + 1, y + 4, x + 7, y + 6, TEXT_DIM);
        gfx.fill(x + 1, y + 7, x + 7, y + 9, TEXT_DIM);
    }

    private static void drawSearchGlyph(GuiGraphicsExtractor gfx, int x, int y) {
        fillRoundedStatic(gfx, x, y, x + 6, y + 6, 0xFF6F7B92);
        gfx.fill(x + 5, y + 5, x + 7, y + 7, 0xFF6F7B92);
        gfx.fill(x + 7, y + 7, x + 8, y + 8, 0xFF6F7B92);
    }

    private static void drawHouse(GuiGraphicsExtractor gfx, int x, int y) {
        gfx.fill(x + 1, y, x + 2, y + 1, ACCENT);
        gfx.fill(x, y + 1, x + 3, y + 2, ACCENT);
        gfx.fill(x + 1, y + 2, x + 7, y + 3, ACCENT);
        gfx.fill(x + 2, y + 3, x + 6, y + 6, ACCENT);
        gfx.fill(x + 3, y + 5, x + 5, y + 7, ACCENT);
    }

    private void drawPill(GuiGraphicsExtractor gfx, int x, int y, boolean on, int accent) {
        fillRounded(gfx, x, y, x + 18, y + 9, on ? accent : 0xFF2A303C);
        int kx = on ? x + 10 : x + 1;
        fillRounded(gfx, kx, y + 1, kx + 7, y + 8, 0xFFF4F7FB);
    }

    private int sliderCard(GuiGraphicsExtractor gfx, int x, int w, int y,
                           String label, double cur, double min, double max, DoubleConsumer setter) {
        fillRounded(gfx, x + 2, y, x + w - 2, y + 26, CARD);
        gfx.text(this.font, label, x + 8, y + 3, TEXT_MED);
        String val = max <= 5 ? String.format(Locale.ROOT, "%.1f", cur) : String.valueOf((int) Math.round(cur));
        gfx.text(this.font, val, x + w - 8 - this.font.width(val), y + 3, TEXT_MAIN);
        int ty = y + 17;
        fillRounded(gfx, x + 8, ty, x + w - 8, ty + 4, TRACK);
        double frac = Math.max(0.0, Math.min(1.0, (cur - min) / (max - min)));
        int fillEnd = x + 8 + Math.max(3, (int) ((w - 16) * frac));
        fillRounded(gfx, x + 8, ty, fillEnd, ty + 4, ACCENT);
        fillRounded(gfx, fillEnd - 3, ty - 2, fillEnd + 4, ty + 6, 0xFFF4F7FB);
        sliders.add(new SliderHit(x + 8, ty - 4, x + w - 8, ty + 7, min, max, setter));
        return y + 30;
    }

    // ============================= interaction =============================

    @Override
    public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent event, boolean doubleClick) {
        int mx = (int) event.x();
        int my = (int) event.y();
        if (event.button() == 0) {
            for (Hit hit : hits) {
                if (hit.contains(mx, my)) {
                    hit.action.run();
                    return true;
                }
            }
            for (SliderHit slider : sliders) {
                if (slider.contains(mx, my)) {
                    dragSlider = slider;
                    applySlider(slider, mx);
                    return true;
                }
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(net.minecraft.client.input.MouseButtonEvent event, double dx, double dy) {
        int mx = (int) event.x();
        int my = (int) event.y();
        if (dragPanel) {
            panelX = Math.max(2, Math.min(this.width - panelW - 2, mx - dragOffX));
            panelY = Math.max(2, Math.min(this.height - panelH - 2, my - dragOffY));
            return true;
        }
        if (dragSlider != null) {
            applySlider(dragSlider, mx);
            return true;
        }
        return super.mouseDragged(event, dx, dy);
    }

    @Override
    public boolean mouseReleased(net.minecraft.client.input.MouseButtonEvent event) {
        dragPanel = false;
        dragSlider = null;
        return super.mouseReleased(event);
    }

    @Override
    public boolean keyPressed(net.minecraft.client.input.KeyEvent event) {
        if (event.keycode() == InputConstants.KEY_ESCAPE) {
            onClose();
            return true;
        }
        if (event.keycode() == 259 && !search.isEmpty()) {
            search = search.substring(0, search.length() - 1);
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean charTyped(net.minecraft.client.input.CharacterEvent event) {
        if (event.isAllowedChatCharacter()) {
            search += event.codepointAsString();
            if (search.length() > 24) {
                search = search.substring(0, 24);
            }
            return true;
        }
        return super.charTyped(event);
    }

    @Override
    public void onClose() {
        FogConfig.get().save();
        super.onClose();
    }

    // ============================= helpers =============================

    private void applySlider(SliderHit slider, int mouseX) {
        double frac = (mouseX - slider.x1) / (double) (slider.x2 - slider.x1);
        frac = Math.max(0.0, Math.min(1.0, frac));
        slider.set.accept(slider.min + frac * (slider.max - slider.min));
    }

    private static int hover(int mx, int my, int x1, int y1, int x2, int y2) {
        return mx >= x1 && mx < x2 && my >= y1 && my < y2 ? 1 : 0;
    }

    private static void fillRounded(GuiGraphicsExtractor gfx, int x1, int y1, int x2, int y2, int color) {
        fillRoundedStatic(gfx, x1, y1, x2, y2, color);
    }

    private static void fillRoundedStatic(GuiGraphicsExtractor gfx, int x1, int y1, int x2, int y2, int color) {
        if (x2 - x1 < 5 || y2 - y1 < 5) {
            gfx.fill(x1, y1, x2, y2, color);
            return;
        }
        gfx.fill(x1 + 2, y1, x2 - 2, y2, color);
        gfx.fill(x1, y1 + 2, x2, y2 - 2, color);
        gfx.fill(x1 + 1, y1 + 1, x2 - 1, y2 - 1, color);
    }

    private static void fillRoundedLeft(GuiGraphicsExtractor gfx, int x1, int y1, int x2, int y2, int color) {
        if (x2 - x1 < 5 || y2 - y1 < 5) {
            gfx.fill(x1, y1, x2, y2, color);
            return;
        }
        gfx.fill(x1, y1, x2 - 2, y2, color);
        gfx.fill(x1, y1 + 2, x2, y2 - 2, color);
        gfx.fill(x1, y1 + 1, x2 - 1, y2 - 1, color);
    }

    private static String clip(String s, Font font, int maxWidth) {
        if (font.width(s) <= maxWidth) {
            return s;
        }
        while (s.length() > 1 && font.width(s + "...") > maxWidth) {
            s = s.substring(0, s.length() - 1);
        }
        return s + "...";
    }

    private record Hit(int x1, int y1, int x2, int y2, Runnable action) {
        boolean contains(int x, int y) {
            return x >= x1 && x < x2 && y >= y1 && y < y2;
        }
    }

    private record SliderHit(int x1, int y1, int x2, int y2, double min, double max, DoubleConsumer set) {
        boolean contains(int x, int y) {
            return x >= x1 && x < x2 && y >= y1 && y < y2;
        }
    }
}

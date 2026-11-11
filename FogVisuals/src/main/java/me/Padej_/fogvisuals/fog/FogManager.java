package me.Padej_.fogvisuals.fog;

import me.Padej_.fogvisuals.config.FogConfig;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import org.joml.Vector4f;

/**
 * Turns the config into actual fog values written into the vanilla {@link FogData}
 * after the game has computed its own fog for the frame.
 * All FogData fields are public in 26.3, so no accessors are needed.
 */
public final class FogManager {
    private FogManager() {
    }

    public static void applyFog(FogData data, Camera camera, ClientLevel level, float partialTicks) {
        FogConfig cfg = FogConfig.get();

        boolean inLiquid = isEyeInLiquid(camera);
        if (inLiquid && !cfg.replaceLiquidFog) {
            return; // leave vanilla water/lava/powder snow fog alone
        }

        if (cfg.clearVision) {
            pushFogAway(data, cfg.clearDistance);
            return;
        }

        if (!cfg.enabled) {
            return; // fully vanilla fog
        }

        float end = Math.max(cfg.endDistance, 1.0f);
        float start = Mth.clamp(cfg.startDistance, 0.0f, end);
        float alpha = Mth.clamp(cfg.opacity / 100.0f, 0.0f, 1.0f);

        if (cfg.pulse) {
            float time = level.getGameTime() + partialTicks;
            float wave = 0.5f + 0.5f * Mth.sin(time * 0.06f * cfg.pulseSpeed);
            float depth = Mth.clamp(cfg.pulseDepth / 100.0f, 0.0f, 1.0f);
            float factor = 1.0f - depth * wave;
            end *= factor;
            alpha *= 1.0f - depth * wave * 0.5f;
        }

        Vector4f color = computeColor(data, cfg, level, camera.blockPosition());

        // rainbow: hue of the fog color slowly cycles
        if (cfg.rainbow) {
            float hue = (System.currentTimeMillis() % 20000L) / 20000.0f;
            int rgb = Mth.hsvToRgb(hue, 0.85f, 0.9f);
            float strength = 0.75f;
            color.x = Mth.lerp(strength, color.x, ((rgb >> 16) & 0xFF) / 255.0f);
            color.y = Mth.lerp(strength, color.y, ((rgb >> 8) & 0xFF) / 255.0f);
            color.z = Mth.lerp(strength, color.z, (rgb & 0xFF) / 255.0f);
        }

        color.w = alpha;

        // night fog: thicker and shorter range as the sky darkens
        if (cfg.nightFog) {
            float night = Mth.clamp(level.getSkyDarken() / 15.0f, 0.0f, 1.0f);
            if (night > 0.0f) {
                end *= 1.0f - 0.35f * night;
                alpha = Mth.clamp(alpha * (1.0f + 0.3f * night), 0.0f, 1.0f);
            }
        }

        // weather boost: rain/thunder thickens the fog
        if (cfg.weatherBoost) {
            float rain = level.getRainLevel(partialTicks);
            if (rain > 0.0f) {
                end *= 1.0f - 0.3f * rain;
                alpha = Mth.clamp(alpha * (1.0f + 0.35f * rain), 0.0f, 1.0f);
            }
        }

        // health tint: fog reddens when the player is low on health
        if (cfg.healthTint) {
            Entity entity = camera.entity();
            if (entity instanceof net.minecraft.client.player.LocalPlayer player) {
                float maxHp = Math.max(player.getMaxHealth(), 1.0f);
                float hpPct = player.getHealth() / maxHp * 100.0f;
                float threshold = Mth.clamp(cfg.healthThreshold, 1.0f, 99.0f);
                if (hpPct < threshold) {
                    float strength = 1.0f - hpPct / threshold;
                    color.x = Mth.lerp(strength * 0.8f, color.x, 1.0f);
                    color.y *= 1.0f - strength * 0.55f;
                    color.z *= 1.0f - strength * 0.55f;
                }
            }
        }

        color.w = alpha;

        data.color = color;
        data.environmentalStart = start;
        data.environmentalEnd = end;
        data.renderDistanceStart = start;
        data.renderDistanceEnd = end;
        data.skyEnd = end * 2.0f;
        data.cloudEnd = end * 2.0f;
    }

    private static Vector4f computeColor(FogData vanilla, FogConfig cfg, ClientLevel level, BlockPos pos) {
        Vector4f result;

        if (cfg.biomeFog) {
            // Blend the vanilla (already time-of-day aware) fog color towards
            // the biome's water color - gives every biome its own mood.
            int biomeColor = BiomeColors.getAverageWaterColor(level, pos);
            float br = (biomeColor >> 16 & 0xFF) / 255.0f;
            float bg = (biomeColor >> 8 & 0xFF) / 255.0f;
            float bb = (biomeColor & 0xFF) / 255.0f;

            float strength = Mth.clamp(cfg.biomeTint / 100.0f, 0.0f, 1.0f);
            Vector4f v = new Vector4f(vanilla.color);
            result = new Vector4f(
                    Mth.lerp(strength, v.x, br),
                    Mth.lerp(strength, v.y, bg),
                    Mth.lerp(strength, v.z, bb),
                    v.w);
        } else {
            float r = Mth.clamp(cfg.colorRed / 255.0f, 0.0f, 1.0f);
            float g = Mth.clamp(cfg.colorGreen / 255.0f, 0.0f, 1.0f);
            float b = Mth.clamp(cfg.colorBlue / 255.0f, 0.0f, 1.0f);
            result = new Vector4f(r, g, b, 1.0f);
        }

        // Nights should not glow: darken the fog color with the sky darkening value.
        float brighten = 1.0f - level.getSkyDarken() / 15.0f * 0.6f;
        result.x *= brighten;
        result.y *= brighten;
        result.z *= brighten;
        return result;
    }

    private static void pushFogAway(FogData data, float distance) {
        data.environmentalStart = distance;
        data.environmentalEnd = distance * 1.25f;
        data.renderDistanceStart = distance;
        data.renderDistanceEnd = distance * 1.25f;
        data.skyEnd = distance * 1.5f;
        data.cloudEnd = distance * 1.5f;
    }

    private static boolean isEyeInLiquid(Camera camera) {
        Entity entity = camera.entity();
        return entity != null
                && (entity.isEyeInFluid(FluidTags.WATER)
                || entity.isEyeInFluid(FluidTags.LAVA));
    }
}

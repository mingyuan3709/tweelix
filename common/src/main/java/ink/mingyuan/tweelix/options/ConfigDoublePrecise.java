package ink.mingyuan.tweelix.options;

import fi.dy.masa.malilib.config.options.ConfigDouble;
import net.minecraft.util.Mth;

/**
 * 固定精度的 {@link ConfigDouble} 子类，保存/加载时自动四舍五入到指定位数。
 * <p>
 * 解决 MaLiLib 原生 ConfigDouble 显示过多小数位的问题（如 {@code 0.46511627906976744}）。
 */
public class ConfigDoublePrecise extends ConfigDouble {

    private final int decimalPlaces;
    private final double multiplier;

    /**
     * @param decimalPlaces 保留的小数位数（4 = 保留 4 位）
     * @see ConfigDouble#ConfigDouble(String, double, double, double, boolean, String)
     */
    public ConfigDoublePrecise(String name, double defaultValue, double minValue, double maxValue,
                               boolean useSlider, String comment, int decimalPlaces) {
        super(name, defaultValue, minValue, maxValue, useSlider, comment, name);
        this.decimalPlaces = decimalPlaces;
        this.multiplier = Math.pow(10, decimalPlaces);
    }

    @Override
    protected double getClampedValue(double value) {
        double clamped = Mth.clamp(value, this.getMinDoubleValue(), this.getMaxDoubleValue());
        return Math.round(clamped * this.multiplier) / this.multiplier;
    }

    @Override
    public ConfigDoublePrecise apply(String translationPrefix) {
        return (ConfigDoublePrecise) super.apply(translationPrefix);
    }
}

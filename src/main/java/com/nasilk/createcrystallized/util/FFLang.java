package com.nasilk.createcrystallized.util;

import com.nasilk.createcrystallized.CreateCrystallized;
import net.createmod.catnip.lang.Lang;
import net.createmod.catnip.lang.LangBuilder;
import net.createmod.catnip.lang.LangNumberFormat;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

@SuppressWarnings("unused")
public class FFLang {

    public static LangBuilder builder() {
        return Lang.builder(CreateCrystallized.MOD_ID);
    }

    public static LangBuilder text(final String text) {
        return builder().text(text);
    }

    public static LangBuilder translate(final String key, final Object... args) {
        return builder().translate(key, args);
    }

    public static LangBuilder number(final double number) {
        return builder().text(LangNumberFormat.format(number));
    }

    public static LangBuilder space() {
        return builder().space();
    }

    public static void emptyLine(final List<Component> tooltip) {
        builder().text("").forGoggles(tooltip);
    }

    public static LangBuilder blockName(final BlockState blockState) {
        return builder().add(blockState.getBlock().getName());
    }

    public static LangBuilder kilopixelGram(final double value) {
        return translate("unit.kpg", String.format("%,.2f", value));
    }

    public static LangBuilder kilopixelGram(final double value, final String format) {
        return getPrefixedUnit("pg", value, format,1);
    }

    public static LangBuilder pixelNewton(final double value) {
        return pixelNewton(value, "%,.2f");
    }

    public static LangBuilder pixelNewton(final double value, final String format) {
        return getPrefixedUnit("pn", value, format,0);
    }

    private static LangBuilder getPrefixedUnit(String unit, double value, final String format, final int offset) {
        final String[] prefixes = {"k", "m", "g"};
        int index = offset-1;
        while (value >= 1000 && index < prefixes.length - 1) {
            value /= 1000;
            index++;
        }
        if(index >= 0)
            unit = prefixes[index] + unit;

        return translate("unit." + unit, format.formatted(value));
    }
}

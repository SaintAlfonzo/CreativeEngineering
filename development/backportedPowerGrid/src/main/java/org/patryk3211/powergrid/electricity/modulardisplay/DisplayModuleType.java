// Modified for the Forge 1.19.2 / Create 0.5.1 port (backportedPowerGrid, 2026); changes listed in PORT_NOTES.md.
package org.patryk3211.powergrid.electricity.modulardisplay;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.INamedIconOptions;
import com.simibubi.create.foundation.gui.AllIcons;
import org.patryk3211.powergrid.utility.Lang;

import java.util.function.IntFunction;

public enum DisplayModuleType implements INamedIconOptions {
    ZERO_TO_NINE(0, "powergrid.gui.modular_display.0 - 9", "zerotonine", 80f, 9f, AllIcons.I_NONE),
    NINE_TO_ZERO(1, "powergrid.gui.modular_display.9 - 0", "ninetozero", 80f, 9f, AllIcons.I_NONE),
    ONE_TO_ZERO(2, "powergrid.gui.modular_display.1 - 0", "onetozero", 80f, 9f, AllIcons.I_NONE),
    HEXADECIMAL(3, "powergrid.gui.modular_display.hexadecimal", "zerotof", 112f, 15f, AllIcons.I_NONE),
    SYMBOLS(4, "powergrid.gui.modular_display.symbols", "symbols", 80f, 8f, AllIcons.I_NONE),
    ALPHABET(5, "powergrid.gui.modular_display.alphabet", "alphabet", 176f, 25f, AllIcons.I_NONE);

    // Port: 1.20 ByIdMap.continuous(..., ZERO) written out (ids are the ordinals)
    private static final IntFunction<DisplayModuleType> BY_ID = id -> id >= 0 && id < values().length ? values()[id] : values()[0];
    private final int id;

    private final String name;
    private final String displayTexture;
    private final AllIcons icon;
    private final float spriteWidth;
    private final float characterCount;

    DisplayModuleType(int id, String name, String displayTexture, float spriteWidth, float characterCount, AllIcons icon) {
        this.id = id;
        this.name = name;
        this.displayTexture = displayTexture;
        this.spriteWidth = spriteWidth;
        this.characterCount = characterCount;
        this.icon = icon;
    }

    public int getId() {
        return this.id;
    }

    public String getDisplayTexture() {
        return this.displayTexture;
    }

    public float getSpriteWidth() {
        return this.spriteWidth;
    }

    public float getCharacterCount() {
        return this.characterCount;
    }

    public static DisplayModuleType byId(int moduleId) {
        return BY_ID.apply(moduleId);
    }

    // Port: upstream resolves the lang text when the enum loads and returns that text as the key. Here the field holds
    // the lang key itself; ModularDisplayBlockEntity's option board translates it (Create 0.5.1's option formatter
    // would prefix "create.").
    @Override
    public String getTranslationKey() {
        return name;
    }

    @Override
    public AllIcons getIcon() {
        return icon;
    }
}
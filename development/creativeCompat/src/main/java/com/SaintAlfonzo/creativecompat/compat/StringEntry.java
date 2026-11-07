package com.SaintAlfonzo.creativecompat.compat;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.config.ui.ConfigTextField;
import com.simibubi.create.foundation.config.ui.entries.ValueEntry;
import com.simibubi.create.foundation.gui.Theme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraftforge.common.ForgeConfigSpec;

/**
 * The GUI widget Create's own Configs screen is missing for a plain {@code ConfigValue<String>}
 * (see {@code SubMenuConfigScreenMixin}, which is what actually gets this class constructed
 * instead of Create's "Impl missing" placeholder row).
 *
 * <p>Modeled directly on Create's own {@code NumberEntry} (same {@link ValueEntry} base +
 * {@link ConfigTextField}), just without the numeric parsing/bounds machinery a raw string
 * doesn't need: no min/max, and the "parser" is the identity function. {@code ValueEntry}
 * already provides the row label, the reset-to-default button, and change-tracking (the toml
 * is only touched on Save, same as every other entry type) - this class only wires up the text
 * field itself.
 *
 * <p><b>Naming note:</b> {@code create-*.jar} is a raw, never-reobfuscated third-party jar
 * (added via {@code compileOnly fileTree(dir: 'libs', ...)}), so its classes keep whatever
 * method names Create's own build produced - e.g. the vanilla
 * {@code AbstractSelectionList.Entry#render} override shows up as {@code m_6311_} on
 * {@code ValueEntry}/{@code LabeledEntry}, not the friendly dev-time name this project's own
 * {@code minecraft} dependency resolves it to. This class's own override keeps the friendly
 * name (so javac recognizes it as a real override and patcherMod's reobf pass renames it
 * correctly for runtime), but its {@code super} call has to name Create's method literally.
 */
public class StringEntry extends ValueEntry<String> {

    private final EditBox textField = new ConfigTextField(Minecraft.getInstance().font, 0, 0, 200, 20);

    public StringEntry(String label, ForgeConfigSpec.ConfigValue<String> value, ForgeConfigSpec.ValueSpec spec) {
        super(label, value, spec);

        // EditBox defaults to a 32-char maxLength, tuned for vanilla's own short text fields.
        // Potion/effect ids ("creativecommon:magic_resistance" alone is 32 chars) blow past
        // that. Past maxLength, EditBox#setValue truncates its *stored* value but still fires the
        // responder with the *untruncated* argument (a real vanilla quirk, not our bug) - so
        // onValueChange's "does the field already show this?" check below would find a permanent
        // mismatch and call setValue again forever: setValue -> truncate -> responder(full) ->
        // setValue(full) -> mismatch -> setValue -> ... (confirmed via a StackOverflowError crash
        // report). Raising maxLength keeps every realistic config string under the truncation
        // branch entirely, so the two values never diverge in the first place.
        this.textField.setMaxLength(256);
        this.textField.setValue(this.getValue());
        this.textField.setTextColor(Theme.i(Theme.Key.TEXT));
        this.textField.setResponder(s -> {
            if (!spec.test(s)) {
                this.textField.setTextColor(Theme.i(Theme.Key.BUTTON_FAIL));
                return;
            }
            this.textField.setTextColor(Theme.i(Theme.Key.TEXT));
            this.setValue(s);
        });
        this.textField.moveCursorToEnd();
        this.listeners.add(this.textField);
        this.onReset();
    }

    @Override
    protected void setEditable(boolean editable) {
        super.setEditable(editable);
        this.textField.setEditable(editable);
    }

    @Override
    public void onValueChange(String newValue) {
        super.onValueChange(newValue);
        if (!this.textField.getValue().equals(newValue)) {
            this.textField.setValue(newValue);
        }
    }

    @Override
    public void tick() {
        super.tick();
        this.textField.tick();
    }

    @Override
    public void render(PoseStack ms, int index, int y, int x, int width, int height,
                        int mouseX, int mouseY, boolean hovered, float partialTicks) {
        // ValueEntry/LabeledEntry (Create's own, unmapped jar) implement this vanilla override
        // under its raw compiled name, not the friendly "render" this method is declared with -
        // see the class javadoc.
        super.m_6311_(ms, index, y, x, width, height, mouseX, mouseY, hovered, partialTicks);
        int labelWidth = this.getLabelWidth(width);
        this.textField.x = x + labelWidth;
        this.textField.y = y + 8;
        this.textField.setWidth(width - labelWidth - resetWidth);
        this.textField.setHeight(20);
        this.textField.render(ms, mouseX, mouseY, partialTicks);
    }
}

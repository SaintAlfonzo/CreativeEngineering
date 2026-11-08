// Behaviour translated from Olympus 1.0.18 (MIT, Terrarium), earth.terrarium.olympus.client.components.dropdown.DropdownState.
package earth.terrarium.tempad.client.ui;

import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;

public class DropdownState<T> implements State<T> {
    private T value;
    private boolean opened;
    @Nullable
    private Button button;

    public DropdownState(T value) {
        this.value = value;
    }

    public static <T> DropdownState<T> of(T value) {
        return new DropdownState<>(value);
    }

    @Override
    public T get() {
        return value;
    }

    @Override
    public void set(T value) {
        this.value = value;
    }

    public boolean isOpened() {
        return opened;
    }

    public void setOpened(boolean opened) {
        this.opened = opened;
    }

    @Nullable
    public Button getButton() {
        return button;
    }

    public void setButton(Button button) {
        this.button = button;
    }

    /** A renderer rebuilt each frame from (value, opened). */
    public WidgetRenderer withRenderer(BiFunction<T, Boolean, WidgetRenderer> factory) {
        return (pose, ctx, pt) -> factory.apply(value, opened).render(pose, ctx, pt);
    }
}

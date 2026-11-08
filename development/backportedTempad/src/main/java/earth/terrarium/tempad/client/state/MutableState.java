// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/client/state/MutableState.kt. Translated to Java for Forge 1.19.2.
package earth.terrarium.tempad.client.state;

import earth.terrarium.tempad.client.ui.State;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class MutableState<T> implements State<T> {
    public final List<Consumer<T>> listeners = new ArrayList<>();
    protected T value;

    public MutableState(T value) {
        this.value = value;
    }

    @SafeVarargs
    public static <T> MutableState<T> of(T value, Consumer<T>... listeners) {
        MutableState<T> state = new MutableState<>(value);
        state.listeners.addAll(List.of(listeners));
        return state;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
        for (Consumer<T> listener : listeners) listener.accept(value);
    }

    @Override
    public T get() {
        return getValue();
    }

    @Override
    public void set(T value) {
        setValue(value);
    }
}

// Behaviour translated from Olympus 1.0.18 (MIT, Terrarium), earth.terrarium.olympus.client.utils.State.
package earth.terrarium.tempad.client.ui;

import java.util.function.Function;

public interface State<T> {
    T get();

    void set(T value);

    /** Olympus State.map: a view of this state through a two-way conversion. */
    default <R> State<R> map(Function<T, R> to, Function<R, T> from) {
        State<T> self = this;
        return new State<>() {
            @Override
            public R get() {
                return to.apply(self.get());
            }

            @Override
            public void set(R value) {
                self.set(from.apply(value));
            }
        };
    }

    /** A renderer that is rebuilt from the current value on every frame. */
    default WidgetRenderer withRenderer(Function<T, WidgetRenderer> factory) {
        return (pose, ctx, pt) -> factory.apply(get()).render(pose, ctx, pt);
    }
}

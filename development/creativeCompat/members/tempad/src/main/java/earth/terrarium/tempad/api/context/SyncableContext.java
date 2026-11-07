// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/api/context/SyncableContext.kt. Translated to Java for Forge 1.19.2.
package earth.terrarium.tempad.api.context;

public interface SyncableContext<T> extends ItemContext {
    ContextType<T> getType();

    T getData();

    default ContextHolder<T> getHolder() {
        return new ContextHolder<>(getType(), getData());
    }
}

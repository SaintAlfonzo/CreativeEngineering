// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/api/PriorityId.kt. Translated to Java for Forge 1.19.2.
package earth.terrarium.tempad.api;

public enum Priority {
    LOWEST(0),
    LOW(1),
    NORMAL(2),
    HIGH(3),
    HIGHEST(4);

    public final int priority;

    Priority(int priority) {
        this.priority = priority;
    }
}

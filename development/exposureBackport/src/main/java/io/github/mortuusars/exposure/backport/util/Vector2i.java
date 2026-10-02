package io.github.mortuusars.exposure.backport.util;

/** The bit of JOML's {@code org.joml.Vector2i} Exposure uses (1.19.2 doesn't ship JOML). */
public class Vector2i {
    public int x;
    public int y;

    public Vector2i() {
    }

    public Vector2i(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Vector2i v && v.x == x && v.y == y;
    }

    @Override
    public int hashCode() {
        return 31 * x + y;
    }

    @Override
    public String toString() {
        return "(" + x + " " + y + ")";
    }
}

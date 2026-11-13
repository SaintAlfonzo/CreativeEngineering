// Copyright 2026 SaintAlfonzo. GPLv3. Minimal stand-in for JUnit 5's @Test (JUnit is not in the offline Gradle cache), so
// upstream Power Grid solver tests compile unchanged for tools/test_power_grid_solver.sh.
package org.junit.jupiter.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Test {
}

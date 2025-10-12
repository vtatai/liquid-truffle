package io.github.liquidTruffle;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LiquidLanguageSanityTest {
    @Test
    public void helloWorldTest() {
        try (Context ctx = Context.newBuilder("liquid")
                .allowAllAccess(true)
                .build()) {
            Value result = ctx.eval("liquid", "hello {{ \"world\" }}");
            assertTrue(result.isString());
            assertThat(result.asString()).isEqualTo("hello world");
        }
    }
}
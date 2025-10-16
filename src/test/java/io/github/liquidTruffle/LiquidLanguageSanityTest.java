package io.github.liquidTruffle;

import io.github.liquidTruffle.parser.LiquidLanguage;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LiquidLanguageSanityTest {
    @Test
    public void objectWithinTextTest() {
        try (Context ctx = Context.newBuilder("liquid")
                .allowAllAccess(true)
                .build()) {
            Value result = ctx.eval("liquid", "hello {{ \"big\" }} world");
            assertTrue(result.isString());
            assertThat(result.asString()).isEqualTo("hello big world");
        }
    }

    @Test
    public void simpleVarTest() {
        try (Context ctx = Context.newBuilder("liquid")
                .allowAllAccess(true)
                .build()) {
            ctx.getBindings(LiquidLanguage.ID).putMember("world", "bob");
            Value result = ctx.eval("liquid", "hello {{ world }}");
            assertTrue(result.isString());
            assertThat(result.asString()).isEqualTo("hello bob");
        }
    }
}
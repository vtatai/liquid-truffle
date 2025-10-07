package io.github.liquidTruffle

import org.assertj.core.api.Assertions.assertThat
import org.graalvm.polyglot.Context
import org.graalvm.polyglot.Value
import org.junit.jupiter.api.Test

class LiquidLanguageSanityTest {
    @Test
    fun helloWorldTest() {
        Context.newBuilder("liquid")
            .allowAllAccess(true)
            .build().use { ctx ->
                val result: Value = ctx.eval("liquid", "hello {{ \"world\" }}")
                assertThat(result.isString)
                assertThat(result.asString()).isEqualTo("hello world")
            }
    }
}
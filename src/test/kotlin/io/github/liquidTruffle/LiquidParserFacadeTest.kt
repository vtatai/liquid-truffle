package io.github.liquidTruffle

import io.github.liquidTruffle.ast.nodes.TextNode
import io.github.liquidTruffle.ast.nodes.VariableNode
import org.junit.jupiter.api.Test
import org.assertj.core.api.Assertions.assertThat

class LiquidParserFacadeTest {
	@Test
	fun canCreateParser() {
		val parser = LiquidParserFacade()
		assertThat(parser).isNotNull()
	}

	@Test
	fun astNodeClassesExist() {
		// Test that the AST node classes can be instantiated (without Truffle context)
		// This verifies the basic structure is correct
		
		// Test TextNode
		val textNode = TextNode("test")
		assertThat(textNode).isNotNull()
		
		// Test VariableNode - it may or may not throw an exception depending on Truffle context
		try {
			val varNode = VariableNode("test", emptyList())
			assertThat(varNode).isNotNull()
		} catch (e: Exception) {
			// Expected due to Truffle context requirement
			assertThat(e.message).contains("Truffle")
		}
	}
}

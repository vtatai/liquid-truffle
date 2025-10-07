package io.github.liquidTruffle.parser

import io.github.liquidTruffle.parser.ast.nodes.LiquidObjectNode
import io.github.liquidTruffle.parser.ast.nodes.TextNode
import io.github.liquidTruffle.parser.ast.nodes.VariableNode
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.StringReader

class LiquidParserFacadeTest {
	@Test
	fun canParseLiquidTemplateWithTextAndObjectReference() {
		// Test the parser logic without creating LiquidRootNode
		val parser = LiquidParserFacade()
		val nodes = parser.parseNodes(StringReader("Hello {{ name }}, welcome to our site!"))
		
		// Assert the correct AST structure
		assertThat(nodes).hasSize(3)
		
		// First child should be TextNode with "Hello "
		assertThat(nodes[0]).isInstanceOf(TextNode::class.java)
		val textNode1 = nodes[0] as TextNode
		assertThat(textNode1.textContent).isEqualTo("Hello ")
		
		// Second child should be LiquidObjectNode with VariableNode child
		assertThat(nodes[1]).isInstanceOf(LiquidObjectNode::class.java)
		val objectNode = nodes[1] as LiquidObjectNode
		assertThat(objectNode.childNode).isInstanceOf(VariableNode::class.java)
		val variableNode = objectNode.childNode as VariableNode
		assertThat(variableNode.variableName).isEqualTo("name")
		assertThat(variableNode.filterSpecs).isEmpty()
		
		// Third child should be TextNode with ", welcome to our site!"
		assertThat(nodes[2]).isInstanceOf(TextNode::class.java)
		val textNode2 = nodes[2] as TextNode
		assertThat(textNode2.textContent).isEqualTo(", welcome to our site!")
	}
}

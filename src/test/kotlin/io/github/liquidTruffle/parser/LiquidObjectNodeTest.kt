package io.github.liquidTruffle.parser

import io.github.liquidTruffle.parser.ast.nodes.LiquidObjectNode
import io.github.liquidTruffle.parser.ast.nodes.NumberLiteralNode
import io.github.liquidTruffle.parser.ast.nodes.StringLiteralNode
import io.github.liquidTruffle.parser.ast.nodes.VariableNode
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import java.io.StringReader

class LiquidObjectNodeTest {

	@Test
	fun canParseObjectWithStringLiteral() {
		val parser = LiquidParserFacade()
		val nodes = parser.parseNodes(StringReader("{{ \"hello\" }}"))

		Assertions.assertThat(nodes).hasSize(1)
		Assertions.assertThat(nodes[0]).isInstanceOf(LiquidObjectNode::class.java)

		val objectNode = nodes[0] as LiquidObjectNode
		Assertions.assertThat(objectNode.childNode).isInstanceOf(StringLiteralNode::class.java)

		val stringLiteral = objectNode.childNode as StringLiteralNode
		Assertions.assertThat(stringLiteral.stringValue).isEqualTo("hello")
	}

	@Test
	fun canParseObjectWithVariable() {
		val parser = LiquidParserFacade()
		val nodes = parser.parseNodes(StringReader("{{ name }}"))

		Assertions.assertThat(nodes).hasSize(1)
		Assertions.assertThat(nodes[0]).isInstanceOf(LiquidObjectNode::class.java)

		val objectNode = nodes[0] as LiquidObjectNode
		Assertions.assertThat(objectNode.childNode).isInstanceOf(VariableNode::class.java)

		val variableNode = objectNode.childNode as VariableNode
		Assertions.assertThat(variableNode.variableName).isEqualTo("name")
	}

	@Test
	fun canParseObjectWithNumberLiteral() {
		val parser = LiquidParserFacade()
		val nodes = parser.parseNodes(StringReader("{{ 42 }}"))

		Assertions.assertThat(nodes).hasSize(1)
		Assertions.assertThat(nodes[0]).isInstanceOf(LiquidObjectNode::class.java)

		val objectNode = nodes[0] as LiquidObjectNode
		Assertions.assertThat(objectNode.childNode).isInstanceOf(NumberLiteralNode::class.java)
	}

	@Test
	fun canParseMixedContentWithObjects() {
		val parser = LiquidParserFacade()
		val nodes = parser.parseNodes(StringReader("Hello {{ name }}, you have {{ count }} messages!"))

		Assertions.assertThat(nodes).hasSize(5)

		// First text node
		Assertions.assertThat(nodes[0].javaClass.simpleName).isEqualTo("TextNode")

		// First object node with variable
		Assertions.assertThat(nodes[1]).isInstanceOf(LiquidObjectNode::class.java)
		val objectNode1 = nodes[1] as LiquidObjectNode
		Assertions.assertThat(objectNode1.childNode).isInstanceOf(VariableNode::class.java)

		// Text node
		Assertions.assertThat(nodes[2].javaClass.simpleName).isEqualTo("TextNode")

		// Second object node with variable
		Assertions.assertThat(nodes[3]).isInstanceOf(LiquidObjectNode::class.java)
		val objectNode2 = nodes[3] as LiquidObjectNode
		Assertions.assertThat(objectNode2.childNode).isInstanceOf(VariableNode::class.java)

		// Final text node
		Assertions.assertThat(nodes[4].javaClass.simpleName).isEqualTo("TextNode")
	}
}
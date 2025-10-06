package io.github.liquidTruffle

import io.github.liquidTruffle.ast.nodes.LiquidObjectNode
import io.github.liquidTruffle.ast.nodes.NumberLiteralNode
import io.github.liquidTruffle.ast.nodes.StringLiteralNode
import io.github.liquidTruffle.ast.nodes.VariableNode
import org.junit.jupiter.api.Test
import org.assertj.core.api.Assertions.assertThat
import java.io.StringReader

class LiquidObjectNodeTest {
	
	@Test
	fun canCreateLiquidObjectNode() {
		val child = StringLiteralNode("test")
		val objectNode = LiquidObjectNode(child)
		
		assertThat(objectNode).isNotNull()
		assertThat(objectNode.childNode).isEqualTo(child)
	}
	
	@Test
	fun canParseObjectWithStringLiteral() {
		val parser = LiquidParserFacade()
		val nodes = parser.parseNodes(StringReader("{{ \"hello\" }}"))
		
		assertThat(nodes).hasSize(1)
		assertThat(nodes[0]).isInstanceOf(LiquidObjectNode::class.java)
		
		val objectNode = nodes[0] as LiquidObjectNode
		assertThat(objectNode.childNode).isInstanceOf(StringLiteralNode::class.java)
		
		val stringLiteral = objectNode.childNode as StringLiteralNode
		assertThat(stringLiteral.stringValue).isEqualTo("hello")
	}
	
	@Test
	fun canParseObjectWithVariable() {
		val parser = LiquidParserFacade()
		val nodes = parser.parseNodes(StringReader("{{ name }}"))
		
		assertThat(nodes).hasSize(1)
		assertThat(nodes[0]).isInstanceOf(LiquidObjectNode::class.java)
		
		val objectNode = nodes[0] as LiquidObjectNode
		assertThat(objectNode.childNode).isInstanceOf(VariableNode::class.java)
		
		val variableNode = objectNode.childNode as VariableNode
		assertThat(variableNode.variableName).isEqualTo("name")
	}
	
	@Test
	fun canParseObjectWithNumberLiteral() {
		val parser = LiquidParserFacade()
		val nodes = parser.parseNodes(StringReader("{{ 42 }}"))
		
		assertThat(nodes).hasSize(1)
		assertThat(nodes[0]).isInstanceOf(LiquidObjectNode::class.java)
		
		val objectNode = nodes[0] as LiquidObjectNode
		assertThat(objectNode.childNode).isInstanceOf(NumberLiteralNode::class.java)
	}
	
	@Test
	fun canParseMixedContentWithObjects() {
		val parser = LiquidParserFacade()
		val nodes = parser.parseNodes(StringReader("Hello {{ name }}, you have {{ count }} messages!"))
		
		assertThat(nodes).hasSize(5)
		
		// First text node
		assertThat(nodes[0].javaClass.simpleName).isEqualTo("TextNode")
		
		// First object node with variable
		assertThat(nodes[1]).isInstanceOf(LiquidObjectNode::class.java)
		val objectNode1 = nodes[1] as LiquidObjectNode
		assertThat(objectNode1.childNode).isInstanceOf(VariableNode::class.java)
		
		// Text node
		assertThat(nodes[2].javaClass.simpleName).isEqualTo("TextNode")
		
		// Second object node with variable
		assertThat(nodes[3]).isInstanceOf(LiquidObjectNode::class.java)
		val objectNode2 = nodes[3] as LiquidObjectNode
		assertThat(objectNode2.childNode).isInstanceOf(VariableNode::class.java)
		
		// Final text node
		assertThat(nodes[4].javaClass.simpleName).isEqualTo("TextNode")
	}
}
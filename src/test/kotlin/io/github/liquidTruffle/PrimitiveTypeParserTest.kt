package io.github.liquidTruffle

import io.github.liquidTruffle.ast.nodes.BooleanLiteralNode
import io.github.liquidTruffle.ast.nodes.LiquidObjectNode
import io.github.liquidTruffle.ast.nodes.NilLiteralNode
import io.github.liquidTruffle.ast.nodes.NumberLiteralNode
import io.github.liquidTruffle.ast.nodes.StringLiteralNode
import io.github.liquidTruffle.ast.nodes.VariableNode
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.StringReader

class PrimitiveTypeParserTest {
	
	@Test
	fun parserCreatesStringLiteralNodes() {
		val parser = LiquidParserFacade()
		val nodes = parser.parseNodes(StringReader("{{ var | upcase:'hello' }}"))
		
		// Should have a LiquidObjectNode with a VariableNode child
		assertThat(nodes).hasSize(1)
		assertThat(nodes[0]).isInstanceOf(LiquidObjectNode::class.java)
		
		val objectNode = nodes[0] as LiquidObjectNode
		assertThat(objectNode.childNode).isInstanceOf(VariableNode::class.java)
		val variableNode = objectNode.childNode as VariableNode
		assertThat(variableNode.filterSpecs).hasSize(1)
		assertThat(variableNode.filterSpecs[0].args).hasSize(1)
		assertThat(variableNode.filterSpecs[0].args[0]).isInstanceOf(StringLiteralNode::class.java)
		
		val stringNode = variableNode.filterSpecs[0].args[0] as StringLiteralNode
		assertThat(stringNode.stringValue).isEqualTo("hello")
	}
	
	@Test
	fun parserCreatesNumberLiteralNodes() {
		val parser = LiquidParserFacade()
		val nodes = parser.parseNodes(StringReader("{{ count | times: 5 }}"))
		
		assertThat(nodes).hasSize(1)
		assertThat(nodes[0]).isInstanceOf(LiquidObjectNode::class.java)
		
		val objectNode = nodes[0] as LiquidObjectNode
		assertThat(objectNode.childNode).isInstanceOf(VariableNode::class.java)
		val variableNode = objectNode.childNode as VariableNode
		assertThat(variableNode.filterSpecs).hasSize(1)
		assertThat(variableNode.filterSpecs[0].args).hasSize(1)
		assertThat(variableNode.filterSpecs[0].args[0]).isInstanceOf(NumberLiteralNode::class.java)
		
		val numberNode = variableNode.filterSpecs[0].args[0] as NumberLiteralNode
		assertThat(numberNode.numberValue).isEqualTo(5)
	}
	
	@Test
	fun parserCreatesBooleanLiteralNodes() {
		val parser = LiquidParserFacade()
		val nodes = parser.parseNodes(StringReader("{{ flag | default:true }}"))
		
		assertThat(nodes).hasSize(1)
		assertThat(nodes[0]).isInstanceOf(LiquidObjectNode::class.java)
		
		val objectNode = nodes[0] as LiquidObjectNode
		assertThat(objectNode.childNode).isInstanceOf(VariableNode::class.java)
		val variableNode = objectNode.childNode as VariableNode
		assertThat(variableNode.filterSpecs).hasSize(1)
		assertThat(variableNode.filterSpecs[0].args).hasSize(1)
		assertThat(variableNode.filterSpecs[0].args[0]).isInstanceOf(BooleanLiteralNode::class.java)
		
		val booleanNode = variableNode.filterSpecs[0].args[0] as BooleanLiteralNode
		assertThat(booleanNode.booleanValue).isTrue()
	}
	
	@Test
	fun parserCreatesNullLiteralNodes() {
		val parser = LiquidParserFacade()
		val nodes = parser.parseNodes(StringReader("{{ value | default:nil }}"))
		
		assertThat(nodes).hasSize(1)
		assertThat(nodes[0]).isInstanceOf(LiquidObjectNode::class.java)
		
		val objectNode = nodes[0] as LiquidObjectNode
		assertThat(objectNode.childNode).isInstanceOf(VariableNode::class.java)
		val variableNode = objectNode.childNode as VariableNode
		assertThat(variableNode.filterSpecs).hasSize(1)
		assertThat(variableNode.filterSpecs[0].args).hasSize(1)
		assertThat(variableNode.filterSpecs[0].args[0]).isInstanceOf(NilLiteralNode::class.java)
	}
	
	@Test
	fun parserHandlesMultiplePrimitiveTypesInFilter() {
		val parser = LiquidParserFacade()
		val nodes = parser.parseNodes(StringReader("{{ text | slice:0,10,true }}"))
		
		assertThat(nodes).hasSize(1)
		assertThat(nodes[0]).isInstanceOf(LiquidObjectNode::class.java)
		
		val objectNode = nodes[0] as LiquidObjectNode
		assertThat(objectNode.childNode).isInstanceOf(VariableNode::class.java)
		val variableNode = objectNode.childNode as VariableNode
		assertThat(variableNode.filterSpecs).hasSize(1)
		assertThat(variableNode.filterSpecs[0].args).hasSize(3)
		
		// First argument should be NumberLiteralNode (0)
		assertThat(variableNode.filterSpecs[0].args[0]).isInstanceOf(NumberLiteralNode::class.java)
		val numberNode1 = variableNode.filterSpecs[0].args[0] as NumberLiteralNode
		assertThat(numberNode1.numberValue).isEqualTo(0)
		
		// Second argument should be NumberLiteralNode (10)
		assertThat(variableNode.filterSpecs[0].args[1]).isInstanceOf(NumberLiteralNode::class.java)
		val numberNode2 = variableNode.filterSpecs[0].args[1] as NumberLiteralNode
		assertThat(numberNode2.numberValue).isEqualTo(10)
		
		// Third argument should be BooleanLiteralNode (true)
		assertThat(variableNode.filterSpecs[0].args[2]).isInstanceOf(BooleanLiteralNode::class.java)
		val booleanNode = variableNode.filterSpecs[0].args[2] as BooleanLiteralNode
		assertThat(booleanNode.booleanValue).isTrue()
	}
}
package io.github.liquidTruffle

import io.github.liquidTruffle.ast.nodes.BooleanLiteralNode
import io.github.liquidTruffle.ast.nodes.NilLiteralNode
import io.github.liquidTruffle.ast.nodes.NumberLiteralNode
import io.github.liquidTruffle.ast.nodes.StringLiteralNode
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class PrimitiveTypeNodesTest {
	
	@Test
	fun stringLiteralNodeWorks() {
		val node = StringLiteralNode("hello world")
		assertThat(node.stringValue).isEqualTo("hello world")
		assertThat(node).isNotNull()
	}
	
	@Test
	fun numberLiteralNodeWorks() {
		val node = NumberLiteralNode(42)
		assertThat(node.numberValue).isEqualTo(42)
		assertThat(node).isNotNull()
		
		val floatNode = NumberLiteralNode(3.14)
		assertThat(floatNode.numberValue).isEqualTo(3.14)
		assertThat(floatNode).isNotNull()
	}
	
	@Test
	fun booleanLiteralNodeWorks() {
		val trueNode = BooleanLiteralNode(true)
		assertThat(trueNode.booleanValue).isTrue()
		assertThat(trueNode).isNotNull()
		
		val falseNode = BooleanLiteralNode(false)
		assertThat(falseNode.booleanValue).isFalse()
		assertThat(falseNode).isNotNull()
	}
	
	@Test
	fun nullLiteralNodeWorks() {
		val node = NilLiteralNode()
		assertThat(node).isNotNull()
	}
}
package io.github.liquidTruffle.ast.nodes

import io.github.liquidTruffle.ast.AstNode
import com.oracle.truffle.api.frame.VirtualFrame
import com.oracle.truffle.api.nodes.NodeInfo

@NodeInfo(description = "Represents string literal values in Liquid templates")
class StringLiteralNode(private val value: String) : AstNode() {
	
	override fun executeGeneric(frame: VirtualFrame): String = value

	// Expose value for testing purposes
	val stringValue: String get() = this.value
}
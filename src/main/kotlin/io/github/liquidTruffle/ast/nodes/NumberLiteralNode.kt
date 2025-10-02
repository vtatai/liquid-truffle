package io.github.liquidTruffle.ast.nodes

import io.github.liquidTruffle.ast.AstNode
import com.oracle.truffle.api.frame.VirtualFrame
import com.oracle.truffle.api.nodes.NodeInfo

@NodeInfo(description = "Represents numeric literal values in Liquid templates")
class NumberLiteralNode(private val value: Number) : AstNode() {
	
	override fun executeGeneric(frame: VirtualFrame): Number = value
	
	// Expose value for testing purposes
	val numberValue: Number get() = this.value
}
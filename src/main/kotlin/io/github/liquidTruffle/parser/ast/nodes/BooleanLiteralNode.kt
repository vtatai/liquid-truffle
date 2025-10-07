package io.github.liquidTruffle.parser.ast.nodes

import io.github.liquidTruffle.parser.ast.AstNode
import com.oracle.truffle.api.frame.VirtualFrame
import com.oracle.truffle.api.nodes.NodeInfo

@NodeInfo(description = "Represents boolean literal values (true/false) in Liquid templates")
class BooleanLiteralNode(private val value: Boolean) : AstNode() {
	
	override fun executeGeneric(frame: VirtualFrame): Boolean = value
	
	// Expose value for testing purposes
	val booleanValue: Boolean get() = this.value
}
package io.github.liquidTruffle.parser.ast.nodes

import io.github.liquidTruffle.parser.ast.AstNode
import com.oracle.truffle.api.frame.VirtualFrame
import com.oracle.truffle.api.nodes.NodeInfo

@NodeInfo(description = "Represents Liquid objects that hold a child object, which can be either a literal or a variable")
class LiquidObjectNode(
	private val child: AstNode
) : AstNode() {
	
	override fun executeGeneric(frame: VirtualFrame): Any? {
		return child.executeGeneric(frame)
	}
	
	// Expose child for testing purposes
	val childNode: AstNode get() = this.child
}
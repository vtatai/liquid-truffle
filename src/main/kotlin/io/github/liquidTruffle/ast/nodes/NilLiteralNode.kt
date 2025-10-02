package io.github.liquidTruffle.ast.nodes

import io.github.liquidTruffle.ast.AstNode
import com.oracle.truffle.api.frame.VirtualFrame
import com.oracle.truffle.api.nodes.NodeInfo

@NodeInfo(description = "Represents nil literal values in Liquid templates")
class NilLiteralNode : AstNode() {
	
	override fun executeGeneric(frame: VirtualFrame): Any? = null
}
package io.github.liquidTruffle.parser.ast.nodes

import io.github.liquidTruffle.parser.ast.AstNode
import com.oracle.truffle.api.frame.VirtualFrame
import com.oracle.truffle.api.nodes.RootNode
import com.oracle.truffle.api.TruffleLanguage
import com.oracle.truffle.api.nodes.NodeInfo

@NodeInfo(description = "Root node of the Liquid template AST that orchestrates execution of child nodes")
class LiquidRootNode(
	language: TruffleLanguage<*>,
	private val children: Array<AstNode>
) : RootNode(language) {
	
	override fun execute(frame: VirtualFrame): String {
		return children.joinToString("") { node ->
			node.executeGeneric(frame)?.toString() ?: ""
		}
	}
	
	// Expose children for testing purposes
	val childrenNodes: Array<AstNode> get() = this.children
}

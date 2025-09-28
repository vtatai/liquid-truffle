package io.github.liquidTruffle.ast.nodes

import io.github.liquidTruffle.ast.AstNode
import com.oracle.truffle.api.frame.VirtualFrame
import com.oracle.truffle.api.nodes.RootNode
import com.oracle.truffle.api.TruffleLanguage

class LiquidRootNode(
	language: TruffleLanguage<*>,
	private val children: Array<AstNode>
) : RootNode(language) {
	
	override fun execute(frame: VirtualFrame): String {
		return children.joinToString("") { node ->
			node.executeGeneric(frame)?.toString() ?: ""
		}
	}
}

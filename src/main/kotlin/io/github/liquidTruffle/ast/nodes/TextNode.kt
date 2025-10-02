package io.github.liquidTruffle.ast.nodes

import io.github.liquidTruffle.ast.AstNode
import com.oracle.truffle.api.frame.VirtualFrame

class TextNode(private val text: String) : AstNode() {
	override fun executeGeneric(frame: VirtualFrame): String = text
	
	// Expose text for testing purposes
	val textContent: String get() = this.text
}

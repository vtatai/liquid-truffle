package io.github.liquidTruffle.ast.nodes

import io.github.liquidTruffle.ast.AstNode
import io.github.liquidTruffle.runtime.LiquidRuntime
import com.oracle.truffle.api.frame.VirtualFrame

class IfNode(
	private val variableName: String,
	private val body: Array<AstNode>
) : AstNode() {
	
	override fun executeGeneric(frame: VirtualFrame): String {
		val v = LiquidRuntime.getVariable(frame, variableName)
		return if (LiquidRuntime.isTruthy(v)) {
			body.joinToString("") { node ->
				node.executeGeneric(frame)?.toString() ?: ""
			}
		} else {
			""
		}
	}
}

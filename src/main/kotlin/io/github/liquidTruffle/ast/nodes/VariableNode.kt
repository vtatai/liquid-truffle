package io.github.liquidTruffle.ast.nodes

import io.github.liquidTruffle.ast.AstNode
import io.github.liquidTruffle.runtime.LiquidRuntime
import com.oracle.truffle.api.frame.VirtualFrame

class VariableNode(
	private val name: String,
	private val filters: List<FilterSpec>
) : AstNode() {
	
	override fun executeGeneric(frame: VirtualFrame): Any? {
		var current = LiquidRuntime.getVariable(frame, name)
		for (filter in filters) {
			current = LiquidRuntime.applyFilter(frame, filter.name, current, filter.args)
		}
		return current
	}
	
	// Expose fields for testing purposes
	val variableName: String get() = this.name
	val filterSpecs: List<FilterSpec> get() = this.filters

	data class FilterSpec(
		val name: String,
		val args: List<Any?> = emptyList()
	)
}

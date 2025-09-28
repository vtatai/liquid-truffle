package io.github.liquidTruffle.runtime

import io.github.liquidTruffle.filters.Filter
import com.oracle.truffle.api.frame.VirtualFrame

object LiquidRuntime {
	
	fun isTruthy(v: Any?): Boolean {
		return when (v) {
			null -> false
			is Boolean -> v
			is Number -> v.toDouble() != 0.0
			else -> v.toString().isNotEmpty()
		}
	}

	@Suppress("UNCHECKED_CAST")
	fun getVariables(frame: VirtualFrame): Map<String, Any?> {
		val args = frame.arguments
		val o = if (args.isNotEmpty()) args[0] else null
		return if (o is Map<*, *>) o as Map<String, Any?> else emptyMap()
	}

	fun getVariable(frame: VirtualFrame, name: String): Any? {
		return getVariables(frame)[name]
	}

	@Suppress("UNCHECKED_CAST")
	fun applyFilter(frame: VirtualFrame, filterName: String, input: Any?, args: List<Any?>): Any? {
		val frameArgs = frame.arguments
		val fObj = if (frameArgs.size > 1) frameArgs[1] else null
		val filters = if (fObj is Map<*, *>) fObj as Map<String, Filter> else emptyMap()
		val filter = filters[filterName] ?: return input
		return filter.apply(input, args, emptyMap())
	}
}

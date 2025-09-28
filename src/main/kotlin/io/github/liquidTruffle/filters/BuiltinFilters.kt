package io.github.liquidTruffle.filters

object BuiltinFilters {
	fun installInto(out: MutableMap<String, Filter>) {
		out["upcase"] = object : Filter {
			override fun apply(input: Any?, args: List<Any?>, kwargs: Map<String, Any?>): Any? {
				return input?.toString()?.uppercase() ?: ""
			}
		}
		out["downcase"] = object : Filter {
			override fun apply(input: Any?, args: List<Any?>, kwargs: Map<String, Any?>): Any? {
				return input?.toString()?.lowercase() ?: ""
			}
		}
		out["append"] = object : Filter {
			override fun apply(input: Any?, args: List<Any?>, kwargs: Map<String, Any?>): Any? {
				val rhs = args.firstOrNull() ?: ""
				return "${input ?: ""}${rhs}"
			}
		}
	}
}

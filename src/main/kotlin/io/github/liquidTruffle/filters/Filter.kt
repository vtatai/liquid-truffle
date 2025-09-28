package io.github.liquidTruffle.filters

interface Filter {
	fun apply(input: Any?, args: List<Any?>, kwargs: Map<String, Any?>): Any?
}

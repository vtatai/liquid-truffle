package io.github.liquidTruffle

import io.github.liquidTruffle.filters.BuiltinFilters
import io.github.liquidTruffle.filters.Filter
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.liquidTruffle.parser.LiquidLanguage
import io.github.liquidTruffle.parser.LiquidParserFacade
import java.nio.file.Files
import java.nio.file.Path

fun main(args: Array<String>) {
	val cli = parseArgs(args)
	val template = cli["template"] ?: if (cli["file"] != null) {
		Files.readString(Path.of(cli["file"]!!))
	} else null
	
	if (template == null) {
		System.err.println("Provide --template or --file")
		System.exit(2)
	}
	
	val vars = parseJson(cli["vars"])

	val parser = LiquidParserFacade()
	val root = parser.parse(LiquidLanguage(), template!!)
	val ct = root.callTarget
	val filters = mutableMapOf<String, Filter>()
	BuiltinFilters.installInto(filters)
	val result = ct.call(vars, filters)
	print(result)
}

private fun parseArgs(args: Array<String>): Map<String, String> {
	val m = mutableMapOf<String, String>()
	var i = 0
	while (i < args.size) {
		if (args[i].startsWith("--")) {
			val key = args[i].substring(2)
			val value = if (i + 1 < args.size) args[++i] else ""
			m[key] = value
		}
		i++
	}
	return m
}

private fun parseJson(s: String?): Map<String, Any?> {
	if (s.isNullOrEmpty()) return emptyMap()
	return try {
		ObjectMapper().readValue(s, object : TypeReference<Map<String, Any?>>() {})
	} catch (e: Exception) {
		throw RuntimeException(e)
	}
}

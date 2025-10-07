package io.github.liquidTruffle.parser

import com.oracle.truffle.api.CallTarget
import com.oracle.truffle.api.TruffleLanguage

@TruffleLanguage.Registration(
	id = LiquidLanguage.ID,
	name = "Liquid",
	version = "0.1",
	defaultMimeType = LiquidLanguage.MIME,
	characterMimeTypes = [LiquidLanguage.MIME]
)
class LiquidLanguage : TruffleLanguage<LiquidLanguage.Context>() {
	companion object {
		const val ID = "liquid"
		const val MIME = "application/x-liquid"
	}

    class Context

    override fun createContext(env: Env): Context = Context()

	override fun parse(request: ParsingRequest): CallTarget {
		val source = request.source
		val parser = LiquidParserFacade()
		val root = parser.parse(this, source.characters.toString())
		return root.callTarget
	}
}
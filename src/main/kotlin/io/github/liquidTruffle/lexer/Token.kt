package io.github.liquidTruffle.lexer

data class Token(
	val type: TokenType,
	val lexeme: String,
	val start: Int,
	val end: Int
) {
	override fun toString(): String = "$type($lexeme)@$start..$end"
}

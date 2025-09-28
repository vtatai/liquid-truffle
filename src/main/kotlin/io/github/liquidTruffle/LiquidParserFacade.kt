package io.github.liquidTruffle

import com.oracle.truffle.api.TruffleLanguage
import io.github.liquidTruffle.ast.AstNode
import io.github.liquidTruffle.lexer.Lexer
import io.github.liquidTruffle.lexer.Token
import io.github.liquidTruffle.lexer.TokenType
import io.github.liquidTruffle.ast.nodes.IfNode
import io.github.liquidTruffle.ast.nodes.LiquidRootNode
import io.github.liquidTruffle.ast.nodes.TextNode
import io.github.liquidTruffle.ast.nodes.VariableNode
import java.io.Reader
import java.io.StringReader

class LiquidParserFacade {
	private lateinit var tokens: List<Token>
	private var p = 0

	fun parse(language: TruffleLanguage<*>, reader: Reader): LiquidRootNode {
		tokens = Lexer(reader).lex()
		p = 0
		val nodes = mutableListOf<AstNode>()
		while (!match(TokenType.EOF)) {
			when {
				check(TokenType.TEXT) -> nodes.add(TextNode(advance().lexeme))
				match(TokenType.VAR_OPEN) -> {
					nodes.add(parseVariable())
					expect(TokenType.VAR_CLOSE, "Expected '}}'")
				}
				match(TokenType.TAG_OPEN) -> {
					val tag = parseTag()
					expect(TokenType.TAG_CLOSE, "Expected '%}'")
					if (tag != null) nodes.add(tag)
				}
				check(TokenType.WHITESPACE) -> {
					// keep whitespace outside tags/vars as text
					nodes.add(TextNode(advance().lexeme))
				}
				else -> {
					// fallback consume
					nodes.add(TextNode(advance().lexeme))
				}
			}
		}
		return LiquidRootNode(language, nodes.toTypedArray())
	}

	fun parse(language: TruffleLanguage<*>, src: String): LiquidRootNode {
		return parse(language, StringReader(src))
	}

	private fun parseVariable(): AstNode {
		skipSpace()
		val name = ident()
		val filters = mutableListOf<VariableNode.FilterSpec>()
		while (true) {
			skipSpace()
			if (!match(TokenType.PIPE)) break
			skipSpace()
			val filterName = ident()
			val args = mutableListOf<Any?>()
			skipSpace()
			if (match(TokenType.COLON)) {
				do {
					skipSpace()
					args.add(literal())
					skipSpace()
				} while (match(TokenType.COMMA))
			}
			filters.add(VariableNode.FilterSpec(filterName, args))
		}
		return VariableNode(name, filters)
	}

	private fun parseTag(): AstNode? {
		skipSpace()
		val kw = ident()
		skipSpace()
		if (kw == "if") {
			val varName = ident()
			expect(TokenType.TAG_CLOSE, "Expected '%}' after if condition")
			val body = mutableListOf<AstNode>()
			while (!(matchSeq(TokenType.TAG_OPEN, TokenType.IDENT, TokenType.TAG_CLOSE) && prev(1).lexeme == "endif")) {
				if (check(TokenType.EOF)) break
				when {
					check(TokenType.TEXT) || check(TokenType.WHITESPACE) -> {
						body.add(TextNode(advance().lexeme))
					}
					match(TokenType.VAR_OPEN) -> {
						body.add(parseVariable())
						expect(TokenType.VAR_CLOSE, "Expected '}}'")
					}
					match(TokenType.TAG_OPEN) -> {
						val nested = parseTag()
						expect(TokenType.TAG_CLOSE, "Expected '%}'")
						if (nested != null) body.add(nested)
					}
					else -> {
						body.add(TextNode(advance().lexeme))
					}
				}
			}
			return IfNode(varName, body.toTypedArray())
		}
		// unknown tag -> ignore
		return null
	}

	private fun skipSpace() {
		while (check(TokenType.WHITESPACE)) advance()
	}

	private fun ident(): String {
		val t = expect(TokenType.IDENT, "Expected identifier")
		return t.lexeme
	}

	private fun literal(): Any? {
		return when {
			match(TokenType.STRING) -> prev().lexeme
			match(TokenType.NUMBER) -> prev().lexeme.toInt()
			match(TokenType.IDENT) -> prev().lexeme
			else -> ""
		}
	}

	private fun check(t: TokenType): Boolean = peek().type == t

	private fun match(t: TokenType): Boolean {
		return if (check(t)) {
			advance()
			true
		} else {
			false
		}
	}

	private fun matchSeq(a: TokenType, b: TokenType, c: TokenType): Boolean {
		return if (check(a)) {
			advance()
			expect(b, "")
			expect(c, "")
			true
		} else {
			false
		}
	}

	private fun expect(t: TokenType, msg: String): Token {
		if (!check(t)) throw RuntimeException("$msg at token ${peek()}")
		return advance()
	}

	private fun advance(): Token = tokens[p++]
	private fun prev(): Token = tokens[p - 1]
	private fun prev(back: Int): Token = tokens[p - back]
	private fun peek(): Token = tokens[p]
}

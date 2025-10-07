package io.github.liquidTruffle.parser

import com.oracle.truffle.api.TruffleLanguage
import io.github.liquidTruffle.parser.ast.AstNode
import io.github.liquidTruffle.parser.ast.nodes.BooleanLiteralNode
import io.github.liquidTruffle.parser.ast.nodes.IfNode
import io.github.liquidTruffle.parser.ast.nodes.LiquidObjectNode
import io.github.liquidTruffle.parser.ast.nodes.LiquidRootNode
import io.github.liquidTruffle.parser.ast.nodes.NilLiteralNode
import io.github.liquidTruffle.parser.ast.nodes.NumberLiteralNode
import io.github.liquidTruffle.parser.ast.nodes.StringLiteralNode
import io.github.liquidTruffle.parser.ast.nodes.TextNode
import io.github.liquidTruffle.parser.ast.nodes.VariableNode
import io.github.liquidTruffle.lexer.Lexer
import io.github.liquidTruffle.lexer.Token
import io.github.liquidTruffle.lexer.TokenType
import java.io.Reader
import java.io.StringReader

class LiquidParserFacade {
	private lateinit var tokens: List<Token>
	private var p = 0

	fun parse(language: TruffleLanguage<*>, reader: Reader): LiquidRootNode {
        return LiquidRootNode(language, parseNodes(reader).toTypedArray())
	}
	
	fun parseNodes(reader: Reader): List<AstNode> {
		tokens = Lexer(reader).lex()
		p = 0
        val nodes = mutableListOf<AstNode>()
        while (!match(TokenType.EOF)) {
            when {
                check(TokenType.TEXT) -> nodes.add(TextNode(advance().lexeme))
                match(TokenType.VAR_OPEN) -> {
                    nodes.add(parseObject())
                    expect(TokenType.VAR_CLOSE, "Expected '}}'")
                }
                match(TokenType.TAG_OPEN) -> {
                    nodes.add(parseTag())
                    expect(TokenType.TAG_CLOSE, "Expected '%}'")
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
		return nodes
	}

	fun parse(language: TruffleLanguage<*>, src: String): LiquidRootNode {
		return parse(language, StringReader(src))
	}

	private fun parseObject(): AstNode {
		skipSpace()
		
		// Check if this is a literal or a variable
		val child = when {
			check(TokenType.STRING) || check(TokenType.NUMBER) || check(TokenType.KEYWORD) -> {
				// Parse as literal
				literal()
			}
			check(TokenType.IDENT) -> {
				// Parse as variable
				parseVariable()
			}
			else -> {
				// Fallback to literal parsing
				literal()
			}
		}
		
		return LiquidObjectNode(child)
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
			val args = mutableListOf<AstNode>()
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

	private fun parseTag(): AstNode {
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
						body.add(parseObject())
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
		throw LiquidParserException("Unsupported / unexpected tag cmmand $kw")
	}

	private fun skipSpace() {
		while (check(TokenType.WHITESPACE)) advance()
	}

	private fun ident(): String {
		val t = when {
			match(TokenType.IDENT) -> prev()
			match(TokenType.KEYWORD) -> prev()
			else -> throw RuntimeException("Expected identifier or keyword")
		}
		return t.lexeme
	}

	private fun literal(): AstNode {
		return when {
			match(TokenType.STRING) -> StringLiteralNode(prev().lexeme)
			match(TokenType.NUMBER) -> NumberLiteralNode(prev().lexeme.toInt())
			match(TokenType.KEYWORD) -> {
				val keyword = prev().lexeme
				when (keyword) {
					"true" -> BooleanLiteralNode(true)
					"false" -> BooleanLiteralNode(false)
					"nil", "null" -> NilLiteralNode()
					else -> StringLiteralNode(keyword) // Treat other keywords as strings
				}
			}
			match(TokenType.IDENT) -> StringLiteralNode(prev().lexeme)
			else -> StringLiteralNode("")
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

package io.github.liquidTruffle.lexer

import java.io.Reader
import java.io.StringReader

enum class LexerMode {
    IN_TEXT,
    IN_OBJ,
    IN_TAG
}

class Lexer(private val reader: Reader, private val reportWhitespaceTokens: Boolean = false) {
	private val buffer = CharArray(4096) // Buffer for reading from Reader
	private var bufferStart = 0
	private var bufferEnd = 0
	private var bufferPos = 0
	private var position = 0 // Global position counter
	private var eof = false
    private var currentMode = LexerMode.IN_TEXT

    private val objectOpenMatcher = TokenMatcherAdapter({peek2("{{")}, ::processObjOpen)
    private val objectCloseMatcher = TokenMatcherAdapter({peek2("}}")}, ::processObjClose)
    private val tagOpenMatcher = TokenMatcherAdapter({peek2("{%")}, ::processTagOpen)
    private val tagCloseMatcher = TokenMatcherAdapter({peek2("%}")}, ::processTagClose)
    private val whitespaceMatcher = TokenMatcherAdapter({peek().isWhitespace()}, ::processWhitespace)
    private val pipeMatcher = TokenMatcherAdapter({peek() == '|'}, ::processPipe)
    private val colonMatcher = TokenMatcherAdapter({peek() == ':'}, ::processColon)
    private val dotMatcher = TokenMatcherAdapter({peek() == '.'}, ::processDot)
    private val commaMatcher = TokenMatcherAdapter({peek() == ','}, ::processComma)
    private val stringMatcher = TokenMatcherAdapter({peek() == '"' || peek() == '\''}, ::processString)
    private val numberMatcher = TokenMatcherAdapter({peek().isDigit()}, ::processNumber)
    private val identMatcher = TokenMatcherAdapter({isIdentStart(peek())}, ::processIdentifier)
    private val textMatcher = TokenMatcherAdapter({peek() != '\u0000'}, ::processText)

    private val textModeMatchers = listOf(objectOpenMatcher, tagOpenMatcher, textMatcher)
    private val objectModeMatchers = listOf(objectCloseMatcher, whitespaceMatcher, stringMatcher, numberMatcher, dotMatcher, pipeMatcher, commaMatcher, colonMatcher, identMatcher)
    private val tagModeMatchers = listOf(tagCloseMatcher, whitespaceMatcher, stringMatcher, numberMatcher, dotMatcher, pipeMatcher, commaMatcher, identMatcher)

    constructor(src: String, reportWhitespaceTokens: Boolean = false) : this(StringReader(src), reportWhitespaceTokens)
	
	fun lex(): List<Token> {
		val tokens = mutableListOf<Token>()
		fillBuffer() // Initial buffer fill

		while (!eof || bufferPos < bufferEnd) {
			val token = processCurrentState()
            if (reportWhitespaceTokens || token.type != TokenType.WHITESPACE) {
                tokens.add(token)
            }
            fillBuffer()
		}

		tokens.add(Token(TokenType.EOF, "", position, position))
		return tokens
	}
	
	private fun processCurrentState(): Token {
        return when(currentMode) {
            LexerMode.IN_TEXT -> textModeMatchers.first(TokenMatcher::match).run()
            LexerMode.IN_TAG -> tagModeMatchers.first(TokenMatcher::match).run()
            LexerMode.IN_OBJ -> objectModeMatchers.first(TokenMatcher::match).run()
        }
	}

	private fun isIdentStart(c: Char): Boolean = c.isLetter() || c == '_' || c == '-'
	private fun isIdentPart(c: Char): Boolean = c.isLetterOrDigit() || c == '_' || c == '-'
	
	private fun collectIdent(): String {
		val result = StringBuilder()
		if (isIdentStart(peek())) {
			result.append(peek())
			advance(1)
		}
		result.append(collectWhile { isIdentPart(it) })
		return result.toString()
	}
	
	private fun parseString(): Token {
		val startPos = position
		val quote = peek()
		advance(1) // consume opening quote
		
		val result = StringBuilder()
		while (!eof || bufferPos < bufferEnd) {
			val c = peek()
			if (c == quote) {
				advance(1) // consume closing quote
				break
			}
			if (c == '\\') {
				advance(1) // consume backslash
				if (!eof || bufferPos < bufferEnd) {
					val next = peek()
					result.append(next)
					advance(1)
				}
			} else {
				result.append(c)
				advance(1)
			}
		}
		
		return Token(TokenType.STRING, result.toString(), startPos, position)
	}
	
	private fun collectText(): String {
		val result = StringBuilder()
		while (!eof || bufferPos < bufferEnd) {
			val c = peek()
			if (c == '\u0000') break // EOF
			
			// Stop at special sequences
			if (peek2("{{") || peek2("}}") || peek2("{%") || peek2("%}")) {
				break
			}
			
			result.append(c)
			advance(1)
		}
		
		// Fallback for single character
		if (result.isEmpty() && (!eof || bufferPos < bufferEnd)) {
			result.append(peek())
			advance(1)
		}
		
		return result.toString()
	}

    // Processing functions
	private fun processObjOpen(): Token {
		val startPos = position
		advance(2)
		currentMode = LexerMode.IN_OBJ
		return Token(TokenType.VAR_OPEN, "{{", startPos, position)
	}

	private fun processObjClose(): Token {
		val startPos = position
		advance(2)
        currentMode = LexerMode.IN_TEXT
		return Token(TokenType.VAR_CLOSE, "}}", startPos, position)
	}

    private fun processTagOpen(): Token {
		val startPos = position
		advance(2)
        currentMode = LexerMode.IN_TAG
		return Token(TokenType.TAG_OPEN, "{%", startPos, position)
	}
	
	private fun processTagClose(): Token {
		val startPos = position
		advance(2)
        currentMode = LexerMode.IN_TEXT
		return Token(TokenType.TAG_CLOSE, "%}", startPos, position)
	}
	
	private fun processWhitespace(): Token {
		val startPos = position
		val lexeme = collectWhile { it.isWhitespace() }
		return Token(TokenType.WHITESPACE, lexeme, startPos, position)
	}
	
	private fun processPipe(): Token {
		val startPos = position
		advance(1)
		return Token(TokenType.PIPE, "|", startPos, position)
	}
	
	private fun processColon(): Token {
		val startPos = position
		advance(1)
		return Token(TokenType.COLON, ":", startPos, position)
	}
	
	private fun processComma(): Token {
		val startPos = position
		advance(1)
		return Token(TokenType.COMMA, ",", startPos, position)
	}
	
	private fun processDot(): Token {
		val startPos = position
		advance(1)
		return Token(TokenType.DOT, ".", startPos, position)
	}

	private fun processString(): Token {
		val token = parseString()
		return token
	}
	
	private fun processNumber(): Token {
		val startPos = position
		val lexeme = collectWhile { it.isDigit() }
		return Token(TokenType.NUMBER, lexeme, startPos, position)
	}
	
	private fun processIdentifier(): Token {
		val startPos = position
		val lexeme = collectIdent()
		return Token(TokenType.IDENT, lexeme, startPos, position)
	}
	
	private fun processText(): Token {
		val startPos = position
		val lexeme = collectText()
		return Token(TokenType.TEXT, lexeme, startPos, position)
	}

    // Buffer management methods
    private fun fillBuffer() {
        if (bufferPos >= bufferEnd && !eof) {
            bufferStart = position
            bufferPos = 0
            val charsRead = reader.read(buffer)
            if (charsRead == -1) {
                eof = true
                bufferEnd = 0
            } else {
                bufferEnd = charsRead
            }
        }
    }

    private fun peek(): Char {
        fillBuffer()
        return if (bufferPos < bufferEnd) buffer[bufferPos] else '\u0000'
    }

    private fun peek2(s: String): Boolean {
        fillBuffer()
        if (bufferPos + s.length > bufferEnd) {
            // Need to check across buffer boundary
            return peekString(s.length) == s
        }
        return bufferPos + s.length <= bufferEnd &&
                buffer.copyOfRange(bufferPos, bufferPos + s.length).contentEquals(s.toCharArray())
    }

    private fun peekString(length: Int): String {
        fillBuffer()
        val result = StringBuilder()
        var remaining = length
        var pos = bufferPos

        while (remaining > 0) {
            if (pos >= bufferEnd) {
                // Need more data
                val charsRead = reader.read(buffer, bufferEnd, buffer.size - bufferEnd)
                if (charsRead == -1) {
                    break // EOF
                }
                bufferEnd += charsRead
            }

            val available = bufferEnd - pos
            val toRead = minOf(remaining, available)
            result.append(buffer, pos, toRead)
            pos += toRead
            remaining -= toRead
        }

        return result.toString()
    }

    private fun advance(count: Int) {
        repeat(count) {
            fillBuffer()
            if (bufferPos < bufferEnd) {
                bufferPos++
                position++
            }
        }
    }

    private fun collectWhile(predicate: (Char) -> Boolean): String {
        val result = StringBuilder()
        while (!eof || bufferPos < bufferEnd) {
            val c = peek()
            if (!predicate(c)) break
            result.append(c)
            advance(1)
        }
        return result.toString()
    }

    private interface TokenMatcher {
        fun match(): Boolean
        fun run(): Token
    }

    private class TokenMatcherAdapter(val matcher:() -> Boolean, val runner: () -> Token): TokenMatcher {
        override fun match(): Boolean {
            return matcher()
        }
        override fun run(): Token {
            return runner()
        }
    }

}

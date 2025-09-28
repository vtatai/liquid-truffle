package io.github.liquidTruffle.lexer

import java.io.Reader
import java.io.StringReader

enum class LexerState {
	INITIAL,           // Starting state, looking for next token
	VAR_OPEN,          // Processing {{ token
	VAR_CLOSE,         // Processing }} token
	TAG_OPEN,          // Processing {% token
	TAG_CLOSE,         // Processing %} token
	WHITESPACE,        // Processing whitespace characters
	PIPE,              // Processing | token
	COLON,             // Processing : token
	COMMA,             // Processing , token
	STRING,            // Processing string literals
	NUMBER,            // Processing numeric literals
	IDENTIFIER,        // Processing identifiers
	TEXT,              // Processing text content
	EOF                // End of file reached
}

class Lexer(private val reader: Reader, private val reportWhitespaceTokens: Boolean = true) {
	private val buffer = CharArray(4096) // Buffer for reading from Reader
	private var bufferStart = 0
	private var bufferEnd = 0
	private var bufferPos = 0
	private var position = 0 // Global position counter
	private var eof = false
	private var currentState = LexerState.INITIAL
	
	constructor(src: String, reportWhitespaceTokens: Boolean = true) : this(StringReader(src), reportWhitespaceTokens)
	
	// Getter for current state (useful for testing and debugging)
	fun getCurrentState(): LexerState = currentState

	fun lex(): List<Token> {
		val tokens = mutableListOf<Token>()
		fillBuffer() // Initial buffer fill
		currentState = LexerState.INITIAL
		
		while (!eof || bufferPos < bufferEnd) {
			val token = processCurrentState()
			if (token != null) {
				tokens.add(token)
			}
		}
		
		// Ensure we end in EOF state
		currentState = LexerState.EOF
		tokens.add(Token(TokenType.EOF, "", position, position))
		return tokens
	}
	
	private fun processCurrentState(): Token? {
		return when (currentState) {
			LexerState.INITIAL -> processInitialState()
			LexerState.VAR_OPEN -> processVarOpenState()
			LexerState.VAR_CLOSE -> processVarCloseState()
			LexerState.TAG_OPEN -> processTagOpenState()
			LexerState.TAG_CLOSE -> processTagCloseState()
			LexerState.WHITESPACE -> processWhitespaceState()
			LexerState.PIPE -> processPipeState()
			LexerState.COLON -> processColonState()
			LexerState.COMMA -> processCommaState()
			LexerState.STRING -> processStringState()
			LexerState.NUMBER -> processNumberState()
			LexerState.IDENTIFIER -> processIdentifierState()
			LexerState.TEXT -> processTextState()
			LexerState.EOF -> null
		}
	}

	private fun isIdentStart(c: Char): Boolean = c.isLetter() || c == '_' || c == '-'
	private fun isIdentPart(c: Char): Boolean = c.isLetterOrDigit() || c == '_' || c == '-'
	
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
	
	// State processing methods
	private fun processInitialState(): Token? {
		val c = peek()
		return when {
			peek2("{{") -> {
				currentState = LexerState.VAR_OPEN
				null // Will be processed in next iteration
			}
			peek2("}}") -> {
				currentState = LexerState.VAR_CLOSE
				null // Will be processed in next iteration
			}
			peek2("{%") -> {
				currentState = LexerState.TAG_OPEN
				null // Will be processed in next iteration
			}
			peek2("%}") -> {
				currentState = LexerState.TAG_CLOSE
				null // Will be processed in next iteration
			}
			c.isWhitespace() -> {
				currentState = LexerState.WHITESPACE
				null // Will be processed in next iteration
			}
			c == '|' -> {
				currentState = LexerState.PIPE
				null // Will be processed in next iteration
			}
			c == ':' -> {
				currentState = LexerState.COLON
				null // Will be processed in next iteration
			}
			c == ',' -> {
				currentState = LexerState.COMMA
				null // Will be processed in next iteration
			}
			c == '"' || c == '\'' -> {
				currentState = LexerState.STRING
				null // Will be processed in next iteration
			}
			c.isDigit() -> {
				currentState = LexerState.NUMBER
				null // Will be processed in next iteration
			}
			else -> {
				currentState = LexerState.TEXT
				null // Will be processed in next iteration
			}
		}
	}
	
	private fun processVarOpenState(): Token? {
		val startPos = position
		advance(2)
		currentState = LexerState.INITIAL
		return Token(TokenType.VAR_OPEN, "{{", startPos, position)
	}
	
	private fun processVarCloseState(): Token? {
		val startPos = position
		advance(2)
		currentState = LexerState.INITIAL
		return Token(TokenType.VAR_CLOSE, "}}", startPos, position)
	}
	
	private fun processTagOpenState(): Token? {
		val startPos = position
		advance(2)
		currentState = LexerState.INITIAL
		return Token(TokenType.TAG_OPEN, "{%", startPos, position)
	}
	
	private fun processTagCloseState(): Token? {
		val startPos = position
		advance(2)
		currentState = LexerState.INITIAL
		return Token(TokenType.TAG_CLOSE, "%}", startPos, position)
	}
	
	private fun processWhitespaceState(): Token? {
		val startPos = position
		val lexeme = collectWhile { it.isWhitespace() }
		currentState = LexerState.INITIAL
		return if (reportWhitespaceTokens) {
			Token(TokenType.WHITESPACE, lexeme, startPos, position)
		} else {
			null
		}
	}
	
	private fun processPipeState(): Token? {
		val startPos = position
		advance(1)
		currentState = LexerState.INITIAL
		return Token(TokenType.PIPE, "|", startPos, position)
	}
	
	private fun processColonState(): Token? {
		val startPos = position
		advance(1)
		currentState = LexerState.INITIAL
		return Token(TokenType.COLON, ":", startPos, position)
	}
	
	private fun processCommaState(): Token? {
		val startPos = position
		advance(1)
		currentState = LexerState.INITIAL
		return Token(TokenType.COMMA, ",", startPos, position)
	}
	
	private fun processStringState(): Token? {
		val token = parseString()
		currentState = LexerState.INITIAL
		return token
	}
	
	private fun processNumberState(): Token? {
		val startPos = position
		val lexeme = collectWhile { it.isDigit() }
		currentState = LexerState.INITIAL
		return Token(TokenType.NUMBER, lexeme, startPos, position)
	}
	
	private fun processIdentifierState(): Token? {
		val startPos = position
		val lexeme = collectIdent()
		currentState = LexerState.INITIAL
		return Token(TokenType.IDENT, lexeme, startPos, position)
	}
	
	private fun processTextState(): Token? {
		val startPos = position
		val lexeme = collectText()
		currentState = LexerState.INITIAL
		return Token(TokenType.TEXT, lexeme, startPos, position)
	}
}
